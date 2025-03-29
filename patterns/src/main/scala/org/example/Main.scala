package org.example

import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.Ref
import java.time.Instant
import java.time.LocalDate
import cats.syntax.all._

object CQRS extends IOApp:

  // ---- Domain model ----
  case class Order(
    id: String,
    customerId: String,
    amount: BigDecimal,
    description: String,
    createdAt: Instant
  )

  // ---- Events ----
  sealed trait OrderEvent
  case class OrderCreated(order: Order) extends OrderEvent
  case class OrderCancelled(id: String) extends OrderEvent

  // ---- Command service ----
  class OrderCommandService(
    eventHandler: OrderEvent => IO[Unit]
  ):
    def createOrder(order: Order): IO[Unit] =
      IO.println(s"Publishing OrderCreated event for order ${order.id}") *>
        eventHandler(OrderCreated(order))

    def cancelOrder(id: String): IO[Unit] =
      IO.println(s"Publishing OrderCancelled event for order $id") *>
        eventHandler(OrderCancelled(id))

  // ---- Query services ----
  class OrdersByCustomerQueryService(ref: Ref[IO, Map[String, List[Order]]]):

    def handleEvent(event: OrderEvent): IO[Unit] =
      event match
        case OrderCreated(order) =>
          IO.println(s"OrdersByCustomerQueryService: Processing OrderCreated for ${order.id}") *>
            ref.update(orders =>
              val updated = orders.getOrElse(order.customerId, List.empty) :+ order
              orders.updated(order.customerId, updated)
            )
        case OrderCancelled(id) =>
          IO.println(s"OrdersByCustomerQueryService: Processing OrderCancelled for $id") *>
            ref.update(orders =>
              orders.map { case (customerId, orderList) =>
                customerId -> orderList.filter(_.id != id)
              }
            )

    def getOrders(customerId: String): IO[List[Order]] =
      ref.get.map(_.getOrElse(customerId, List.empty))

  class OrdersByDateRangeQueryService(ref: Ref[IO, Map[LocalDate, List[Order]]]):
    def handleEvent(event: OrderEvent): IO[Unit] =
      event match
        case OrderCreated(order) =>
          IO.println(s"OrdersByDateRangeQueryService: Processing OrderCreated for ${order.id}") *>
            ref.update(orders =>
              val date    = LocalDate.ofInstant(order.createdAt, java.time.ZoneOffset.UTC)
              val updated = orders.getOrElse(date, List.empty) :+ order
              orders.updated(date, updated)
            )
        case OrderCancelled(id) =>
          IO.println(s"OrdersByDateRangeQueryService: Processing OrderCancelled for $id") *>
            ref.update(orders =>
              orders.map { case (date, orderList) =>
                date -> orderList.filter(_.id != id)
              }
            )

    def getOrders(date: LocalDate): IO[List[Order]] =
      ref.get.map(_.getOrElse(date, List.empty))

  // ---- Application ----
  def run(args: List[String]): IO[ExitCode] =
    for
      byCustomerRef <- Ref.of[IO, Map[String, List[Order]]](Map.empty)
      byCustomerQueryService = OrdersByCustomerQueryService(byCustomerRef)
      byDateRangeRef <- Ref.of[IO, Map[LocalDate, List[Order]]](Map.empty)
      byDateRangeQueryService = OrdersByDateRangeQueryService(byDateRangeRef)

      commandService = OrderCommandService((event: OrderEvent) =>
        IO.println(s"Processing event: $event") *>
          byCustomerQueryService.handleEvent(event) *>
          byDateRangeQueryService.handleEvent(event)
      )

      _ <- IO.println("Starting to publish events...")
      _ <- commandService.createOrder(Order("1", "1", BigDecimal(99.99), "Order 1", Instant.now()))
      _ <- commandService.createOrder(Order("2", "1", BigDecimal(123.50), "Order 2", Instant.now()))
      _ <- commandService.createOrder(Order("3", "1", BigDecimal(100.00), "Order 3", Instant.now()))
      _ <- commandService.createOrder(Order("4", "1", BigDecimal(199.99), "Order 4", Instant.now()))
      _ <- commandService.cancelOrder("2")
      _ <- IO.println("All events published.")

      ordersByCustomer <- byCustomerQueryService.getOrders("1")
      _                <- IO.println(s"Orders by customer: $ordersByCustomer")

      ordersByDate <- byDateRangeQueryService.getOrders(LocalDate.now())
      _            <- IO.println(s"Orders by date: $ordersByDate")
    yield ExitCode.Success
