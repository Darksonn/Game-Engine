package gameengine.styles

import gameengine._

trait Game extends BaseGame {
	def preUpdate(input: Input) {}
	def update(input: Input): Seq[ControlUpdate]
	def postUpdate(): Seq[ControlUpdate] = Seq()

	final def step(input: Input): Seq[ControlUpdate] = {
		preUpdate(input)
		update(input) ++ postUpdate()
	}
}

trait Style { this: Game =>
	override def preUpdate(input: Input) {}
	override def postUpdate(): Seq[ControlUpdate] = Seq()
}

class PollingInputState {
	private var keysDown = Set[Key]()
	def keyDown(key: Key) = keysDown.contains(key)

	private var _mousePos: Option[Point] = None
	def mousePos = _mousePos.get

	private var _closeRequested = false
	def closeRequested = _closeRequested

	private var _timeDelta = 0L
	def timeDelta = _timeDelta

	private[gameengine] def update(input: Input) {
		input.queue.foreach {
			case KeyDownEvent(key) => keysDown += key
			case KeyUpEvent(key) => keysDown -= key
			case MouseMoveEvent(pos) => _mousePos = Some(pos)
			case CloseRequestedEvent => _closeRequested = true
			case TimePassEvent(ns) => _timeDelta = ns
			case _: MouseClickEvent =>
			case _: KeyTypeEvent =>
		}
	}
}

trait PollingInputStyle extends Style { this: Game =>
	private[gameengine] val pollingInputState = new PollingInputState
	def keyDown(key: Key) = pollingInputState.keyDown(key)
	def mousePos = pollingInputState.mousePos
	def closeRequested = pollingInputState.closeRequested
	def timeDelta = pollingInputState.timeDelta

	override def preUpdate(input: Input) {
		pollingInputState.update(input)
		super.preUpdate(input)
	}
}

trait EventInputStyle extends Style { this: Game =>
	override def preUpdate(input: Input) {
		super.preUpdate(input)
		unhandled(input.queue.filterNot { ev => on.lift(ev).isDefined })
	}

	def on: PartialFunction[InputEvent, Unit]
	def unhandled(evs: Seq[InputEvent]) {}
}

trait ImperativeControlStyle extends Style { this: Game =>
	private[gameengine] var controlUpdates = Seq[ControlUpdate]()

	def quit() {
		controlUpdates :+= ControlUpdate.Quit
	}

	override def postUpdate() = {
		val updates = super.postUpdate() ++ controlUpdates
		controlUpdates = Seq()
		updates
	}
}