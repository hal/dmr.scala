package org.jboss.dmr

/**
 * Provides classes for dealing with the management model of a WildFly instance.
 *
 * ==Overview==
 * TODO
 *
 * ==Implicit Conversions==
 * TODO
 */
package object scala {
  /** An empty address */
  val root = new Address(List())

  implicit def tupleToAddress(tuple: (String, String)) = new Address(List(tuple))

  implicit def symbolToOperation(name: Symbol) = new Operation(name)

  implicit def optionToModelNode(opt: Option[ModelNode]) = opt.getOrElse(ModelNode.empty())
}
