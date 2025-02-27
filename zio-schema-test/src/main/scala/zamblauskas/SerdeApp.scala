package zamblauskas

import zio.schema.codec.{JsonCodec, ProtobufCodec}

object SerdeApp extends App {

  // round trip JSON
  val ordersJsonCodec = JsonCodec.jsonCodec[List[Order]](ordersSchema)
  val ordersJsonValue = ordersJsonCodec.encodeJson(orders, indent = Some(4))
  println(ordersJsonValue)
  val ordersDecodedJson = ordersJsonCodec.decodeJson(ordersJsonValue)
  println(ordersDecodedJson)

  // round trip Protobuf
  val ordersProtobufCodec = ProtobufCodec.protobufCodec[List[Order]](ordersSchema)
  val ordersProtobufValue = ordersProtobufCodec.encode(orders)
  println(ordersProtobufValue)
  val ordersDecodedProtobuf = ordersProtobufCodec.decode(ordersProtobufValue)
  println(ordersDecodedProtobuf)
}
