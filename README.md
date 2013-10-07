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
val node = ModelNode()

// creates a new model node holding a simple value
val node = ModelNode(42)

// creates a new model node with structure
val node = ModelNode(
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
    ),
    "value-list" -> List(
      ModelNode(1),
      ModelNode(2),
      ModelNode(3)
    )
  )
)
```

## Addressing and operations

You can use a DSL like API to set the address and operation for a model node. To describe the "read-resource" operation
on "/subsystem=datasources/data-source=ExampleDS" use the following code:

```scala
ModelNode() at ("subsystem" -> "datasources") / ("data-source" -> "ExampleDS") exec 'read_resource(
  'include_runtime -> false,
  'recursive_depth -> 2
)
```

Addresses can be written down as `(String, String)` tuples separated by "/". Operations are specified using
`Symbol`s and an optional list of parameters. Each parameter is made up of another `Symbol` and a value. Using symbols
makes the DSL both more readable and extentable.

However there's one drawback in using symbols: when using the short form `'someSymbol` characters like "-" are not allowed. `'read-resource` is therefore an illegal symbol. As most DMR operations and many parameters do contain "-", all
underscores will be replaced with dashes:

```scala
ModelNode() exec 'read_resource('include_runtime -> true)
// is exactly the same as
ModelNode() exec Symbol("read-resource")(Symbol("include-runtime") -> true)
```

Here are some more examples using addresses and operations:

```scala
// root is a constant for an empty address
ModelNode() at root exec 'read_resource
ModelNode() at ("subsystem" -> "datasources") / ("data-source" -> "ExampleDS") exec 'disable

// parameters are specified as pairs (Symbol -> Any)
ModelNode() at ("core-service" -> "platform-mbean") / ("type" -> "runtime") exec 'read_resource(
  'attributes_only -> true,
  'include_runtime -> false,
  'recursive_depth -> 3,
  'custom_parameter -> "custom-value"
)

// unsupported parameter types will throw an IllegalArgumentException
ModelNode() at root exec 'read_resource('proxies -> Console.out)
```

## Reading Nodes

Reading values from a model node follows the sementics of a `Map[String, ModelNode]`, but instead of a string you have to 
provide a `Path` as key. Thanks to an implicit conversion expressions like `"a" / "b" / "c"` are automatically converted
to a path.

```scala
val node = ModelNode(
  "flag" -> true,
  "hello" -> "world",
  "answer" -> 42,
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

val flag = node("flag")
val boom = node("gag") // throws a NoSuchelementException
val hello = node.get("hello") // returns an Option[ModelNode]
val x = node.getOrElse("nope", ModelNode("y"))
val check = node.contains("level0" / "level1" / "level2" / "level3")
val level3 = node("level0" / "level1" / "level2" / "level3")
val level2 = for {
  l0 <- node.get("level0")
  l1 <- l0.get("level1")
  l2 <- l1.get("level2")
} yield l2
```

Since the result of `node.get("a" / "b" / "c")` is `Option[ModelNode]`, reading nested model nodes is safe even if
some children in the path do not exist. In this case `None` wil be returned:

```scala
val nope = node.get("level0", "oops", "level2", "level3")
```

## Reading Values

You can use the folowing methods to read values from model nodes:

- `ModelNode.asBoolean`
- `ModelNode.asInt`
- `ModelNode.asLong`
- `ModelNode.asBigInt`
- `ModelNode.asDouble`
- `ModelNode.asString`

These methods return `Option` instances of the relevant type. This is because not all conversions make sense on all kind of
model nodes:

```scala
val node = ModelNode(
  "flag" -> true,
  "child" -> ModelNode(
    "size" -> 0
  )
)

val nonsense = node("child").asDouble // None
```

## Writing

Simple values can be set using `node("foo") = "bar"`. If "foo" doesn't exist it will be created and updated
otherwise. As an alternative you can use the `+=` operator, which comes in handy if you want to add multiple
key / value pairs:

```scala
val node = ModelNode()

node("foo") = "bar"
node += ("foo" -> "bar")

node += (
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
node("child" / "deep-inside") += ("foo" -> "xyz")
```

## Collection Operations

Since `ModelNode` mixes in `Traversable[(String, ModelNode)]` you can use all those nifty collection methods like
`foreach`, `map` or `filter`, ...

```scala
val node = ModelNode(
  "flag" -> true,
  "hello" -> "world",
  "answer" -> 42
)

// turn all keys to upper case
val shout = node.map(kv => kv._1.toUpperCase -> kv._2)

// filter for nodes
val aa = node.filter(_._1 contains "a")
val n42 = node.filter(_._2 == ModelNode(42))

// combine nodes
val node2 = node ++ ModelNode("abc" -> 1)
```

If you want to read only the leaf model nodes from a deeply nested model node, use `shallow()`:
```scala
val node = ModelNode(
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

`node.shallow()` will return this node:
```scala
org.jboss.dmr.scala.ModelNode =
{
  "flag" => true,
  "hello" => "world",
  "answer" => 42,
  "inner-a" => 123,
  "inner-b" => "test",
  "foo" => "bar",
  "one" => 1,
  "two" => 2,
  "three" => 3
}
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
