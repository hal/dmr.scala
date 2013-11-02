package org.jboss.dmr.scala

import org.scalatest.{Matchers, FlatSpec}

class OperationSpec extends FlatSpec with Matchers {

  "An Operation" should "replace '_' with '-' for its name" in {
    val op = Operation('symbol_with_underscores)
    assert(Symbol("symbol-with-underscores") === op.name)
  }

  it should "replace '_' with '-' for its parameters" in {
    val op = Operation('foo) {
      'param_1 -> "foo"
    }
    assert(Symbol("param-1") === op.params.head._1)
  }
}
