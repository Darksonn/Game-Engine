package gameengine.styles

import gameengine._

trait StyledGame extends Game {
	def preUpdate(input: Input): Unit
	def update(input: Input): Seq[ControlUpdate]
	def postUpdate(): Seq[ControlUpdate]

	final def step(input: Input): Seq[ControlUpdate] = {
		preUpdate(input)
		update(input) ++ postUpdate()
	}
}

trait Style { this: StyledGame =>
	override def preUpdate(input: Input) {}
	override def postUpdate(): Seq[ControlUpdate] = Seq()
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

trait PollingInputStyle extends Style { this: StyledGame =>
	private val pollingInputState = new PollingInputState
	def keyDown(key: Key) = pollingInputState.keyDown(key)
	def mousePos = pollingInputState.mousePos

	override def preUpdate(input: Input) = {
		pollingInputState.update(input)
		super.preUpdate(input)
	}
}

trait EventInputStyle extends Style { this: StyledGame =>
	override def preUpdate(input: Input) = {
		unhandled(input.queue.filterNot { ev => on.lift(ev).isDefined })
		super.preUpdate(input)
	}

	def on: PartialFunction[InputEvent, Unit]
	def unhandled(evs: Seq[InputEvent])
}

trait ControlUpdateAccumulatingStyle extends Style { this: StyledGame =>
	private var controlUpdates = Seq[ControlUpdate]()

	def quit() {
		controlUpdates :+= ControlUpdate.Quit
	}

	override def postUpdate() = {
		val updates = super.postUpdate() ++ controlUpdates
		controlUpdates = Seq()
		updates
	}
}