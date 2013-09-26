### Reading values

```scala

// return value
val syncResult = client.execute(node @@ "/subsystem=infinispan" ! 'read_resource('recursive_depth -> 3)

// return future
val asyncResult = client.execute(node @@ "/subsystem=infinispan" ? 'read_resource('recursive_depth -> 3)

// Using apply? ...
val result = node("result")
val module = node("result", "cache-container", "web", "module")

// setter (:>)
val model = node @@ ("datasource" / "Example" # "jndiName") :> "java:/Test"


// getter (<:)
val value = model <: ("datasource" / "Example" # "jndiName") 



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
