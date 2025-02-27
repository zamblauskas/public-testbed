package zamblauskas

import zio.schema.Schema
import zio.schema.DeriveSchema

case class Order(id: String, lines: List[OrderLine])
case class OrderLine(productId: String, quantity: Int)

// automatic Schema derivation
given orderSchema: Schema[Order]        = DeriveSchema.gen[Order]
given ordersSchema: Schema[List[Order]] = Schema.list(orderSchema)
// Schema for OrderLine is not generated because it is not used

val orders = List(
  Order("123", List(OrderLine("456", 3))),
  Order("789", List(OrderLine("101", 1)))
)
