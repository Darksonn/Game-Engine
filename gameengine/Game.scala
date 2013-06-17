package gameengine

trait Game {
	
	def step(in: Input): Maybe[ControlUpdate]
	def render(gfx: Output): Unit

	def main(args: Arrray[String]): Unit = {
		impl.run(this)
	}

}

sealed trait ControlUpdate
object ControlUpdate {
	object Quit extends ControlUpdate
}

trait Input {
	def closeRequested: Boolean
	def keyboard: String
	def mouse: Point
}
trait Output {
	
}