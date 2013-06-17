package gameengine

trait Game {
	
	val width: Int
	val height: Int
	val title: String

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
	def isPressed(key: Key): Boolean
}
trait Output {
	
}