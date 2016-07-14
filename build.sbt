name := """ssmgr"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  "com.typesafe.play" %% "anorm" % "2.4.0",
  cache,
  ws,
  "mysql" % "mysql-connector-java" % "latest.integration",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

val poiVersion = "latest.integration"

libraryDependencies += "org.apache.poi" % "poi" % poiVersion
libraryDependencies += "org.apache.poi" % "poi-ooxml" % poiVersion

libraryDependencies += evolutions

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"


fork in run := true