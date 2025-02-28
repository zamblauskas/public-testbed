package org.example

object ImplicitsApp extends App:

  trait Schema[A]:
    def encode(value: A): String

  case class Data(key: String, value: String)

  // anonymous given
  given Schema[Data] with
    def encode(value: Data): String = s"${value.key}->${value.value}"

  // summoning the given
  def printSchema[A: Schema](value: A): Unit =
    val str = summon[Schema[A]].encode(value)
    println(str)

  // using the given
  def printSchema2[A](value: A)(using schema: Schema[A]): Unit =
    val str = schema.encode(value)
    println(str)

  printSchema(Data("key", "value"))
  printSchema2(Data("key", "value"))
