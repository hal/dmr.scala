package org.jboss.dmr.scala

import scala.collection.JavaConversions._
import scala.reflect.runtime.universe.{Symbol => _, _}
import org.jboss.dmr.{ModelNode => JavaModelNode}

object ModelNode {

  /**
   * @return an empty model node
   */
  def empty() = new ModelNode()

  /**
   * @return a new model node containing an empty address
   */
  def root(): ModelNode = new ModelNode().emptyAddress()

  /**
   *
   * @param tuples the key value pairs
   * @tparam T the values type
   * @return a new model node with the specified key value pairs
   */
  def apply[T: TypeTag](tuples: (String, T)*): ModelNode = {
    val node = empty()
    tuples.toList.foreach(tuple => node(tuple._1) = tuple._2)
    node
  }

  /**
   * @param n the first model node
   * @param xn additional model nodes
   * @return a new composite model node containing the specified model nodes
   */
  def composite(n: ModelNode, xn: ModelNode*): ModelNode = {
    val node = empty exec 'composite
    node("steps") = List(n) ++ xn
    node
  }
}

class ModelNode(delegate: JavaModelNode = new JavaModelNode()) {

  /**
   * @return the underlying Java ModelNode
   */
  def underlying = delegate

  /**
   * Sets the address for this model node
   * @param address the address
   * @return this model node with the address set
   */
  def at(address: Address): ModelNode = {
    emptyAddress()
    address.tuples.foreach(tuple => underlying.get("address").add(tuple._1, tuple._2))
    this
  }

  private def emptyAddress(): ModelNode = {
    underlying.get("address").setEmptyList()
    this
  }

  /**
   * Sets the specified operation for this model node
   * @param operation the operation (symbols are implictly converted)
   * @return this model node with the operation set
   */
  def exec(operation: Operation): ModelNode = {
    underlying.get("operation").set(operation.name.name)
    operation.params.foreach(param => this(param._1.name) = param._2)
    this
  }

  /**
   * Finds the specified nested model node
   * @param name the name of the model node
   * @return an option for the specified model node
   */
  def /(name: String): Option[ModelNode] = {
    if (underlying.has(name)) Some(new ModelNode(underlying.get(name))) else None
  }

  /**
   * Finds the specified property
   * @param name the name of the property
   * @return an option for the specified simple property
   */
  def apply(name: String): Option[ModelNode] = {
    val javaNode = underlying.get(name)
    if (javaNode.isDefined) Some(new ModelNode(javaNode)) else None
  }

  /**
   * Sets multiple key value pairs
   * @param tuples the key value pairs
   * @tparam T the values type
   */
  def <<[T: TypeTag](tuples: (String, T)*) = tuples.foreach(tuple => this(tuple._1) = tuple._2)

  /**
   * Sets the specified property to the given value
   * @param name the properties name
   * @param value the value
   * @tparam T the value type
   * @throws IllegalArgumentException if the value type is not supported
   */
  @throws[IllegalArgumentException]("if the value type is not supported")
  def update[T: TypeTag](name: String, value: T) {
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
        val targs = typeOf[T] match { case TypeRef(_, _, args) => args }
        if (targs.isEmpty) {
          // coming from the << operator with multiple tuples
          try {
            val nodes = values.asInstanceOf[Traversable[ModelNode]]
            val javaNodes = nodes.toList.map(_.underlying)
            delegate.get(name).set(javaNodes)
          } catch {
            case e: ClassCastException => throw new IllegalArgumentException(
              s"""Illegal type parameter in type ${value.getClass.getName} for "$name".""")
          }
        } else {
          // one tuple: type argument is recognized
          targs(0) match {
            case innerNode if innerNode =:= typeOf[ModelNode] => {
              val javaNodes = values.toList.map(_.asInstanceOf[ModelNode].underlying)
              delegate.get(name).set(javaNodes)
            }
            case _ => throw new IllegalArgumentException(
              s"""Illegal type parameter ${targs(0)} in type ${value.getClass.getName} for "$name".""")
          }
        }
      }
      case _ => throw new IllegalArgumentException( s"""Illegal type ${value.getClass.getName} for "$name".""")
    }
  }

  override def toString = underlying.toString
}
