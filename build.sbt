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
  "org.scalatest" % "scalatest_2.10" % "2.0.RC3" % "test",
  "org.jboss" % "jboss-dmr" % "1.2.0.Final"
)

initialCommands += """
  import org.jboss.dmr.scala._
  import org.jboss.dmr.scala.ModelNode
            """

publishMavenStyle := true

pomExtra :=
  <licenses>
    <license>
      <name>lgpl</name>
      <url>http://repository.jboss.com/licenses/lgpl.txt</url>
    </license>
  </licenses>
  <url>
    https://github.com/hal/dmr.scala
  </url>

publishTo <<= version { (v: String) =>
  val nexus = "https://repository.jboss.org/nexus/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

