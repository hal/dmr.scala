name := "dmr-scala"

organization := "org.jboss.dmr"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.2"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

scalacOptions in (Compile, doc) += s"-doc-external-doc:${scalaInstance.value.libraryJar}#http://www.scala-lang.org/api/${scalaVersion.value}/"

resolvers ++= Seq(
  "jboss repo" at "https://repository.jboss.org/nexus/content/groups/public/"
)

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.10.2",
  "org.jboss" % "jboss-dmr" % "1.2.0.Final"
)

initialCommands += """
  import org.jboss.dmr.scala._
  import org.jboss.dmr.scala.ModelNode
"""
