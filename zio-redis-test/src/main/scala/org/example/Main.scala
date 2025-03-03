package org.example

import zio.ZIO
import zio.ZIOAppDefault
import zio.ZLayer
import zio.redis.CodecSupplier
import zio.redis.Redis
import zio.redis.RedisClusterConfig
import zio.Chunk
import zio.redis.RedisUri
import zio.redis.RedisSubscription
import zio.redis.RedisConfig

object Main extends ZIOAppDefault {

  val app = for {
    redis <- ZIO.service[Redis]

    _ <- KeyValue.run
    _ <- Hashes.run
    _ <- Publish.run
    _ <- Subscribe.run
    // increment counter
    _ <- redis.incr("version_id")
  } yield ()

  override def run = app.provide(
    Redis.cluster,
    RedisSubscription.singleNode,
    ZLayer.succeed(
      RedisConfig("127.0.0.1", 6380)
    ),
    ZLayer.succeed(
      RedisClusterConfig(
        addresses = Chunk(
          RedisUri("127.0.0.1", 6379),
          RedisUri("127.0.0.1", 6380),
          RedisUri("127.0.0.1", 6381)
        )
      )
    ),
    ZLayer.succeed(CodecSupplier.utf8)
  )
}
