name := "dmr.scala"

version := "1.0.0"

scalaVersion := "2.10.2"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

resolvers ++= Seq(
  "jboss repo" at "https://repository.jboss.org/nexus/content/groups/public/"
)

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.10.2",
  "org.jboss" % "jboss-dmr" % "1.2.0.Final"
)

initialCommands += """
  import org.jboss.dmr.scala.ModelNode._
  import org.jboss.dmr.scala.Operation._
  import org.jboss.dmr.scala.Operation.Predefs._
"""