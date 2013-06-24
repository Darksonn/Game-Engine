package gameengine.styles

import org.scalatest.FunSpec
import gameengine.{KeyUpEvent, KeyDownEvent, Input, Key}
import java.awt.event.KeyEvent

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
	}
}
