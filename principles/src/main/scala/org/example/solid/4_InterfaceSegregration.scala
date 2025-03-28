package org.example.solid

// Clients should not be forced to depend on interfaces that they do not use
// Instead of one fat interface, split it into smaller ones
object `4_InterfaceSegregration` extends App:

  // Bad example
  // Machine is a fat interface that has two unrelated methods
  // trait Machine:
  //   def print(str: String): Unit
  //   def scan(): String

  trait Printer:
    def print(str: String): Unit

  trait Scanner:
    def scan(): String
