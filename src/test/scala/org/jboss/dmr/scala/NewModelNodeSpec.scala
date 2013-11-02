package org.jboss.dmr.scala

import scala.None
import scala.language.reflectiveCalls
import org.scalatest.{FlatSpec, Matchers}

class NewModelNodeSpec extends FlatSpec with Matchers {
  def fixture = new {
    val values = List(true, 12, 1234l, 1.2f, 3.4d)
  }

  "A ModelNode created from a value" should "be a ValueModelNode" in {
    val f = fixture
    for (value <- f.values) {
      val node = ModelNode(value)
      node shouldBe a [ValueModelNode]
    }
  }

  it should "return None for asList()" in {
    val f = fixture
    for (value <- f.values) {
      val node = ModelNode(value)
      node.asList should be (None)
    }
  }

  it should "return Some() for the remaining as...() methods" in {
    val f = fixture
    for (value <- f.values) {
      val node = ModelNode(value)
      node.asBigInt should be ('defined)
      node.asBoolean should be ('defined)
      node.asDouble should be ('defined)
      node.asInt should be ('defined)
      node.asLong should be ('defined)
      node.asString should be ('defined)
    }
  }
}
