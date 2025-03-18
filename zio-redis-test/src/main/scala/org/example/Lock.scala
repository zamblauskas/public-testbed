package org.example

import zio.{IO, ZIO}
import zio.redis.Redis
import zio.redis.RedisError
import zio.durationInt
import zio.Random

object Lock:
  def run: ZIO[Redis, Throwable, Unit] = for {
    redis <- ZIO.service[Redis]
    _     <- redis.setNx("lock:user:1", "1")
    _     <- redis.expire("lock:user:1", 60.seconds)
    _     <- action(redis)
    _     <- redis.del("lock:user:1")
  } yield ()

  def action(redis: Redis): IO[RedisError, Unit] = for {
    version        <- redis.hGet("user:1", "version").returning[Int]
    randomDuration <- Random.nextIntBetween(1, 10).map(_.seconds)
    _              <- ZIO.sleep(randomDuration)
    _              <- redis.hSet("user:1", "version" -> (version.getOrElse(0) + 1))
  } yield ()
