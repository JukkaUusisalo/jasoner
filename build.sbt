name := "template-api-rest-java-playframework"

version := "1.1"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.codehaus.jackson" % "jackson-mapper-asl" % "1.8.5",
  "com.github.spullara.mustache.java" % "compiler" %  "0.9.2"
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
