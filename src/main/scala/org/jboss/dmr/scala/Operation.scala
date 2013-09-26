package org.jboss.dmr.scala

import org.jboss.dmr.scala.Operation.Parameter

object Operation {
  type Parameter[T] = (Symbol, T)
}

/**
 * An operation of a model node containing optional parameters.
 * @param n The name of the operator as symbol
 */
class Operation(val n: Symbol) {
  val name = Symbol(n.name.replace('_', '-'))
  var params = List.empty[Parameter[_]]

  def apply[T](params: Parameter[T]*): Operation = {
    this.params = params.map((p : Parameter[T]) => Symbol(p._1.name.replace('_', '-')) -> p._2).toList
    this
  }

  override def toString = s"Operation($name,$params)"
}
