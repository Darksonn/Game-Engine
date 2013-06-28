package gameengine.styles

import org.scalatest.FunSpec
import gameengine._
import java.awt.event.KeyEvent
import gameengine.KeyDownEvent
import gameengine.Input
import gameengine.KeyUpEvent

class PollingInputStateSpec extends FunSpec {
	describe("PollingInputState") {
		it("should handle key events") {
			val s = new PollingInputState

			val aKey = Key.KeyboardKey(KeyEvent.VK_A)
			val bKey = Key.KeyboardKey(KeyEvent.VK_B)

			assert((s.keyDown(aKey), s.keyDown(bKey)) === (false, false))
			s.update(Input(Seq(KeyDownEvent(aKey))))
			assert((s.keyDown(aKey), s.keyDown(bKey)) === (true, false))
			s.update(Input(Seq(KeyDownEvent(bKey))))
			assert((s.keyDown(aKey), s.keyDown(bKey)) === (true, true))
			s.update(Input(Seq(KeyUpEvent(aKey))))
			assert((s.keyDown(aKey), s.keyDown(bKey)) === (false, true))
		}

		it("should handle multiple events in the same tick") {
			val s = new PollingInputState

			val aKey = Key.KeyboardKey(KeyEvent.VK_A)
			val bKey = Key.KeyboardKey(KeyEvent.VK_B)

			assert((s.keyDown(aKey), s.keyDown(bKey)) === (false, false))
			s.update(Input(Seq(KeyDownEvent(aKey), KeyDownEvent(bKey))))
			assert((s.keyDown(aKey), s.keyDown(bKey)) === (true, true))
		}

		it("should handle mouse events") {
			val s = new PollingInputState

			intercept[NoSuchElementException] {
				s.mousePos // Not fed any mouse data yet
			}

			s.update(Input(Seq(MouseMoveEvent(Point(0, 0)))))
			assert(s.mousePos === Point(0, 0))
			s.update(Input(Seq(MouseMoveEvent(Point(2, 1)))))
			assert(s.mousePos === Point(2, 1))
		}

		it("should handle close request events") {
			val s = new PollingInputState

			assert(s.closeRequested === false)
			s.update(Input(Seq(CloseRequestedEvent)))
			assert(s.closeRequested === true)
		}
	}
}

class EventInputStyleSpec extends FunSpec {
	describe("EventInputStyle") {
		describe("preUpdate") {
			it("should fire event handlers") {
				var events = Seq[InputEvent]()

				val handler = new TestGame with EventInputStyle {
					override def on = {
						case x => events :+= x
					}
				}

				handler.preUpdate(Input(Seq(CloseRequestedEvent)))
				assert(events === Seq(CloseRequestedEvent))
			}

			it("should fire event handlers after bubbling") {
				var events = Seq[InputEvent]()

				val handler = new TestGame with UpdateHooks with EventInputStyle {
					override def postPreUpdate(input: Input) {
						assert(events === Seq()) // Verify events is still unchanged
					}

					override def on = {
						case x => events :+= x
					}
				}

				handler.preUpdate(Input(Seq(CloseRequestedEvent)))
			}
		}
	}
}

/**
 * Game subtrait that provides some defaults that otherwise quickly get repetitive or don't make sense for the tests
 */
trait TestGame extends Game {
	override val title = ""
	override val (width, height) = (0, 0)

	override def render(gfx: Output) {}
	override def update(input: Input): Seq[ControlUpdate] = Seq()
}

trait UpdateHooks extends Style { this: Game =>
	def prePreUpdate(input: Input) {}
	def postPreUpdate(input: Input) {}

	override def preUpdate(input: Input) {
		prePreUpdate(input)
		super.preUpdate(input)
		postPreUpdate(input)
	}

	def prePostUpdate(): Seq[ControlUpdate] = Seq()
	def postPostUpdate(): Seq[ControlUpdate] = Seq()

	override def postUpdate(): Seq[ControlUpdate] =
		prePostUpdate() ++ super.postUpdate() ++ postPostUpdate()
}
