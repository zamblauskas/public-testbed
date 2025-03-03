package org.example

import zio.ZIO
import zio.redis.Redis
import zio.redis.RedisError

object Hashes:

  val userData = Map(
    "id"    -> "1000",
    "name"  -> "John Doe",
    "email" -> "john.doe@example.com",
    "age"   -> "30",
    "city"  -> "New York"
  )

  extension (redis: Redis)
    def hSet(key: String, fields: (String, String)*): ZIO[Any, RedisError, Long] =
      fields match
        case head :: tail => redis.hSet(key, head, tail*)
        case Nil          => ZIO.succeed(0)

  def run: ZIO[Redis, Throwable, Unit] = for {
    redis <- ZIO.service[Redis]
    _     <- redis.hSet(s"user:${userData("id")}", userData.filterKeys(_ != "id").toList*)
  } yield ()
