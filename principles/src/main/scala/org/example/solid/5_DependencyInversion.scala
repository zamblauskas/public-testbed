package org.example.solid

// High-level modules should not depend on low-level modules.
// Both should depend on abstractions.
object `5_DependencyInversion` extends App:
  // Bad example
  // OrderService depends on Stripe
  // class OrderService:
  //   val gateway = StripeGateway()
  //   def checkout(order: Order): Unit = gateway.charge()

  case class Order(id: String, amount: BigDecimal)

  trait PaymentGateway:
    def charge(amount: BigDecimal): Unit

  class OrderService(gateway: PaymentGateway):
    def checkout(order: Order): Unit = gateway.charge(order.amount)

  class StripeGateway extends PaymentGateway:
    def charge(amount: BigDecimal): Unit = println(s"Charging $amount")

  val orderService = OrderService(StripeGateway())
  orderService.checkout(Order("123", 100))
