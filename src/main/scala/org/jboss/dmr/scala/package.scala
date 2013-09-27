package org.jboss.dmr

/**
 * @author Harald Pehl
 */
package object scala {
  implicit def tupleToAddress(tuple: (String, String)) = new Address(List(tuple))
  implicit def symbolToOperation(name: Symbol) = new Operation(name)
  implicit def optionToModelNode(opt: Option[ModelNode]) = opt.getOrElse(ModelNode.empty())
}
