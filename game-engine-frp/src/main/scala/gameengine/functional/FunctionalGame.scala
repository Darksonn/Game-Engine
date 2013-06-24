package gameengine.functional

import gameengine._
import scalaz._
import scalaz.effect._

trait FunctionalGame {

	val width: Int
	val height: Int
	val title: String
	def game: SignalT[({type l[+A]=WriterT[IO, Seq[ControlUpdate], A]})#l, Picture[Any]]

	def main(args: Array[String]) = {
		val self = this
		new BaseGame {
			val width = self.width
			val height = self.height
			val title = self.title
			var state = game
			def step(in: Input) = {
				val (controlUpdates, next) = state.tail.run(in).run.unsafePerformIO
				state = next
				controlUpdates
			}
			def render(gfx: Output) {
				state.head.run(gfx).unsafePerformIO
			}
		}.main(args)
	}

}