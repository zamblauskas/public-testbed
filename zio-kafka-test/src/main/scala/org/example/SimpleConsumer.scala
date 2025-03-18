package org.example

import zio.kafka.consumer.Consumer
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.consumer.Subscription
import zio.kafka.serde.Deserializer
import zio.ZIO
import zio.ZIOAppDefault

object SimpleConsumer extends ZIOAppDefault:

  val servers = List("localhost:29092", "localhost:29093", "localhost:29094")
  val topic   = "test-2"
  val groupId = "simple-consumer"

  val consumer = Consumer.consumeWith(
    ConsumerSettings(servers).withGroupId(groupId),
    Subscription.topics(topic),
    Deserializer.string,
    Deserializer.string
  ) { record =>
    ZIO.logInfo(s"Received message: ${record.value}")
  }

  def run = consumer
