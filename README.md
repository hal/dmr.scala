# DMR.scala

Scala client library for DMR operations. Offers a DSL for creating DMR operations.

## Usage

Start by using one of the factory methods in `org.jboss.dmr.scala.ModelNode`:

- `node()`: creates an empty model node
- `composite(n: ModelNode, xn: ModelNode*)` creates a composite containing the given model nodes as steps

Please make sure to import `org.jboss.dmr.scala.ModelNode._` and `org.jboss.dmr.scala.Operation.Predefs._` in order
to take full advantage of the DSL.

### Address

Use the `@@` operator to specify the address of the model node. The address can be specified as string or as
`Traversable[(String, String)]`

```scala
// address as string
node @@ "/subsystem=datasources/data-source=ExampleDS"

// address as list of pairs
node @@ (("subsystem" -> "datasources") :: ("data-source" -> "ExampleDS") :: Nil)

// an unbalanced address will throw an IllegalArgumentException
node @@ "/subsystem=datasources/data-source="
```

### Operations

Operations are specified using the method `op(operation: Operation)`. An `Operation` is thereby defined by a
`Symbol` and optional parameters. Each parameter is made up of another `Symbol` and a value. Using symbols
makes the DSL both more readable and extentable. There's an implicit conversion from `Symbol` to `Operator`, so
you don't have to use `new Operator('foo)` all over the place.

However there's one drawback in using symbols: they cannot contain characters like "-". `'read-resource` is therefore
an illegal symbol. To use such a symbol you have to use the factory method `Symbol("read-resource")`. For the most
common operations and parameters there are predefined symbols in `org.jboss.dmr.scala.Operation.Predefs`

```scala
// root is just a constant for ""
// read_resource is predefined in org.jboss.dmr.scala.Operation.Predefs
node @@ root op read_resource

// use own symbols as you need
node @@ "/subsystem=datasources/data-source=ExampleDS" op 'disable

// but be sure to use the factory method for operations containing "-"
node @@ "/foo=bar" op Symbol("read-resource-metrics")

// parameters can be specified as pairs (Symbol -> Any)
node @@ "/core-service=platform-mbean/type=runtime" op read_resource(
  attributes_only -> true,
  include_runtime -> false,
  recursive_depth -> 3,
  Symbol("custom-parameter") -> "custom-value")

// unsupported parameter values will throw an IllegalArgumentException
node @@ root op read_resource('proxies -> Conole.out)
```

### Composites

A composite is setup using the `composite(n: ModelNode, xn: ModelNode*)` factory method:

```scala
composite (
  node @@ "/core-service=management/access=authorization" op read_resource(
    recursive_depth -> 2),
  node @@ "/core-service=management/access=authorization" op read_children_names(
    'name -> "role-mapping"),
  node @@ "/subsystem=mail/mail-session=*" op read_resource_description,
  node @@ "/subsystem=datasources/data-source=ExampleDS" op 'disable,
  node @@ "/core-service=platform-mbean/type=runtime" op read_attribute(
    'name -> "start-time")
)
```

## Execute an operation

Once you have setup a model node you can execute it using [DMR.repl](https://github.com/heiko-braun/dmr-repl) like
this:

```scala
val client = connect()
val rootResource = node @@ root op read_resource
val result = client.execute(rootResource.underlying)
```