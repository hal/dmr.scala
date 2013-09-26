### Reading values

```scala

// return value
val syncResult = client ! (node at "/subsystem=infinispan" exec 'read_resource('recursive_depth -> 3)

// return future
val asyncResult = client ? (node at "/subsystem=infinispan" exec 'read_resource('recursive_depth -> 3)

// complex getter & setter aka traversal
val c = ModelNode.createComposite(...) // 3 steps

// complex type traversal (immutable)
val step1 = c / "result" / "step1" 

// simple type getter / setter
val poolSize = step1("poolSize").asInt
step1("poolSize") =  10

// setting complex types
val t = ModelNode.create
t / "child" / "grandchild" += ("foo" -> anotherModelNode)

// property model nodes

val subsystemNames = ModelNode.create at ("foo" -> "bar") exec 'read_children_names('child_type->"subsystem")


// Chaining Options
val response = ...
val module = for {
    cacheContainer <- response / "result" / "cache-container"
    module <- cacheContainer / "web" / "module"
} yield module



val t = ModelNode.create

t += "child" -> ModelNode.create {
    "foo" -> 1,
    "bar" -> 2
}




```
