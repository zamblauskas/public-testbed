package org.example

object Main extends App:

  trait ContravariantFunctor[F[_]]:
    def contramap[A, B](fa: F[A])(f: B => A): F[B]

  trait Show[A]:
    def show(a: A): String

  given ContravariantFunctor[Show] with
    def contramap[A, B](showA: Show[A])(f: B => A): Show[B] =
      new Show[B]:
        def show(b: B) = showA.show(f(b))

  case class Money(amount: Int)
  case class Salary(amount: Money)

  given Show[Money] with
    def show(a: Money): String = s"${a.amount} USD"

  given Show[Salary] = summon[ContravariantFunctor[Show]].contramap(summon[Show[Money]])(_.amount)

  println(summon[Show[Salary]].show(Salary(Money(100))))
