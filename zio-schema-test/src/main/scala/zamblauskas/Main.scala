package zamblauskas

import zio.schema.Schema
import zio.schema.DeriveSchema
import zio.schema.codec.{JsonCodec, ProtobufCodec}

object Main extends App {

  val jsonCodec     = JsonCodec.jsonCodec[Order](orderSchema)
  val protobufCodec = ProtobufCodec.protobufCodec[Order]

  val order = Order("123", List(OrderLine("456", 3)))

  val json = jsonCodec.encodeJson(order, indent = Some(4))
  println(json)

  val decodedJson = jsonCodec.decodeJson(json)
  println(decodedJson)

  val protobuf = protobufCodec.encode(order)
  println(protobuf)

  val decodedProtobuf = protobufCodec.decode(protobuf)
  println(decodedProtobuf)
}
