package org.jboss.dmr.scala

import scala.collection.JavaConversions._
import scala.reflect.runtime.universe.{Symbol => _, _}
import org.jboss.dmr.{ModelNode => JavaModelNode}

object ModelNode {
  def root(): ModelNode = new ModelNode().emptyAddress()

  def create(): ModelNode = new ModelNode

  def createComposite(n: ModelNode, xn: ModelNode*): ModelNode = {
    val comp = root() exec 'composite
    comp("steps") = Seq(n) ++ xn
    comp
  }
}

class ModelNode {
  private val delegate: JavaModelNode = new JavaModelNode

  /**
   * @return the underlying Java ModelNode
   */
  def underlying = delegate

  def emptyAddress(): ModelNode = {
    delegate.get("address").setEmptyList()
    this
  }

  def at(address: (String, String)): ModelNode = {
    emptyAddress()
    delegate.get("address").add(address._1, address._2)
    this
  }

  def / (address: (String, String)) : ModelNode = {
    if(!delegate.get("address").isDefined()) emptyAddress()
    delegate.get("address").add(address._1, address._2)
    this
  }

  /**
   * Executes the specified operation
   * @param operation the operation (symbols are implictly converted)
   * @return this with the operation set
   */
  def exec (operation: Operation): ModelNode = {
    delegate.get("operation").set(operation.name.name)
    operation.params.foreach(param => this(param._1.name) = param._2)
    this
  }

  // TODO def ? for async execution

  /**
   * Sets the specified property to the given value
   * @param name the properties name
   * @param value the value
   * @tparam T the value type
   * @throws if the value type is not supported
   */
  @throws[IllegalArgumentException]("if the value type is not supported")
  def update[T: TypeTag](name: String, value: T) {
    value match {
      case boolean: Boolean => delegate.get(name).set(boolean)
      case int: Int => delegate.get(name).set(int)
      case long: Long => delegate.get(name).set(long)
      case bigInt: BigInt => delegate.get(name).set(bigInt.underlying())
      case float: Float => delegate.get(name).set(float)
      case double: Double => delegate.get(name).set(double)
      case bigDecimal: BigDecimal => delegate.get(name).set(bigDecimal.underlying())
      case string: String => delegate.get(name).set(string)
      case values: Traversable[_] => {
        val targs = typeOf[T] match { case TypeRef(_, _, args) => args }
        targs(0) match {
          case _: ModelNode => {
            val nodes = values.toList.map(_.asInstanceOf[ModelNode].delegate)
            delegate.get(name).set(nodes)
          }
          case _ => throw new IllegalArgumentException(
            s"""Illegal type parameter ${targs(0)} in type ${value.getClass.getName} for "$name".""")
        }
      }
      case _ => throw new IllegalArgumentException(s"""Illegal type ${value.getClass.getName} for "$name".""")
    }
  }

  override def toString = delegate.toString
}
