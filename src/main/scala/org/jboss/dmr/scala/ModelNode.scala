package org.jboss.dmr.scala

import scala.Some
import scala.collection.TraversableLike
import scala.collection.JavaConversions._
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable.Builder
import scala.collection.mutable.ListBuffer
import org.jboss.dmr.{ModelNode => JavaModelNode}
import org.jboss.dmr.ModelType._
import org.jboss.dmr.scala.ModelNode.NodeTuple

/** Factory for [[org.jboss.dmr.scala.ModelNode]] */
object ModelNode {

  type NodeTuple = (String, ModelNode)

  object Undefined extends ComplexModelNode

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
  def apply(kvs: (String, Any)*): ModelNode = {
    val node = new ComplexModelNode()
    kvs.toList.foreach(kv => node(kv._1) = kv._2)
    node
  }

  /**
   * Creates a new composite containing the specified model nodes
   *
   * @param n the first model node
   * @param xn additional model nodes
   */
  def composite(n: ModelNode, xn: ModelNode*): ModelNode = {
    val node = new ComplexModelNode() exec 'composite
    node("steps") = List(n) ++ xn
    node
  }

  def fromNodeTuples(kvs: List[NodeTuple]): ModelNode = {
    println(s"in fromNodeTuples($kvs)")
    val node = new ComplexModelNode()
    kvs.toList.foreach(kv => node(kv._1) = kv._2)
    node
  }

  implicit def canBuildFrom: CanBuildFrom[ModelNode, NodeTuple, ModelNode] =
    new CanBuildFrom[ModelNode, NodeTuple, ModelNode] {
      def apply(): Builder[NodeTuple, ModelNode] = newBuilder

      def apply(from: ModelNode): Builder[NodeTuple, ModelNode] = newBuilder
    }

  def newBuilder: Builder[NodeTuple, ModelNode] = new ListBuffer mapResult fromNodeTuples


  // TODO Add an extractor which can be used with pattern matching to check the result of an DMR operation
}

/**
 * A Scala wrapper around a `org.jboss.dmr.ModelNode` offering methods to interact with model nodes in a more
 * natural way.
 *
 * @param javaModelNode the underlying Java `org.jboss.dmr.ModelNode`
 */
abstract class ModelNode(javaModelNode: JavaModelNode)
    extends Traversable[NodeTuple]
        with TraversableLike[NodeTuple, ModelNode]
//        with Builder[NodeTuple, ModelNode]
        with ValueConversions {

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

  def apply(key: String): ModelNode = {
    if (underlying.has(key)) {
      val jchild = underlying.get(key)
      if (isSimple(jchild)) new ValueModelNode(jchild) else new ComplexModelNode(jchild)
    } else defaultValue
  }

  protected def defaultValue = ModelNode.Undefined

  def get(key: String): Option[ModelNode] = find(key)

  /**
   * Returns an option for the nested model node with the specified key(s). You can specify multiple keys lookup deeply
   * nested model nodes:
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
   * val flag = node("flag")
   * val level3 = node("level0", "level1", "level2", "level3")
   * }}}
   *
   * The method is safe to call for children that does not exist in the path. In this case [[scala.None]] wil be
   * returned:
   * {{{
   * val nope = node("level0", "oops", "level2", "level3")
   * }}}
   *
   * @param key the key of the directly nested model node
   * @param childKeys the keys of
   * @return [[scala.Some]] if the nested model node exists, [[scala.None]] otherwise
   */
  def find(key: String, childKeys: String*): Option[ModelNode] = {
    if (underlying.has(key)) {
      val jchild = underlying.get(key)
      val child = if (isSimple(jchild)) new ValueModelNode(jchild) else new ComplexModelNode(jchild)
      childKeys.toList match {
        case Nil => Some(child)
        case head :: tail => child.find(head, tail: _*) // call apply on child
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
   * node += (
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
  def +=(tuples: (String, Any)*): ModelNode = {
    tuples.foreach(tuple => this(tuple._1) = tuple._2)
    this
  }

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
          // only colections of model nodes are supported!
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

  /** Delegates to `underlying.toString` */
  override def toString = underlying.toString

  /** Returns the keys for this model node */
  def keys: Iterable[String] = {
    underlying.getType match {
      case OBJECT => underlying.keys.toSet
      case _ => Set()
    }
  }

  override def foreach[U](f: (NodeTuple) => U): Unit = {
    val contents = underlying.getType match {
      case OBJECT =>
        underlying.asList().map(jnode => {
          val jvalue = jnode.asProperty().getValue
          val key = jnode.asProperty().getName
          val value = if (isSimple(jvalue)) new ValueModelNode(jvalue) else new ComplexModelNode(jvalue)
          (key, value)
        })
      case _ => Map.empty
    }
    contents.foreach(f)
  }

  override protected[this] def newBuilder: Builder[NodeTuple, ModelNode] = ModelNode.newBuilder

//  def +=(elem: NodeTuple): this.type = {
//    this(elem._1) = elem._2
//    this
//  }
//
//  def clear(): Unit = underlying.clear()
//
//  def result(): ModelNode = this
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
 * [[org.jboss.dmr.scala.ModelNode#exec]].
 *
 * @param javaModelNode the underlying Java `org.jboss.dmr.ModelNode`
 */
class ValueModelNode(javaModelNode: JavaModelNode) extends ModelNode(javaModelNode) {

  /** Nop - returns this value model undmodified */
  def at(address: Address): ModelNode = this

  /** Nop - returns this value model undmodified */
  def exec(operation: Operation): ModelNode = this
}