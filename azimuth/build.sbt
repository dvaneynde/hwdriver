ThisBuild / scalaVersion := "2.12.7"

ThisBuild / organization := "eu.dlvm"

lazy val domoticScala = (project in file("."))
  .settings(
    name := "DomoticScala",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  )
