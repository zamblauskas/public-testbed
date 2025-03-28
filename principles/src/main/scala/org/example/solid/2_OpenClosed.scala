package org.example.solid

// Classes should be open for extension but closed for modification
// In this example, we can add new discounts without modifying the PriceCalculator class
object `2_OpenClosed` extends App:

  trait Discount:
    def apply(totalAmount: BigDecimal, numItems: Int): BigDecimal

  val newUserDiscount = new Discount:
    def apply(totalAmount: BigDecimal, numItems: Int): BigDecimal =
      totalAmount * 0.9

  val bulkDiscount = new Discount:
    def apply(totalAmount: BigDecimal, numItems: Int): BigDecimal =
      if numItems > 10 then totalAmount * 0.95 else totalAmount

  case class OrderLine(product: String, price: BigDecimal, quantity: Int)

  class PriceCalculator(discounts: List[Discount]):
    def calculate(lines: List[OrderLine]): BigDecimal =
      lines
        .map(line =>
          discounts.foldLeft(line.price * line.quantity) { (acc, discount) =>
            discount(acc, line.quantity)
          }
        )
        .sum

  val calculator = new PriceCalculator(List(newUserDiscount, bulkDiscount))
  val orderLines = List(
    OrderLine("Product 1", price = 10.9, quantity = 1),
    OrderLine("Product 2", price = 9.99, quantity = 5),
    OrderLine("Product 3", price = 2.49, quantity = 11)
  )
  val total = calculator.calculate(orderLines)
  println(s"Total: $total")
