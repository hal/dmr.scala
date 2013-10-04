import org.jboss.dmr.scala._
import org.jboss.dmr.scala.ModelNode


// DSL
val dropOp = ModelNode() at ("subsystem" -> "ee") / ("foo" -> "bar") exec 'drop(
  'param_1 -> true,
  'param_2 -> "doit"
)


// new model node using apply in companion object
val n1 = ModelNode(
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


// set simple property using update()
val n2 = ModelNode()
n2("simple") = "0815"


// set multiple properties using << operator
n2 += (
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


// << operator with one tuple is the same as update()
n2 += ("simple" -> 123)
n2("simple") = 123

n2 += ("another-child2" -> ModelNode("wom" -> "bat"))
n2("another-child2") = ModelNode("wom" -> "bat")

n2 += ("another-list" -> List(ModelNode("foo" -> "bar"), ModelNode("aaa" -> 3)))
n2("another-list") = List(ModelNode("foo" -> "bar"), ModelNode("aaa" -> 3))


// traverse (result is Some[ModelNode])
val child1 = n2("child")
val child2 = for (res <- n2.get("child")) yield res
val deepInside = for {
  res1 <- n2.get("child")
  res2 <- res1.get("deep-inside")
} yield res2


// traverse using for and None[ModelNode]
val nothing1 = n2.find("child", "gibts-doch-gar-net")
val nothing2 = for (res <- n2.find("child", "gibts-doch-gar-net")) yield res
val nothing3 = for {
  res1 <- n2.get("gibts-doch")
  res2 <- res1.get("gar-net")
} yield res2


// keys and values
n2.keys.foreach(println)
n2.foreach(tuple => println(s"Key ${tuple._1}: ${tuple._2}"))


// composite
val c = ModelNode.composite(n1, n2)
