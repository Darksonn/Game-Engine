package gameengine.demos

import gameengine._
import gameengine.styles.{ImperativeControlStyle, Game, EventInputStyle}

trait EventPrinter extends Game with EventInputStyle with ImperativeControlStyle {

	val width = 640
	val height = 480
	val title = "Event test!"

	var tpEvents = 0l
	var tp: BigInt = BigInt(0)

	override def update(in: Input) = {
		for (event <- in.queue) {
			if (event.isInstanceOf[TimePassEvent]) {
				tp = tp + event.asInstanceOf[TimePassEvent].ns
				tpEvents = tpEvents + 1
			} else {
				if (tpEvents > 0) {
					println(tpEvents + " TimePassEvents totalling at " + tp + " nanoseconds")
					tpEvents = 0
					tp = BigInt(0)
				}
				println(event)
			}
		}
		Seq()
	}
	override def render(out: Output) {
		import out._
		withScaling(width, height) {
			drawFilledRect(java.awt.Color.BLACK)
		}
	}

	override val on: PartialFunction[InputEvent, Unit] = {
		case CloseRequestedEvent => quit()
	}
}
object EventPrinterSwing extends EventPrinter