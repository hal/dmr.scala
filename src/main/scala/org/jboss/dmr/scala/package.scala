package org.jboss.dmr

/**
 * @author Harald Pehl
 */
package object scala {
  implicit def symbolToOperation(name: Symbol) = new Operation(name)
}
