package org.jboss.dmr.scala

import org.jboss.dmr.scala.Operation.Parameter

object Operation {
  type Parameter = (Symbol, Any)
}

/**
 * An operation of a model node containing optional parameters.
 * @param name The name of the operator as symbol
 */
class Operation(val name: Symbol) {
  var params = List.empty[Parameter]

  def apply(params: Parameter*): Operation = {
    this.params = params.toList
    this
  }

  override def toString = s"Operation($name,$params)"
}

object Predefs {
  val attributes_only = Symbol("attributes-only")
  val read_attribute = Symbol("read-attribute")
  val read_children_names = Symbol("read-children-names")
  val read_resource = Symbol("read-resource")
  val read_resource_description = Symbol("read-resource-description")
  val recursive_depth = Symbol("recursive-depth")
}
