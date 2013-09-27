package org.jboss.dmr

/**
 * Provides classes for dealing with the management model of a WildFly, JBoss and EAP instance.
 *
 * ==Getting started==
 * Please make sure to import the following packages in order to bring the necessary implicit conversions into
 * scope and to save you some keystrokes:
 * {{{
 * import org.jboss.dmr.scala._
 * import org.jboss.dmr.scala.ModelNode
 * }}}
 *
 * ==Creating model nodes==
 * The main class to use is [[org.jboss.dmr.scala.ModelNode]]. Its companion object acts as a factory for creating new
 * model node instances.
 * {{{
 * // creates an empty model node
 * val en = ModelNode.empty
 *
 * // creates new model node with structure
 * val node = ModelNode(
 *   "flag" -> true,
 *   "hello" -> "world",
 *   "answer" -> 42,
 *   "child" -> ModelNode(
 *     "inner-a" -> 123,
 *     "inner-b" -> "test",
 *     "deep-inside" -> ModelNode("foo" -> "bar"),
 *     "deep-list" -> List(
 *       ModelNode("one" -> 1),
 *       ModelNode("two" -> 2),
 *       ModelNode("three" -> 3)
 *     )
 *   )
 * )
 * }}}
 *
 * ==Addressing and operations==
 * You can use a DSL like API to set the address and operation for a model node. To execute a "read-resource"
 * operation on "/subsystem=datasources/data-source=ExampleDS" use the following code:
 * node:
 * {{{
 * val rro = ModelNode.empty at ("subsystem" -> "datasources") / ("data-source" -> "ExampleDS") exec 'read-resource(
 *   'include_runtime -> flase,
 *   'recursive-depth -> 2
 * )
 * }}}
 *
 * ==Getting and settings values==
 * Reading properties from a model node you will result in `Option[ModelNode]`. Choose between the "/" operator (best
 * used to read nested model nodes) or `node("foo")` (for reading direct child nodes)
 *{{{
 * // node as defined above
 * val answer = node("answer")
 * val deepInside = node / "child" / "deep-inside"
 * }}}
 *
 * ==Implicit Conversions==
 * This package contains the following implicit conversions which are used to support a DSL like API:
 *
 * <ul>
 * <li> `(String, String)` to [[org.jboss.dmr.scala.Address]]
 * <li> [[scala.Symbol]] to [[org.jboss.dmr.scala.Operation]]
 * <li> [[scala.Some[ModelNode]]] to the relevant model node
 * <li> [[scala.None[ModelNode]]] to an empty model node
 * </ul>
 *
 */
package object scala {
  /** An empty address */
  val root = new Address(List())

  implicit def tupleToAddress(tuple: (String, String)) = new Address(List(tuple))

  implicit def symbolToOperation(name: Symbol) = new Operation(name)

  implicit def optionToModelNode(opt: Option[ModelNode]) = opt.getOrElse(ModelNode.empty())
}
