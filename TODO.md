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

// version 1
val jndiName = c("result" / "step1" / "jndiName") 

// version 2
val step1 = c("result" / "step1") 
val poolSize = step1("poolSize")
val poolSizeValue = poolSize.asInt


// property model nodes

val subsystemNames = node @@ root ! 'read_children_names('child_type->"subsystem")


// Chaining Options
val module = for {
    cacheContainer <- node("result" / "cache-container")
    module <- cacheContainer("web" / "module")
} yield module

```
