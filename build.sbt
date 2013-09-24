name := "dmr.scala"

version := "1.0.0"

scalaVersion := "2.10.2"

resolvers ++= Seq(
  "jboss repo" at "https://repository.jboss.org/nexus/content/groups/public/"
)

libraryDependencies ++= Seq(
  "org.jboss" % "jboss-dmr" % "1.2.0.Final",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test"
)

initialCommands += """
  import org.jboss.dmr.scala.ModelNode._
  import org.jboss.dmr.scala.Operation._
  import org.jboss.dmr.scala.Predefs._
"""