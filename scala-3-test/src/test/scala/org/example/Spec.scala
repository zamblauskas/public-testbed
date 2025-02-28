package org.example

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class Spec extends AnyFunSpec with Matchers with TypeCheckedTripleEquals {

  describe("Spec") {
    it("should do something") {
      1 should === (1)
    }
  }

}
