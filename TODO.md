### Reading values

```scala

// return value
val syncResult = client.execute(node @@ "/subsystem=infinispan" ! 'read_resource('recursive_depth -> 3)

// return future
val asyncResult = client.execute(node @@ "/subsystem=infinispan" ? 'read_resource('recursive_depth -> 3)


// node with address
val n = node @@ "subsystem" -> "datasources" / "datasource" -> "ExampleDS"

val pool = n("pool" / "default")
pool("max-size") = 50


// complex getter & setter aka traversal
val c = composite(...) // 3 steps

// complex type traversal (immutable)
val step1 = c / "result" / "step1" 

// simple type getter / setter
val poolSize = step1("poolSize").asInt
step1("poolSize") = 10

// setting complex types
val t = node @@ root
t / "child" / "grandchild" += ("foo" -> anotherModelNode)

// property model nodes

val subsystemNames = node @@ root ! 'read_children_names('child_type->"subsystem")


// Chaining Options
val module = for {
    cacheContainer <- node("result" / "cache-container")
    module <- cacheContainer("web" / "module")
} yield module


// map & flatMap

val comp = composite(...)

val filtered = comp.flatMap(childName => childName.eq("foo") : childName)




```
