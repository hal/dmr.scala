package org.jboss.dmr.scala

/**
 * @author Harald Pehl
 */
trait SimpleConversions extends ModelNode {
  def asBoolean() = underlying.asBoolean()

  def asInt() = underlying.asInt()

  def asLong() = underlying.asLong()

  def asBigInt() = underlying.asBigInteger()

  def asDouble() = underlying.asDouble()

  def asString() = underlying.asString()
}
