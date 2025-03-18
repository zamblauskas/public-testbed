package org.example

import zio.kafka.consumer.Consumer
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.consumer.Subscription
import zio.kafka.serde.Deserializer
import zio.ZIO
import zio.ZIOAppDefault

object StreamConsumer extends ZIOAppDefault:

  val servers = List("localhost:29092", "localhost:29093", "localhost:29094")
  val topic   = "test-2"
  val groupId = "stream-consumer"

  val app = for {
    consumer <- Consumer.make(
      ConsumerSettings(servers).withGroupId(groupId)
    )
    _ <- consumer
      .plainStream(
        Subscription.topics(topic),
        Deserializer.string,
        Deserializer.string
      )
      .tap(record => ZIO.logInfo(s"Received message: ${record.value}"))
      .mapZIO(record => record.offset.commit)
      .runDrain
  } yield ()

  def run = app
