package org.example

import zio.ZIO
import zio.durationInt
import zio.json.EncoderOps
import zio.redis.Redis
import zio.redis.Update

object Publish:

  def run: ZIO[Redis, Throwable, Unit] = for {
    redis        <- ZIO.service[Redis]
    numReceivers <- redis.publish("test-channel", "Hello, world!")
    _            <- ZIO.debug(s"Published message to test-channel. Received $numReceivers messages")
  } yield ()
