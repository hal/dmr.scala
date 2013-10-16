name := "dmr-scala"

organization := "org.jboss"

version := "0.2.0"

scalaVersion := "2.10.2"

scalacOptions ++= Seq(
  "-language:implicitConversions",
  "-feature",
  "-deprecation"
)

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
