import sbt._
import Keys._

object GameEngineBuild extends Build {
	object Deps {
		object V {
			val Scala = "2.10.1"
		}
	}

	override lazy val settings = super.settings ++ Seq(
		scalaVersion := Deps.V.Scala
	)

	lazy val root = Project("game-engine", file("."), settings = Project.defaultSettings) aggregate (gameEngine, functional)

	lazy val gameEngine = Project("game-engine-core", file("game-engine"), settings = Project.defaultSettings)
	lazy val functional = Project("game-engine-frp", file("game-engine-frp"), settings = Project.defaultSettings) dependsOn gameEngine
}