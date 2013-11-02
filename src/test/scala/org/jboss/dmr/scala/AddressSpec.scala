package org.jboss.dmr.scala

import org.scalatest.{Matchers, FlatSpec}

class AddressSpec extends FlatSpec with Matchers {

  "An Address" should "be combinable using the '/' operator" in {
    val ab = Address(List(("a", "b")))
    val cd = Address(List(("c", "d")))
    val abcd = ab / cd
    assert(Address(List(("a", "b"), ("c", "d"))) === abcd)
  }

  it should "use an implicit conversion from (String, String)" in {
    val abcd = ("a" -> "b") / ("c" -> "d")
    assert(Address(List(("a", "b"), ("c", "d"))) === abcd)
  }
}
