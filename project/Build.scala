import sbt._
import Keys._

import com.github.theon.coveralls.CoverallsPlugin

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
		scalacOptions ++= Seq("-deprecation", "-feature", "-Xfatal-warnings"),
		libraryDependencies += Deps.Scalatest
	)

	lazy val masterSettings = Project.defaultSettings ++ ScctPlugin.mergeReportSettings ++ CoverallsPlugin.coverallsSettings
	lazy val subProjectSettings = Project.defaultSettings ++ ScctPlugin.instrumentSettings

	lazy val root = Project("game-engine", file("."), settings = masterSettings ++ Seq(
		run in Runtime <<= run in (demos, Runtime),
		run in Compile <<= run in Runtime
	)) aggregate (core, functional, demos, net, states)

	lazy val core = Project("game-engine-core", file("game-engine-core"), settings = subProjectSettings)
	lazy val functional = Project("game-engine-frp", file("game-engine-frp"), settings = subProjectSettings) dependsOn core
	lazy val demos = Project("game-engine-demos", file("game-engine-demos"), settings = subProjectSettings) dependsOn (core,net)
	lazy val net = Project("game-engine-net", file("game-engine-net"), settings = subProjectSettings) dependsOn core
	lazy val states = Project("game-engine-states", file("game-engine-states"), settings = subProjectSettings) dependsOn core
}
