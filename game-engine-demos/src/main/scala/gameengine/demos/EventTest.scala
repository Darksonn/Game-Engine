package gameengine.demos

import gameengine._
import gameengine.styles.{ImperativeControlStyle, Game, EventInputStyle}

object EventTest extends Game with EventInputStyle with ImperativeControlStyle {

	val width = 640
	val height = 480
	val title = "Event test!"

	var x = 0.0
	var y = 0.0
	val speed = 1
	var upPressed = false
	var downPressed = false
	var leftPressed = false
	var rightPressed = false

	override def update(in: Input) = {
		if (upPressed) y -= speed
		if (downPressed) y += speed
		if (leftPressed) x -= speed
		if (rightPressed) x += speed
		for (event <- in.queue) {
			event match {
				case KeyDownEvent(key) => key match {
					case Key.KeyboardKey(code) => code match {
						case 37 => leftPressed = true
						case 38 => upPressed = true
						case 39 => rightPressed = true
						case 40 => downPressed = true
						case _ => Unit
					}
					case _ => Unit
				}
				case KeyUpEvent(key) => key match {
					case Key.KeyboardKey(code) => code match {
						case 37 => leftPressed = false
						case 38 => upPressed = false
						case 39 => rightPressed = false
						case 40 => downPressed = false
						case _ => Unit
					}
					case _ => Unit
				}
				case _ => Unit
			}
		}
		Seq()
	}
	override def render(out: Output) {
		import out._
		withScaling(width, height) {
			drawFilledRect(java.awt.Color.BLACK)
		}
		drawFilledRect(x, y, 3, 3, java.awt.Color.WHITE)
	}

	override val on: PartialFunction[InputEvent, Unit] = {
		case CloseRequestedEvent => quit()
	}
}
