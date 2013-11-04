package org.jboss.dmr.scala

import scala.collection.JavaConversions._

/** Provides methods to convert a model node to a simple value */
trait ValueConversions {
  this: ModelNode =>

  /** Returns the value of this model node as `Some(Boolean)` or `None` if the underlying node does not support conversion to `Boolean` */
  def asBoolean: Option[Boolean] = safeAs(underlying.asBoolean)

  /** Returns the value of this model node as `Some(Int)` or `None` if the underlying node does not support conversion to `Int` */
  def asInt: Option[Int] = safeAs(underlying.asInt)

  /** Returns the value of this model node as `Some(Long)` or `None` if the underlying node does not support conversion to `Long` */
  def asLong: Option[Long] = safeAs(underlying.asLong)

  /** Returns the value of this model node as `Some(BigInt)` or `None` if the underlying node does not support conversion to `BigInt` */
  def asBigInt: Option[BigInt] = safeAs(underlying.asBigInteger)

  /** Returns the value of this model node as `Some(Double)` or `None` if the underlying node does not support conversion to `Double` */
  def asDouble: Option[Double] = safeAs(underlying.asDouble)

  /** Returns the value of this model node as `Some(String)` or `None` if the underlying node does not support conversion to `String` */
  def asString: Option[String] = safeAs(underlying.asString)

  /** Returns the value of this model node as `Some(List[ModelNode])` or `None` if the underlying node does not support conversion to `List[ModelNode]` */
  def asList: Option[List[ModelNode]] = {
    safeAs(underlying.asList).map(jnodes => jnodes.map(jnode => this.fromJavaNode(jnode)).toList)
  }

  private def safeAs[T](asMethod: () => T) = try {
    Some(asMethod())
  } catch {
    case e: IllegalArgumentException => None
  }
}
