package gameengine

import java.awt.Color
import java.awt.event.{MouseEvent => AWTMouseEvent}

trait BaseGame {
	val width: Int
	val height: Int
	val title: String

	def step(in: Input): Seq[ControlUpdate]
	def render(gfx: Output)

	def main(args: Array[String]) {
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

sealed trait GenericEvent extends InputEvent
case object CloseRequestedEvent extends GenericEvent

trait Output {
	def withRotation(radians: Double)(body: => Unit)
	def withScaling(scaleX: Double, scaleY: Double)(body: => Unit)
	def withTranslation(deltaX: Double, deltaY: Double)(body: => Unit)
	def drawFilledCircle(c: Color)
	def drawFilledRect(c: Color)
	def drawCircle(c: Color)
	def drawRect(c: Color)
	def drawFilledOval(x: Double, y: Double, w: Double, h: Double, c: Color)
	def drawFilledRect(x: Double, y: Double, w: Double, h: Double, c: Color)
	def drawOval(x: Double, y: Double, w: Double, h: Double, c: Color)
	def drawRect(x: Double, y: Double, w: Double, h: Double, c: Color)
	def draw(drawable: Drawable)
}
