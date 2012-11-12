/* basic project info */
name := "fibs"

organization := "name.kaeding"

version := "0.1.0-SNAPSHOT"

description := "Scala wrapper for Interactive Brokers TWS API, using a Scalaz-inspired functional approach"

homepage := Some(url("http://kaeding.name"))

startYear := Some(2012)

licenses := Seq(
  ("BSD New", url("http://opensource.org/licenses/BSD-3-Clause"))
)

scmInfo := Some(
  ScmInfo(
    url("http://kaeding.name"),
    "scm:git:git.kaeding.name:fibs.git",
    Some("scm:git:git@git.kaeding.name:fibs.git")
  )
)

// organizationName := "My Company"

/* scala versions and options */
scalaVersion := "2.9.2"

// crossScalaVersions := Seq("2.9.1")

offline := false

scalacOptions ++= Seq("-deprecation", "-unchecked")

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

/* entry point */
mainClass in (Compile, packageBin) := Some("name.kaeding.fibs.Main")

mainClass in (Compile, run) := Some("name.kaeding.fibs.Main")

/* dependencies */
libraryDependencies ++= Seq (
  "org.scalaz" %% "scalaz-core" % "7.0.0-M4",
  "org.scalaz" %% "scalaz-concurrent" % "7.0.0-M4"
  // "org.scalaz" %% "scalaz-effect" % "7.0.0-M3",
  // "org.scalacheck" %% "scalacheck" % "1.10.0" % "test"
)

/* you may need these repos */
resolvers ++= Seq(
  // Resolvers.sonatypeRepo("snapshots")
  // Resolvers.typesafeIvyRepo("snapshots")
  // Resolvers.typesafeIvyRepo("releases")
  // Resolvers.typesafeRepo("releases")
  // Resolvers.typesafeRepo("snapshots")
  // JavaNet2Repository,
  // JavaNet1Repository
)

/* sbt behavior */
logLevel in compile := Level.Warn

traceLevel := 5

releaseSettings

/* publishing */
publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some(
    "snapshots" at nexus + "content/repositories/snapshots"
  )
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
                      }

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <developers>
    <developer>
      <id>pkaeding</id>
      <name>Patrick Kaeding</name>
      <email>patrick@kaeding.name</email>
      <!-- <url></url> -->
    </developer>
  </developers>
)

// Josh Suereth's step-by-step guide to publishing on sonatype
// httpcom://www.scala-sbt.org/using_sonatype.html

/* assembly plugin */
mainClass in AssemblyKeys.assembly := Some("name.kaeding.fibs.Main")

assemblySettings

test in AssemblyKeys.assembly := {}
