# DMR.scala

Scala client library for DMR operations. Currently a DSL for creating DMR operations is supported.
Gettting values out of a ModelNode will follow soon...

## Usage

For the following examples to work please import `org.jboss.dmr.scala.ModelNode._` and `org.jboss.dmr.scala.Predefs._`

### Read the root node

```scala
node @@ "" op read_resource
```

### Operation with parameters

```scala
node @@ "/core-service=management/access=authorization" op read_children_names(attributes_only -> true)
```

### Composite operation

```scala
composite (
    node @@ "/core-service=management/access=authorization" op read_resource(recursive_depth -> 2),
    node @@ "/core-service=management/access=authorization" op read_children_names('name -> "role-mapping"),
    node @@ "/subsystem=mail/mail-session=*" op read_resource_description,
    node @@ "subsystem=datasources/data-source=ExampleDS" op 'disable,
    node @@ "/core-service=platform-mbean/type=runtime" op read_attribute('name -> "start-time")
)
```



## Ideas for executing and reading model nodes

```scala
val node = client.execute(node @@ "/subsystem=infinispan" op read_resource(recursive_depth -> 3)

// Evaluate using apply
val result = node("result")
val module = node("result", "cache-container", "web", "module")

// Chaining Options
val module = for {
    result <- node.get("result")
    cacheContainer <- result.get("cache-container")
    web <- cacheContainer.get("web")
    module <- web.get("module")
} yield module

// Using custom operators, maybe this could be melt down to
val module = node ? "result" ? "cache-container" ? "web" ? "module"
```
