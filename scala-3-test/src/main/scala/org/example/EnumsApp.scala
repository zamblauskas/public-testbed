package org.example

object EnumsApp extends App:

  enum Shape(val squared: Double = 0):
    case Circle(radius: Double)                   extends Shape(2 * 3.14 * radius)
    case Rectangle(width: Double, length: Double) extends Shape(width * length)
    case Triangle(a: Double, b: Double, c: Double)

  def processShape(shape: Shape): Unit =
    shape match
      case r @ Shape.Rectangle(w, l)   => println(s"rectangle [$w,$l] - ${r.squared}")
      case c @ Shape.Circle(r)         => println(s"circle [$r] - ${c.squared}")
      case t @ Shape.Triangle(a, b, c) => println(s"triangle [$a,$b,$c] - ${t.squared}")

  val shapes = Vector(
    Shape.Rectangle(10, 20),
    Shape.Triangle(1, 3, 5)
  )

  shapes.foreach(processShape)
