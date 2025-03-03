package org.example

import zio.ZIO
import zio.durationInt
import zio.json.EncoderOps
import zio.redis.Redis
import zio.redis.Update
import zio.redis.RedisSubscription

object Subscribe:
  def run: ZIO[RedisSubscription, Throwable, Unit] = for {
    redis <- ZIO.service[RedisSubscription]
    _ <- redis
      .subscribeSingle("test-channel")
      .returning[String]
      .foreach(message => ZIO.debug(s"Received message: $message"))
  } yield ()
