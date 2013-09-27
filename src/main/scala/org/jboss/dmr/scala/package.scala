package org.jboss.dmr

/**
 * Provides classes for dealing with the management model of a WildFly, JBoss or EAP instance.
 *
 * ==Getting started==
 * Please make sure to import the following packages in order to bring the necessary implicit conversions into scope
 * and to save you some keystrokes.
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
 * ModelNode.empty
 *
 * // creates a new model node with structure
 * ModelNode(
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
 * You can use a DSL like API to set the address and operation for a model node. To describe the "read-resource"
 * operation on "/subsystem=datasources/data-source=ExampleDS" use the following code:
 * {{{
 * ModelNode.empty at ("subsystem" -> "datasources") / ("data-source" -> "ExampleDS") exec 'read_resource(
 *   'include_runtime -> false,
 *   'recursive_depth -> 2
 * )
 * }}}
 *
 * Addresses can be written down as `(String, String)` tuples separated by "/". Operations are specified using
 * [[scala.Symbol]]s and an optional list of parameters. Each parameter is made up of another [[scala.Symbol]] and a
 * value. Using symbols makes the DSL both more readable and extentable.
 *
 * However there's one drawback in using symbols: they cannot contain characters like "-". `'read-resource` is
 * therefore an illegal symbol. As most DMR operations and many parameters do contain "-", this library will replace
 * all underscores in a symbol with dashes:
 * {{{
 * ModelNode.empty exec 'read_resource('include_runtime -> true)
 * // is exactly the same as
 * ModelNode.empty exec Symbol("read-resource")(Symbol("include-runtime") -> true)
 * }}}
 *
 * Here are some more examples using addresses and operations:
 * {{{
 * // root is a constant for an empty address
 * ModelNode.empty at root exec 'read_resource
 * ModelNode.empty at ("subsystem" -> "datasources") / ("data-source" -> "ExampleDS") exec 'disable
 *
 * // parameters are specified as pairs (Symbol -> Any)
 * ModelNode.empty at ("core-service" -> "platform-mbean") / ("type" -> "runtime") exec 'read_resource(
 *   'attributes_only -> true,
 *   'include_runtime -> false,
 *   'recursive_depth -> 3,
 *   'custom_parameter -> "custom-value"
 * )
 *
 * // unsupported parameter types will throw an IllegalArgumentException
 * ModelNode.empty at root exec 'read_resource('proxies -> Console.out)
 * }}}
 *
 * ==Reading==
 * Reading properties from a model node will result in an [[scala.Option[ModelNode]]]. Choose between the "/"
 * operator as in `node / "foo"` (best for reading nested model nodes) or `node("foo")` (best for reading direct
 * child nodes)
 * {{{
 * val node = ModelNode(
 *   "level0" -> ModelNode(
 *     "level1" -> ModelNode(
 *       "level2" -> ModelNode(
 *         "level3" -> ModelNode(
 *           "level4" -> ModelNode("foo" -> "bar")
 *         )
 *       )
 *     )
 *   )
 * )
 *
 * val n1 = node("level0")
 * val n2 = node / "level0" / "level1" / "level2" / "level3"
 * val n3 = for {
 *   l0 <- node / "level0"
 *   l1 <- l0 / "level1"
 *   l2 <- l1 / "level2"
 * } yield l2
 * }}}
 *
 * Since the methods return options, reading nested model nodes is safe even if some children in the path do not
 * exist. In this case `None` wil be returned:
 * {{{
 * val nope = node / "level0" / "oops" / "level2" / "level3"
 * }}}
 *
 * ==Writing==
 * Simple values can be set using `node("foo") = "bar"`. If "foo" doesn't exist it will be created and updated
 * otherwise. As an alternative you can use the `<<` operator, which comes in handy if you want to add multiple
 * key / value pairs:
 * {{{
 * val node = ModelNode.empty
 *
 * node("foo") = "bar"
 * node << ("foo" -> "bar")
 *
 * node << (
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
 * Reading and writing can also be combined in one call:
 * {{{
 * (node / "child" / "deep-inside") << ("foo" -> "xyz")
 * }}}
 *
 * ==Composites==
 * A composite operation is setup using the `ModelNode.composite(n: ModelNode, xn: ModelNode*)` factory method:
 * {{{
 * ModelNode.composite(
 *   ModelNode.empty at ("core-service" -> "management") / ("access" -> "authorization") exec 'read_resource(
 *     'recursive_depth -> 2),
 *   ModelNode.empty at ("core-service" -> "management") / ("access" -> "authorization") exec 'read_children_names(
 *     'name -> "role-mapping"),
 *   ModelNode.empty at ("subsystem" -> "mail") / ("mail-session" -> "*") exec 'read_resource_description,
 *   ModelNode.empty at ("subsystem" -> "datasources") / ("data-source" -> "ExampleDS") exec 'disable ,
 *   ModelNode.empty at ("core-service" -> "platform-mbean") / ("type" -> "runtime") exec 'read_attribute(
 *     'name -> "start-time")
 * )
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
