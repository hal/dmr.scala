# DMR.scala

Scala client library for DMR operations. Features:

* DSL for creating DMR operations
* Interact with model nodes in a more natural way

For an introduction to DMR please refer to the [WildFly Wiki](https://docs.jboss.org/author/display/WFLY8/Detyped+management+and+the+jboss-dmr+library).

## Getting started

Please make sure to import the following packages in order to bring the necessary implicit conversions into scope and
to save you some keystrokes.

```scala
import org.jboss.dmr.scala._
import org.jboss.dmr.scala.ModelNode
```

## Creating model nodes

Use the factory methods in `org.jboss.dmr.scala.ModelNode` to create model nodes:

```scala
// creates an empty model node
ModelNode.empty

// creates a new model node with structure
ModelNode(
  "flag" -> true,
  "hello" -> "world",
  "answer" -> 42,
  "child" -> ModelNode(
    "inner-a" -> 123,
    "inner-b" -> "test",
    "deep-inside" -> ModelNode("foo" -> "bar"),
    "deep-list" -> List(
      ModelNode("one" -> 1),
      ModelNode("two" -> 2),
      ModelNode("three" -> 3)
    )
  )
)
```

## Addressing and operations

You can use a DSL like API to set the address and operation for a model node. To describe the "read-resource" operation
on "/subsystem=datasources/data-source=ExampleDS" use the following code:

```scala
ModelNode.empty at ("subsystem" -> "datasources") / ("data-source" -> "ExampleDS") exec 'read_resource(
  'include_runtime -> false,
  'recursive_depth -> 2
)
```

Addresses can be written down as `(String, String)` tuples separated by "/". Operations are specified using
`Symbol`s and an optional list of parameters. Each parameter is made up of another `Symbol` and a value. Using symbols
makes the DSL both more readable and extentable.

However there's one drawback in using symbols: they cannot contain characters like "-". `'read-resource` is therefore
an illegal symbol. As most DMR operations and many parameters do contain "-", this library will replace all
underscores in a symbol with dashes:

```scala
ModelNode.empty exec 'read_resource('include_runtime -> true)
// is exactly the same as
ModelNode.empty exec Symbol("read-resource")(Symbol("include-runtime") -> true)
```

Here are some more examples using addresses and operations:

```scala
// root is a constant for an empty address
ModelNode.empty at root exec 'read_resource
ModelNode.empty at ("subsystem" -> "datasources") / ("data-source" -> "ExampleDS") exec 'disable

// parameters are specified as pairs (Symbol -> Any)
ModelNode.empty at ("core-service" -> "platform-mbean") / ("type" -> "runtime") exec 'read_resource(
  'attributes_only -> true,
  'include_runtime -> false,
  'recursive_depth -> 3,
  'custom_parameter -> "custom-value"
)

// unsupported parameter types will throw an IllegalArgumentException
ModelNode.empty at root exec 'read_resource('proxies -> Console.out)
```

## Reading

Reading properties from a model node will result in an `Option[ModelNode]`. Choose between the "/" operator as in
`node / "foo"` (best for reading nested model nodes) or `node("foo")` (best for reading direct child nodes)

```scala
val node = ModelNode(
  "level0" -> ModelNode(
    "level1" -> ModelNode(
      "level2" -> ModelNode(
        "level3" -> ModelNode(
          "level4" -> ModelNode("foo" -> "bar")
        )
      )
    )
  )
)

val n1 = node("level0")
val n2 = node / "level0" / "level1" / "level2" / "level3"
val n3 = for {
  l0 <- node / "level0"
  l1 <- l0 / "level1"
  l2 <- l1 / "level2"
} yield l2
```

Since the methods return `Option`s, reading nested model nodes is safe even if some children in the path do not exist.
In this case `None` wil be returned:

```scala
val nope = node / "level0" / "oops" / "level2" / "level3"
```

## Writing

Simple values can be set using `node("foo") = "bar"`. If "foo" doesn't exist it will be created and updated
otherwise. As an alternative you can use the `<<` operator, which comes in handy if you want to add multiple
key / value pairs:

```scala
val node = ModelNode.empty

node("foo") = "bar"
node << ("foo" -> "bar")

node << (
  "flag" -> true,
  "hello" -> "world",
  "answer" -> 42,
  "child" -> ModelNode(
    "inner-a" -> 123,
    "inner-b" -> "test",
    "deep-inside" -> ModelNode(
      "foo" -> "bar"
    ),
    "deep-list" -> List(
      ModelNode("one" -> 1),
      ModelNode("two" -> 2),
      ModelNode("three" -> 3)
    )
  )
)
```

Reading and writing can also be combined in one call:

```scala
(node / "child" / "deep-inside") << ("foo" -> "xyz")
```

## Composites

A composite operation is setup using the `ModelNode.composite(n: ModelNode, xn: ModelNode*)` factory method:

```scala
ModelNode.composite(
  ModelNode.empty at ("core-service" -> "management") / ("access" -> "authorization") exec 'read_resource(
    'recursive_depth -> 2),
  ModelNode.empty at ("core-service" -> "management") / ("access" -> "authorization") exec 'read_children_names(
    'name -> "role-mapping"),
  ModelNode.empty at ("subsystem" -> "mail") / ("mail-session" -> "*") exec 'read_resource_description,
  ModelNode.empty at ("subsystem" -> "datasources") / ("data-source" -> "ExampleDS") exec 'disable ,
  ModelNode.empty at ("core-service" -> "platform-mbean") / ("type" -> "runtime") exec 'read_attribute(
    'name -> "start-time")
)
```

## Execute an operation

Once you have setup a model node you can execute it using [DMR.repl](https://github.com/heiko-braun/dmr-repl) like
this:

```scala
val client = connect()
val rootResource = node at root exec 'read_resource
val result = client.execute(rootResource.underlying)
```
