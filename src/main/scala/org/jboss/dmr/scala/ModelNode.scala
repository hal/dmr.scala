package org.jboss.dmr.scala

import scala.Some
import scala.collection.TraversableLike
import scala.collection.JavaConversions._
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable.{Builder, ListBuffer}

import org.jboss.dmr.{ModelNode => JavaModelNode}
import org.jboss.dmr.ModelType._
import org.jboss.dmr.scala.ModelNode.NodeTuple

/** Factory for [[org.jboss.dmr.scala.ModelNode]] */
object ModelNode {

  type NodeTuple = (String, ModelNode)

  object Undefined extends ComplexModelNode

  /** Creates a new model node holding the given value */
  def apply(value: AnyVal): ModelNode = {
    val jvalue = value match {
      case boolean: Boolean => new JavaModelNode(boolean)
      case int: Int => new JavaModelNode(int)
      case long: Long => new JavaModelNode(long)
      case float: Float => new JavaModelNode(float)
      case double: Double => new JavaModelNode(double)
      case _ => new JavaModelNode()
    }
    new ValueModelNode(jvalue)
  }

  /** Creates a new model node holding the given string */
  def apply(value: String): ModelNode = new ValueModelNode(new JavaModelNode(value))

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
  def apply(kvs: (String, Any)*): ModelNode = new ComplexModelNode() += (kvs: _*)

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

  implicit def canBuildFrom: CanBuildFrom[ModelNode, NodeTuple, ModelNode] =
    new CanBuildFrom[ModelNode, NodeTuple, ModelNode] {
      def apply(): Builder[NodeTuple, ModelNode] = newBuilder

      def apply(from: ModelNode): Builder[NodeTuple, ModelNode] = newBuilder
    }

  def newBuilder: Builder[NodeTuple, ModelNode] = new ListBuffer().mapResult(kvs => new ComplexModelNode() += (kvs: _*))

  // TODO Add an extractor which can be used with pattern matching to check the result of a DMR operation
}

/**
 * A Scala wrapper around a `org.jboss.dmr.ModelNode` offering methods to interact with model nodes in a more
 * natural way.
 *
 * This class uses some of the semantics and methods of [[scala.collection.Map]] while mixing
 * in [[scala.collection.TraversableLike]] which turns it into a collection of [[(String, ModelNode)]] tuples.
 *
 * @param javaModelNode the underlying Java `org.jboss.dmr.ModelNode`
 */
abstract class ModelNode(javaModelNode: JavaModelNode) extends
    TraversableLike[NodeTuple, ModelNode]
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
   * Sets the operation for this model node. The operation can be specified as a [[scala.Symbol]] which will
   * be implicitly converted to an operation.
   *
   * @param operation the operation.
   * @return this model node with the operation set
   */
  def exec(operation: Operation): ModelNode

  /**
   * Returns the model node associated with a path, or throws a [[java.util.NoSuchElementException]] if the path is
   * not contained in the model node.
   */
  def apply(path: Path): ModelNode = getOrElse(path, throw new NoSuchElementException)

  /** Optionally returns the value associated with a path. */
  def get(path: Path): Option[ModelNode] = {
    if (underlying.has(path.elements.head)) {
      val jchild = underlying.get(path.elements.head)
      val child = if (isSimple(jchild)) new ValueModelNode(jchild) else new ComplexModelNode(jchild)
      path.elements.tail match {
        case Nil => Some(child)
        case _ => child.get(Path(path.elements.tail))
      }
    } else None
  }

  /**
   * Returns the model node associated with a path, or a default value if the path is not contained in the
   * model node.
   *
   * @param path the path
   * @param default a computation that yields a default value in case no binding for `path` is found in the model node.
   * @return the value associated with `path` if it exists, otherwise the result of the `default` computation.
   */
  def getOrElse(path: Path, default: => ModelNode): ModelNode = get(path) match {
    case Some(node) => node
    case None => default
  }

  /** Tests whether this model node contains a binding for a path. */
  def contains(path: Path): Boolean = get(path) match {
    case Some(x) => true
    case None => false
  }

  protected def isSimple(jnode: JavaModelNode) = jnode.getType match {
    case BIG_DECIMAL | BIG_INTEGER | BOOLEAN | BYTES | DOUBLE | INT | LONG | STRING => true
    case _ => false
  }

  /**
   * Adds multiple key / value pairs to this model node:
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
   * @param kvs the key / value pairs
   */
  def +=(kvs: (String, Any)*): ModelNode = {
    kvs.foreach(tuple => this(tuple._1) = tuple._2)
    this
  }

  /**
   * Adds a given key/value pair. Supports the following types:
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
   * @throws IllegalArgumentException if the type is not supported
   */
  @throws[IllegalArgumentException]("if the type is not supported")
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
  override def toString() = underlying.toString

  /** Returns the keys for this model node */
  def keys: Iterable[String] = map(_._1).toIterable

  /** Returns the values for this model node */
  def values: Iterable[ModelNode] = map(_._2).toIterable

  override def foreach[U](f: (NodeTuple) => U): Unit = contents.foreach(f)

  override def seq: TraversableOnce[NodeTuple] = contents

  override protected[this] def newBuilder: Builder[NodeTuple, ModelNode] = ModelNode.newBuilder

  private def contents: List[NodeTuple] = underlying.getType match {
    case OBJECT =>
      underlying.asList().map(jnode => {
        val jvalue = jnode.asProperty().getValue
        val key = jnode.asProperty().getName
        val value = if (isSimple(jvalue)) new ValueModelNode(jvalue) else new ComplexModelNode(jvalue)
        (key, value)
      }).toList
    case _ => List.empty
  }
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

  /** Safe nop - returns this value model undmodified */
  def at(address: Address): ModelNode = this

  /** Safe nop - returns this value model undmodified */
  def exec(operation: Operation): ModelNode = this
}