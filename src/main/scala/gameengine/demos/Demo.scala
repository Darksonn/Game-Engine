package gameengine.demos

import gameengine._

object Demo extends Game {

	val width = 640
	val height = 480
	val title = "Hello, World!"

	var angle = 0.0
	val angleDelta = math.Pi * 2 / 360

	def step(in: Input) = {
		angle += angleDelta
		Seq()
	}
	def render(out: Output) = {
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

}