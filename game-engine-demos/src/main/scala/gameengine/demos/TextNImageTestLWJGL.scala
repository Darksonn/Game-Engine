package gameengine.demos

import gameengine._
import gameengine.styles.{ImperativeControlStyle, Game, EventInputStyle}

object TextNImageTestLWJGL extends gameengine.lwjgl.LWJGLGame with Game with EventInputStyle with ImperativeControlStyle {

	val width = 640
	val height = 480
	val title = "Avoid the top!"

	val t1 = new DrawableText("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", new java.awt.Font("Monospaced", java.awt.Font.BOLD, 24), java.awt.Color.WHITE)
	val t2 = new DrawableText("""0123456789!"#¤%&/()=?@£$€{[]}|¨^~'*-_.:,;+<>\§""", new java.awt.Font("Monospaced", java.awt.Font.BOLD, 24), java.awt.Color.WHITE)
	val p1 = Drawable.loadImage(new java.net.URL("""http://hd.wallpaperswide.com/thumbs/colors_splash-t2.jpg"""))

	override def update(in: Input) = {
		Seq()
	}
	override def render(out: Output) {
		import out._
		withScaling(width, height) {
			drawFilledRect(java.awt.Color.BLACK)
		}
		withTranslation(10,10) {
			withScaling(15,15) {
				draw(t1)
			}
		}
		withTranslation(10,30) {
			withScaling(15,15) {
				draw(t2)
			}
		}
		withTranslation(10,50) {
			withScaling(510,330) {
				draw(p1)
			}
		}
	}

	override val on: PartialFunction[InputEvent, Unit] = {
		case CloseRequestedEvent => quit()
	}

}
