package gameengine.demos

import gameengine._
import gameengine.lwjgl._
import gameengine.styles.{ImperativeControlStyle, Game, EventInputStyle}

object LwjglDemo extends Game with LWJGLGame with EventInputStyle with ImperativeControlStyle {

	val width = 640
	val height = 480
	val title = "Hello, LWJGL!"

	var angle = 0.0
	val angleDelta = math.Pi * 2 / 360

	override def update(in: Input) = {
		angle += angleDelta
		Seq()
	}
	override def render(out: Output) {
		import out._
		withScaling(width, height) {
			drawFilledRect(java.awt.Color.BLACK)
		}
		withTranslation(width / 2, height / 2) {
			withRotation(angle) {
				withScaling(50, 50) {
					withTranslation(-0.5, -0.5) {
						drawFilledRect(java.awt.Color.RED)
					}
				}
			}
		}
	}

	override val on: PartialFunction[InputEvent, Unit] = {
		case CloseRequestedEvent => quit()
	}
}