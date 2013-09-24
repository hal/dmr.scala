package org.jboss.dmr.scala

import scala.collection.JavaConversions._
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
   * @return this with the address set
   */
  @throws[IllegalArgumentException]("if the address is unbalanced")
  def @@(address: String): ModelNode = {
    val segments = address split "/" filter (_.nonEmpty)
    val pairs = segments.map(nameValue => {
      val parts = nameValue split "="
      if (parts.length == 2) Pair(parts(0), parts(1)) else throw new IllegalArgumentException(s"Unbalanced address '$address'")
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
   * @return this
   */
  def op(operation: Operation): ModelNode = {
    delegate.get("operation").set(operation.name.name)
    operation.params.foreach(param => this(param._1.name) = param._2)
    this
  }

  /**
   * Sets a named value in the underlying ModelNode
   * @param name the name
   * @param value the value
   */
  @throws[IllegalArgumentException]("if type of value is not supported")
  def update(name: String, value: Any) {
    value match {
      case boolean: Boolean => delegate.get(name).set(boolean)
      case int: Int => delegate.get(name).set(int)
      case long: Long => delegate.get(name).set(long)
      case bigInt: BigInt => delegate.get(name).set(bigInt.underlying())
      case float: Float => delegate.get(name).set(float)
      case double: Double => delegate.get(name).set(double)
      case bigDecimal: BigDecimal => delegate.get(name).set(bigDecimal.underlying())
      case string: String => delegate.get(name).set(string)
      // TODO handle 'unchecked since it is eliminated by erasure' warning
      // see http://stackoverflow.com/questions/1094173/how-do-i-get-around-type-erasure-on-scala-or-why-cant-i-get-the-type-paramete
      // and http://daily-scala.blogspot.de/2010/01/overcoming-type-erasure-in-matching-2.html
      case nodes: Seq[ModelNode] => delegate.get(name).set(nodes.map(_.delegate))
      // to be continued...
      case _ => throw new IllegalArgumentException(s"Illegal type ${value.getClass} for $name")
    }
  }

  override def toString = delegate.toString
}
