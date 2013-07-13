package gameengine.demos

import java.awt.Color
import gameengine.functional._
import gameengine._
import dk.tailcalled.pfp._

object DemoFRP extends FunctionalGame {

	val width = 640
	val height = 480
	val title = "Hello, Ivory Tower!"

	def game =
		events.map(_.contains(CloseRequestedEvent)).map(_.guard(Vector(ControlUpdate.Quit)))
			.map(cu => (cu, unit[Picture[Unit]])) ++
		(always(square(Color.BLACK).scaled(width, height)) ++
		unfoldSignal(0.0)(_ + math.Pi / 180.0).map(dir =>
			square(Color.WHITE).translated(-0.5, -0.5).rotated(dir).scaled(50, 50).translated(width/2, height/2)))
		.map(pic => (Vector(), pic))

}