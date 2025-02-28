package org.example

object ExtensionMethodsApp extends App:

  case class Person(name: String, age: Int)

  extension (p: Person) def isAdult: Boolean = p.age >= 18

  val p = Person("John", 30)
  println(p.isAdult)
