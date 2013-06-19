package gameengine

trait Game {
	
	val width: Int
	val height: Int
	val title: String

	def step(in: Input): Option[ControlUpdate]
	def render(gfx: Output): Unit

	def main(args: Array[String]): Unit = {
		impl.run(this)
	}

}

sealed trait Key
object Key {
	object MiddleMouse extends Key
	object LeftMouse extends Key
	object RightMouse extends Key
	case class KeyboardKey(code: Int) extends Key
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
	def withRotation(radians: Double)(body: => Unit): Unit
	def withScaling(scaleX: Double, scaleY: Double)(body: => Unit): Unit
	def withTranslation(deltaX: Double, deltaY: Double)(body: => Unit): Unit
	def drawCircle(c: Color): Unit
	def drawRect(c: Color): Unit
	def drawLine(radians: Double, c: Color): Unit
}