name := "dmr-scala"

organization := "org.jboss.dmr"

version := "0.2-SNAPSHOT"

scalaVersion := "2.10.2"

resolvers ++= Seq(
  "jboss repo" at "https://repository.jboss.org/nexus/content/groups/public/"
)

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.10.2",
  "org.scalatest" % "scalatest_2.10" % "2.0.RC1" % "test",
  "org.jboss" % "jboss-dmr" % "1.2.0.Final"
)

initialCommands += """
  import org.jboss.dmr.scala._
  import org.jboss.dmr.scala.ModelNode
"""
