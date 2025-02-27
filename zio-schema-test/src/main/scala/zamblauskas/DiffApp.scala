package zamblauskas

import zio.schema.Patch

object DiffApp extends App:

  val order1 = Order(id = "123", List(OrderLine(productId = "456", quantity = 3)))
  val order2 = Order(id = "123", List(OrderLine(productId = "456", quantity = 4)))

  val diff: Patch[Order] = orderSchema.diff(order1, order2)
  println(diff)

  assert(diff.patch(order1) == Right(order2))
