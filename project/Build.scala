import sbt._
import Keys._

object GameEngineBuild extends Build {
	object Deps {
		object V {
			val Scala = "2.10.2"
			val Scalatest = "2.0.M5b"
		}
		val Scalatest = "org.scalatest" %% "scalatest" % V.Scalatest % "test"
	}

	override lazy val settings = super.settings ++ Seq(
		scalaVersion := Deps.V.Scala,
		scalacOptions ++= Seq("-deprecation", "-feature"),
		libraryDependencies += Deps.Scalatest
	)

	lazy val root = Project("game-engine", file("."), settings = Project.defaultSettings ++ Seq(
		run in Runtime <<= run in (demos, Runtime),
		run in Compile <<= run in Runtime
	)) aggregate (core, functional, demos, net, states)

	lazy val core = Project("game-engine-core", file("game-engine-core"), settings = Project.defaultSettings)
	lazy val functional = Project("game-engine-frp", file("game-engine-frp"), settings = Project.defaultSettings) dependsOn core
	lazy val demos = Project("game-engine-demos", file("game-engine-demos"), settings = Project.defaultSettings) dependsOn core
	lazy val net = Project("game-engine-net", file("game-engine-net"), settings = Project.defaultSettings) dependsOn core
	lazy val states = Project("game-engine-states", file("game-engine-states"), settings = Project.defaultSettings) dependsOn core
}
