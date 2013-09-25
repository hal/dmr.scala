package org.jboss.dmr.scala

import org.jboss.dmr.scala.Operation.Parameter

object Operation {
  type Parameter[T] = (Symbol, T)

  object Predefs {
    val access_control = Symbol("access-control")
    val attributes_only = Symbol("attributes-only")
    val include_aliases = Symbol("include-aliases")
    val include_defaults = Symbol("include-defaults")
    val include_runtime = Symbol("include-runtime")
    val read_attribute = Symbol("read-attribute")
    val read_children_names = Symbol("read-children-names")
    val read_children_resources = Symbol("read-children-resources")
    val read_children_types = Symbol("read-children-types")
    val read_operation_description = Symbol("read-operation-description")
    val read_operation_names = Symbol("read-operation-names")
    val read_resource = Symbol("read-resource")
    val read_resource_description = Symbol("read-resource-description")
    val recursive_depth = Symbol("recursive-depth")
    val write_attribute = Symbol("write-attribute")
    val undefine_attribute = Symbol("undefine-attribute")
  }
}

/**
 * An operation of a model node containing optional parameters.
 * @param name The name of the operator as symbol
 */
class Operation(val name: Symbol) {
  var params = List.empty[Parameter[_]]

  def apply(params: Parameter[_]*): Operation = {
    this.params = params.toList
    this
  }

  override def toString = s"Operation($name,$params)"
}
