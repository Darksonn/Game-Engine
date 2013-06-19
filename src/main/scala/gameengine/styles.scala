package gameengine.styles

import gameengine._

class Style { this: Game =>
	override def step(input: Input): Seq[ControlUpdate] = Seq()
}

private[gameengine] class PollingInputState {
	private var keysDown = Set[Key]()
	def keyDown(key: Key) = keysDown.contains(key)

	private var _mousePos: Option[Point] = None
	def mousePos = _mousePos.get

	private[gameengine] def update(input: Input) = {
		input.queue.foreach {
			case KeyDownEvent(key) => keysDown += key
			case KeyUpEvent(key) => keysDown -= key
			case MouseMoveEvent(pos) => _mousePos = Some(pos)

			case _: MouseClickEvent =>
			case _: KeyTypeEvent =>
		}
	}
}

trait PollingInputStyle extends Style { this: Game =>
	private val pollingInputState = new PollingInputState
	def keyDown(key: Key) = pollingInputState.keyDown(key)
	def mousePos = pollingInputState.mousePos

	override def step(input: Input) = {
		pollingInputState.update(input)
		super.step(input)
	}
}

trait EventInputStyle extends Style { this: Game =>
	override def step(input: Input) = {
		unhandled(input.queue.filterNot { ev => on.lift(ev).isDefined })
		super.step(input)
	}

	def on: PartialFunction[InputEvent, Unit]
	def unhandled(evs: Seq[InputEvent])
}