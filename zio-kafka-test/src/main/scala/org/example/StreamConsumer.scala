package org.example

import zio.Ref
import zio.UIO
import zio.IO
import zio.ZIO
import zio.ZIOAppDefault
import zio.json.*
import zio.kafka.consumer.CommittableRecord
import zio.kafka.consumer.Consumer
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.consumer.Subscription
import zio.kafka.serde.Deserializer
import zio.stream.ZStream
import zio.durationInt
import scala.collection.immutable.Queue
import zio.Schedule
import zio.kafka.producer.Producer
import zio.kafka.producer.ProducerSettings

object StreamConsumer extends ZIOAppDefault:

  val servers = List("localhost:29092", "localhost:29093", "localhost:29094")
  val topic   = "test-2"
  val groupId = "stream-consumer"

  enum OrderCategory derives JsonCodec:
    case Transport, Materials, Services

  case class Order(
    id: String,
    amount: BigDecimal,
    customerId: CustomerId,
    sequence: Int,
    category: OrderCategory
  ) derives JsonCodec

  type CustomerId = String
  case class CustomerData(sum: BigDecimal, sequences: Queue[Int]):
    def add(amount: BigDecimal, sequence: Int): CustomerData =
      CustomerData(sum + amount, sequences.enqueue(sequence))

  object CustomerData:
    def empty(customerId: CustomerId): CustomerData = CustomerData(0, Queue.empty)

  val app = for {
    consumer    <- Consumer.make(
                     ConsumerSettings(servers).withGroupId(groupId)
                   )
    producer    <- Producer.make(
                     ProducerSettings(servers)
                   )
    databaseRef <- Ref.make(Map.empty[CustomerId, CustomerData])
    _           <- Pipeline
                     .create(consumer, producer, topic)
                     .stage(parseOrder)
                     .stage(filterPositiveAmount)
                     .stage(persistOrder(databaseRef))
                     .commit
                     .run
  } yield ()

  val parseOrder = Pipeline.Stage[String, Order](
    name = "parse order",
    run = { value =>
      ZIO.fromEither(value.fromJson[Order].left.map(Pipeline.Error(_)).map(Some(_)))
    },
    parallelism = Some(8),
    onError = Pipeline.OnError.MoveToDLQ("dlq-parse-order"),
    logBefore = true,
    logAfter = true
  )

  val filterPositiveAmount = Pipeline.Stage[Order, Order](
    name = "filter positive order amount",
    run = { order =>
      ZIO.succeed(Option.when(order.amount > 0)(order))
    },
    parallelism = Some(8),
    onError = Pipeline.OnError.MoveToDLQ("dlq-filter-amount")
  )

  def persistOrder(
    databaseRef: Ref[Map[CustomerId, CustomerData]]
  ) = Pipeline.Stage[Order, Order](
    name = "persist order",
    run = { order =>
      for {
        _ <- databaseRef.update { database =>
               val customerData = database.getOrElse(order.customerId, CustomerData.empty(order.customerId))
               database + (order.customerId -> customerData.add(order.amount, order.sequence))
             }
      } yield Some(order)
    },
    parallelism = None,
    onError = Pipeline.OnError.MoveToDLQ("dlq-persist-order")
  )

  def run = app
