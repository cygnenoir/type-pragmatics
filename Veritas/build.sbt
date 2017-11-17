name := "veritas"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"

libraryDependencies += "io.verizon.quiver" %% "core" % "5.4.12"

libraryDependencies += "org.jetbrains.xodus" % "xodus-entity-store" % "1.0.6"

libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "1.2.1"

libraryDependencies += "org.scalameta" %% "scalameta" % "2.1.2"

unmanagedJars in Compile += file("lib/scalaz3_2.11-3.0.jar")

assemblyJarName in assembly := "Veritas.jar"

test in assembly := {}

scalacOptions += "-deprecation"

scalacOptions += "-feature"

fork in Test := true
