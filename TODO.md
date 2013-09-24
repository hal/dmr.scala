### Reading values

```scala
val node = client.execute(node @@ "/subsystem=infinispan" op read_resource(recursive_depth -> 3)

// Using apply?
val result = node("result")
val module = node("result", "cache-container", "web", "module")

// Chaining Options?
val module = for {
    result <- node.get("result")
    cacheContainer <- result.get("cache-container")
    web <- cacheContainer.get("web")
    module <- web.get("module")
} yield module

// Using custom operators?
val module = node ? "result" ? "cache-container" ? "web" ? "module"
```
