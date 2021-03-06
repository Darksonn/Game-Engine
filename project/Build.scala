import sbt._
import Keys._
import java.io.File

import com.github.retronym.SbtOneJar
import com.github.theon.coveralls.CoverallsPlugin

object GameEngineBuild extends Build {
	object Deps {
		object V {
			val Scala = "2.10.2"
			val Scalatest = "2.0.M5b"
			val Scalamock = "3.0.1"
			val LWJGL = "2.9.0"
		}
		val Scalatest = "org.scalatest" %% "scalatest" % V.Scalatest % "test"
		val Scalamock = "org.scalamock" %% "scalamock-scalatest-support" % V.Scalamock % "test"
		val LWJGL = "org.lwjgl.lwjgl" % "lwjgl" % V.LWJGL
		val LWJGLPlatform = "org.lwjgl.lwjgl" % "lwjgl-platform" % V.LWJGL
		val LWJGLAll = Seq(
			LWJGL,
			LWJGLPlatform classifier "natives-windows"
			/*LWJGLPlatform*/ classifier "natives-linux"
			/*LWJGLPlatform*/ classifier "natives-osx"
		)
	}
	val os = "windows"
	override lazy val settings = super.settings ++ Seq(
		scalaVersion := Deps.V.Scala,
		exportJars := true,
		scalacOptions ++= Seq("-deprecation", "-feature", "-Xfatal-warnings"),
		libraryDependencies ++= Seq(Deps.Scalatest, Deps.Scalamock)
	)

	object pfp {
		lazy val core = ProjectRef(uri("https://github.com/tailcalled/pfp.git#03d727264610bdff39692d76bdeb37792fe68734"), "pfp-core")
	}

	lazy val masterSettings = Project.defaultSettings ++ ScctPlugin.mergeReportSettings ++ CoverallsPlugin.coverallsSettings
	lazy val subProjectSettings = Project.defaultSettings ++ ScctPlugin.instrumentSettings ++ Seq(
		parallelExecution in Test := false,
		parallelExecution in ScctPlugin.ScctTest := false
	)

	lazy val root = Project("game-engine", file("."), settings = masterSettings ++ Seq(
		run in Runtime <<= run in (demos, Runtime),
		run in Compile <<= run in Runtime
	)) aggregate (core, functional, demos, net, states, lwjgl)

	lazy val core = Project("game-engine-core", file("game-engine-core"), settings = Project.defaultSettings)
	lazy val functional = Project("game-engine-frp", file("game-engine-frp"), settings = Project.defaultSettings) settings (libraryDependencies ++= Deps.LWJGLAll) dependsOn (core, pfp.core)
	lazy val demos = Project("game-engine-demos", file("game-engine-demos"), settings = Project.defaultSettings ++ SbtOneJar.oneJarSettings) dependsOn(core, net, functional, pfp.core, lwjgl)
	lazy val net = Project("game-engine-net", file("game-engine-net"), settings = Project.defaultSettings) dependsOn core
	lazy val states = Project("game-engine-states", file("game-engine-states"), settings = Project.defaultSettings) dependsOn core
	lazy val lwjgl = Project("game-engine-lwjgl", file("game-engine-lwjgl"), settings = Project.defaultSettings) settings (libraryDependencies ++= Deps.LWJGLAll) dependsOn core
}
