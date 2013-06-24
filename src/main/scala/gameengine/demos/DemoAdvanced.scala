package gameengine.demos

import gameengine._

object DemoAdvanced extends BaseGame {

	val width = 640
	val height = 480
	val title = "99 bottles of beer"

	object Colors {
		val Background = null
	}

	trait Entity {
		val x: Int
		val y: Int
		val color: java.awt.Color
		def step(in: Input, others: Seq[Entity]): Seq[Entity]
		def render(out: Output) = {
			import out._
			out.withScaling(10, 10) {
				out.drawFilledRect(color)
			}
		}
	}
	object Entity {
		case class Player(x: Int, y: Int) extends Entity {
			val color = java.awt.Color.BLUE
			def step(in: Input, others: Seq[Entity]) = {
				Seq()
			}
		}
	}

	var entities = Seq[Entity]() //todo: fill this in

	def step(in: Input) = {
		entities = entities.flatMap(e => e.step(in, entities))
		Seq()
	}
	def render(out: Output) = {
		out.withScaling(width, height) {
			out.drawFilledRect(Colors.Background)
		}
		for (e <- entities) {
			out.withTranslation(e.x, e.y) {
				e.render(out)
			}
		}
	}

}
