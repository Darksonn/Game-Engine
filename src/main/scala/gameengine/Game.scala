package gameengine

import java.awt.Color
import java.awt.event.{MouseEvent => AWTMouseEvent}

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
  val LeftMouse = MouseButton(AWTMouseEvent.BUTTON1)
  val MiddleMouse = MouseButton(AWTMouseEvent.BUTTON3)
  val RightMouse = MouseButton(AWTMouseEvent.BUTTON2)

  case class MouseButton(code: Int) extends Key
	case class KeyboardKey(code: Int) extends Key
}

sealed trait ControlUpdate
object ControlUpdate {
	object Quit extends ControlUpdate
}

case class Input(queue: Seq[InputEvent])
sealed trait InputEvent

sealed trait KeyInputEvent extends InputEvent
case class KeyTypeEvent(key: Key) extends KeyInputEvent
case class KeyDownEvent(key: Key) extends KeyInputEvent
case class KeyUpEvent(key: Key) extends KeyInputEvent

sealed trait MouseInputEvent extends InputEvent
case class MouseClickEvent(key: Key, pos: Point) extends MouseInputEvent
case class MouseMoveEvent(to: Point) extends MouseInputEvent

trait Output {
	def withRotation(radians: Double)(body: => Unit): Unit
	def withScaling(scaleX: Double, scaleY: Double)(body: => Unit): Unit
	def withTranslation(deltaX: Double, deltaY: Double)(body: => Unit): Unit
	def drawFilledCircle(c: Color): Unit
	def drawFilledRect(c: Color): Unit
	def drawCircle(c: Color): Unit
	def drawRect(c: Color): Unit
	def drawFilledOval(x: Double, y: Double, w: Double, h: Double, c: Color): Unit
	def drawFilledRect(x: Double, y: Double, w: Double, h: Double, c: Color): Unit
	def drawOval(x: Double, y: Double, w: Double, h: Double, c: Color): Unit
	def drawRect(x: Double, y: Double, w: Double, h: Double, c: Color): Unit
	def draw(drawable: Drawable): Unit
}
