package org.jboss.dmr.scala

import scala.collection.JavaConversions._
import org.jboss.dmr.{ModelNode => JavaModelNode}
import org.jboss.dmr.ModelType._

/** Factory for [[org.jboss.dmr.scala.ModelNode]] */
object ModelNode {

  /** Creates an empty model node */
  def empty(): ModelNode = new ComplexModelNode()

  /**
   * Creates a new model node with the specified key / value pairs. Use this method to create a hirarchy of model
   * nodes:
   * {{{
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
   * @param tuples the key / value pairs
   */
  def apply(tuples: (String, Any)*): ModelNode = {
    val node = empty()
    tuples.toList.foreach(tuple => node(tuple._1) = tuple._2)
    node
  }

  /**
   * Creates a new composite containing the specified model nodes
   *
   * @param n the first model node
   * @param xn additional model nodes
   */
  def composite(n: ModelNode, xn: ModelNode*): ModelNode = {
    val node = empty exec 'composite
    node("steps") = List(n) ++ xn
    node
  }
}

/**
 * A Scala wrapper around a `org.jboss.dmr.ModelNode` offering methods to interact with model nodes in a more
 * natural way.
 *
 * @param javaModelNode the underlying Java `org.jboss.dmr.ModelNode`
 */
abstract class ModelNode(javaModelNode: JavaModelNode) {

  /** Returns the underlying Java mode node */
  def underlying = javaModelNode

  /**
   * Sets the address for this model node. An address can be specified using `(String, String)` tuples seperated
   * by "/". Thus an expression of  `("subsystem" -> "datasources") / ("data-source" -> "ExampleDS")` will be
   * implicitly converted to an address.
   *
   * @param address the address
   * @return this model node with the address st
   */
  def at(address: Address): ModelNode

  /**
   * Sets the specified operation for this model node. The operation can be specified as a [[scala.Symbol]] which will
   * be implicitly converted to an operation.
   *
   * @param operation the operation.
   * @return this model node with the operation set
   */
  def exec(operation: Operation): ModelNode

  /**
   * Returns an option for the nested model node with the specified key. You can chain calls to "/" to lookup deeply
   * nested model nodes:
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
   * val level3 = node / "level0" / "level1" / "level2" / "level3"
   * }}}
   *
   * The method is safe to call for children that does not exist in the path. In this case [[scala.None]] wil be
   * returned:
   * {{{
   * val nope = node / "level0" / "level10" / "level2" / "level3"
   * }}}
   *
   * @param key the key of the nested model node
   * @return [[scala.Some]] if the nested model node exists, [[scala.None]] otherwise
   */
  def /(key: String): Option[ModelNode] = lookup(key)

  /**
   * Returns an option for the nested model node with the specified key. In contrast to
   * [[org.jboss.dmr.scala.ModelNode#/]] works best for direct children.
   *
   * @param key the key of the nested model node
   * @return [[scala.Some]] if the nested model node exists, [[scala.None]] otherwise
   */
  def apply(key: String): Option[ModelNode] = lookup(key)

  protected def lookup(key: String) = {
    if (underlying.has(key)) {
      val jchild = underlying.get(key)
      if (isSimple(jchild)) {
        Some(new ValueModelNode(jchild))
      } else {
        Some(new ComplexModelNode(jchild))
      }
    } else None
  }

  protected def isSimple(jnode: JavaModelNode) = jnode.getType match {
    case BIG_DECIMAL | BIG_INTEGER | BOOLEAN | BYTES | DOUBLE | INT | LONG | STRING => true
    case _ => false
  }

  /**
   * Adds multiple name / value pairs to this model node. Use this method to add a hirarchy of model nodes:
   * {{{
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
   * @param tuples the name / value pairs
   */
  def <<(tuples: (String, Any)*) = tuples.foreach(tuple => this(tuple._1) = tuple._2)

  /**
   * Sets the specified key to the given value. Supports the following types:
   * <ul>
   * <li> Boolean
   * <li> Int
   * <li> Long
   * <li> BigInt
   * <li> Float
   * <li> Double
   * <li> BigDecimal
   * <li> String
   * <li> ModelNode
   * <li> Traversable[ModelNode]
   * </ul>
   *
   * @param name the name
   * @param value the value
   * @throws IllegalArgumentException if the value type is not supported
   */
  @throws[IllegalArgumentException]("if the value type is not supported")
  def update(name: String, value: Any) {
    value match {
      case boolean: Boolean => underlying.get(name).set(boolean)
      case int: Int => underlying.get(name).set(int)
      case long: Long => underlying.get(name).set(long)
      case bigInt: BigInt => underlying.get(name).set(bigInt.underlying())
      case float: Float => underlying.get(name).set(float)
      case double: Double => underlying.get(name).set(double)
      case bigDecimal: BigDecimal => underlying.get(name).set(bigDecimal.underlying())
      case string: String => underlying.get(name).set(string)
      case node: ModelNode => underlying.get(name).set(node.underlying)
      case values: Traversable[_] => {
        try {
          // only list of model nodes are supported!
          val nodes = values.asInstanceOf[Traversable[ModelNode]]
          val javaNodes = nodes.toList.map(_.underlying)
          javaModelNode.get(name).set(javaNodes)
        } catch {
          case e: ClassCastException => throw new IllegalArgumentException(
            s"""Illegal type parameter in type ${value.getClass.getName} for "$name".""")
        }
      }
      case _ => throw new IllegalArgumentException( s"""Illegal type ${value.getClass.getName} for "$name".""")
    }
  }

  /**
   * Returns the keys for this model node. If this model node does not have any keys an empty set is returned
   * */
  def keys: Set[String] = {
    underlying.getType match {
      case OBJECT => underlying.keys.toSet
      case _ => Set()
    }
  }

  /**
   * Returns the children of this model node as key / value pairs. If this model node does not have any children,
   * an empty set is returned
   * */
  def values: Set[(String, ModelNode)] = {
    underlying.getType match {
      case OBJECT => underlying.asList().map(jnode => {
        val name = jnode.asProperty().getName
        val jvalue = jnode.asProperty().getValue
        val node = if (isSimple(jvalue)) new ValueModelNode(jvalue) else new ComplexModelNode(jvalue)
        name -> node
      }).toSet
      case _ => Set()
    }
  }

  /** Delegates to `underlying.toString` */
  override def toString = underlying.toString
}

/**
 * Implementation for complex model nodes.
 *
 * @param javaModelNode the underlying Java `org.jboss.dmr.ModelNode`
 */
class ComplexModelNode(javaModelNode: JavaModelNode = new JavaModelNode()) extends ModelNode(javaModelNode) {

  override def at(address: Address): ModelNode = {
    emptyAddress()
    address.tuples.foreach(tuple => underlying.get("address").add(tuple._1, tuple._2))
    this
  }

  private def emptyAddress(): ModelNode = {
    underlying.get("address").setEmptyList()
    this
  }

  override def exec(operation: Operation): ModelNode = {
    underlying.get("operation").set(operation.name.name)
    operation.params.foreach(param => this(param._1.name) = param._2)
    this
  }
}

/**
 * Implementation for value model nodes. Contains empty implementations for [[org.jboss.dmr.scala.ModelNode#at]] and
 * [[org.jboss.dmr.scala.ModelNode#exec]] and offers simple conversions by mixing in
 * [[org.jboss.dmr.scala.ValueConversions]]
 *
 * @param javaModelNode the underlying Java `org.jboss.dmr.ModelNode`
 */
class ValueModelNode(javaModelNode: JavaModelNode) extends ModelNode(javaModelNode) with ValueConversions{

  /** Nop - returns this value model undmodified */
  def at(address: Address): ModelNode = this

  /** Nop - returns this value model undmodified */
  def exec(operation: Operation): ModelNode = this
}