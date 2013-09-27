package org.jboss.dmr.scala

/**
 * Class for holding the address tuples for a model node
 * @param tuples the address tuples
 */
case class Address(tuples: List[(String, String)]) {
  def /(address: Address) = new Address(tuples ++ address.tuples)
}
