package org.jboss.dmr.scala

/**
 * @author Harald Pehl
 */
object Address {
  implicit def tuple2ToAddress(tuple : (String, String)) = new Address(List(tuple))
}

class Address(val tuples: List[(String, String)]) {
}
