package gameengine.functional

import gameengine._
import dk.tailcalled.pfp._

trait FunctionalGame {

	val width: Int
	val height: Int
	val title: String
	def game: Signal[(Vector[ControlUpdate], Picture[Unit])]

	def main(args: Array[String]) = {
		val self = this
		new BaseGame {
			val width = self.width
			val height = self.height
			val title = self.title
			var state = game
			var img: Picture[Unit] = unit[Picture[Unit]]
			def step(in: Input) = {
				val now = state.now
				img = now._2
				state = state.later(in)
				now._1
			}
			def render(gfx: Output) {
				img.paint(gfx)
			}
		}.main(args)
	}

}