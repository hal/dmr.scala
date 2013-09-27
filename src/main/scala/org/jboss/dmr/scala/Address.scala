package org.jboss.dmr.scala

class Address(val tuples: List[(String, String)]) {
  def /(address: Address) = new Address(tuples ++ address.tuples)
  override def toString = tuples.toString()
}
