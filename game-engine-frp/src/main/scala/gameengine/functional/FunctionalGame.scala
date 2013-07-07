package gameengine.functional

import gameengine._

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
			def step(in: Input) = {
				val updates = state.now._1
				state = state.later(in)
				updates
			}
			def render(gfx: Output) {
				state.now._2.paint(gfx)
			}
		}.main(args)
	}

}