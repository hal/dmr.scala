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


// Chaining Options?
val module = for {
    result <- node <: 'result
    cacheContainer <- result.get("cache-container")
    web <- cacheContainer.get("web")
    module <- web.get("module")
} yield module

// Using custom operators?
val module = node ? "result" ? "cache-container" ? "web" ? "module"
```
