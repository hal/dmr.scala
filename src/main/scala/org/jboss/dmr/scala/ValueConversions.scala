package org.jboss.dmr.scala

/** Conversions for [[org.jboss.dmr.scala.ValueModelNode]]s */
trait ValueConversions extends ModelNode {
  def asBoolean() = underlying.asBoolean()

  def asInt() = underlying.asInt()

  def asLong() = underlying.asLong()

  def asBigInt() = underlying.asBigInteger()

  def asDouble() = underlying.asDouble()

  def asString() = underlying.asString()
}
