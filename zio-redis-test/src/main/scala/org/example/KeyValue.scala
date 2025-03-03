package org.example

import zio.ZIO
import zio.redis.Redis
import zio.redis.Update
import zio.json.EncoderOps
import zio.json.{jsonField, JsonCodec}
import zio.durationInt

object KeyValue:
  case class Item(
    id: String,
    description: String,
    price: BigDecimal,
    @jsonField("version_id") versionId: String
  ) derives JsonCodec

  val items = List(
    Item(
      id = "prod_789",
      description = "Shampoo",
      price = BigDecimal(19.99),
      versionId = "1"
    ),
    Item(
      id = "prod_456",
      description = "Conditioner",
      price = BigDecimal(14.99),
      versionId = "1"
    ),
    Item(
      id = "prod_012",
      description = "Hair Gel",
      price = BigDecimal(12.99),
      versionId = "1"
    )
  )

  def run: ZIO[Redis, Throwable, Unit] = for {
    redis <- ZIO.service[Redis]
    _ <- ZIO.foreach(items) { item =>
      redis.set(item.id, item.toJson, expireTime = Some(10.minutes), update = Some(Update.SetNew))
    }
  } yield ()
