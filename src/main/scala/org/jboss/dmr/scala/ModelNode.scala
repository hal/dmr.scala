package org.jboss.dmr.scala

import scala.collection.JavaConversions._
import scala.reflect.runtime.universe.{Symbol => _, _}
import org.jboss.dmr.{ModelNode => JavaModelNode}

object ModelNode {
  val root = ""

  implicit def symbolToOperation(name: Symbol) = new Operation(name)

  def node(): ModelNode = new ModelNode

  def composite(n: ModelNode, xn: ModelNode*): ModelNode = {
    val comp = node() @@ "" op 'composite
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

  /**
   * Sets the address of this model node.
   * @param address the address as string e.g. "/core-service=management/access=authorization"
   * @throws if the address is unbalanced
   * @return this with the address set
   */
  @throws[IllegalArgumentException]("if the address is unbalanced")
  def @@(address: String): ModelNode = {
    val segments = address split "/" filter (_.nonEmpty)
    val pairs = segments.map(nameValue => {
      val parts = nameValue split "="
      if (parts.length == 2) Pair(parts(0), parts(1)) else throw new IllegalArgumentException(
        s"""Unbalanced address "$address".""")
    })
    @@(pairs)
  }

  /**
   * Sets the address of this model node.
   * @param address the address as pairs e.g. List(("/core-service", "management"), ("access", "authorization"))
   * @return this with the address set
   */
  def @@(address: Traversable[(String, String)]): ModelNode = {
    emptyAddress()
    address.foreach(tuple => delegate.get("address").add(tuple._1, tuple._2))
    this
  }

  /**
   * Executes the specified operation
   * @param operation the operation (symbols are implictly converted)
   * @return this with the operation set
   */
  def op(operation: Operation): ModelNode = {
    delegate.get("operation").set(operation.name.name)
    operation.params.foreach(param => this(param._1.name) = param._2)
    this
  }

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
          case ModelNode => {
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
