package gameengine

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
case class TimePassEvent(ns: Long) extends GenericEvent

trait Output {
	def withRotation[A](radians: Double)(body: => A): A
	def withScaling[A](scaleX: Double, scaleY: Double)(body: => A): A
	def withTranslation[A](deltaX: Double, deltaY: Double)(body: => A): A
	/**
	 * Might be enabled by default by implementation.
	 */
	def withAntialiasing[A](enabled: Boolean)(body: => A): A
	def drawFilledCircle(c: Color): Unit
	def drawFilledRect(c: Color): Unit
	def drawCircle(c: Color): Unit
	def drawRect(c: Color): Unit
	def drawFilledOval(x: Double, y: Double, w: Double, h: Double, c: Color) {
		withTranslation(x, y) {
			withScaling(w, h) {
				drawFilledCircle(c)
			}
		}
	}
	def drawFilledRect(x: Double, y: Double, w: Double, h: Double, c: Color) {
		withTranslation(x, y) {
			withScaling(w, h) {
				drawFilledRect(c)
			}
		}
	}
	def drawOval(x: Double, y: Double, w: Double, h: Double, c: Color) {
		withTranslation(x, y) {
			withScaling(w, h) {
				drawCircle(c)
			}
		}
	}
	def drawRect(x: Double, y: Double, w: Double, h: Double, c: Color) {
		withTranslation(x, y) {
			withScaling(w, h) {
				drawRect(c)
			}
		}
	}
	def draw(drawable: Drawable): Unit
	def isAntialiasingEnabled: Boolean
}
