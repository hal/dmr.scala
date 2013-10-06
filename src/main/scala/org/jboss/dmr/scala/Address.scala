package org.jboss.dmr.scala

/** Helper class for holding the address tuples for a model node */
case class Address(tuples: List[(String, String)]) {
  def /(address: Address) = Address(tuples ++ address.tuples)
}
