package org.example.solid

// Subclass should substitute parent class without breaking behavior
object `3_LiskovSubstitution` extends App:

  trait File:
    def read(): String
    def write(content: String): Unit

  class TextFile extends File:
    private var content: String      = ""
    def read(): String               = content
    def write(content: String): Unit = this.content = content

  class ReadOnlyFile(file: File) extends File:
    def read(): String = file.read()
    def write(content: String): Unit = throw new UnsupportedOperationException(
      "I violate Liskov Substitution Principle"
    )
