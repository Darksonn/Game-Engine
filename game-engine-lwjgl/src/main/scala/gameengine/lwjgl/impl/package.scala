package gameengine.lwjgl

import org.lwjgl.opengl.GL11._
import org.lwjgl._
import java.awt.Color
import java.awt.event.KeyEvent
import org.lwjgl.opengl._
import gameengine.{Point, InputEvent, ControlUpdate, CloseRequestedEvent, KeyDownEvent, KeyUpEvent, MouseMoveEvent, Key}
import org.lwjgl.input.{Mouse, Keyboard}

package object impl {

	val fps = 60
	val nspf = 1000000000L / fps

	object EventListener {

		val keys = Map(
			Keyboard.KEY_ESCAPE -> KeyEvent.VK_ESCAPE,
			Keyboard.KEY_1 -> KeyEvent.VK_1,
			Keyboard.KEY_2 -> KeyEvent.VK_2,
			Keyboard.KEY_3 -> KeyEvent.VK_3,
			Keyboard.KEY_4 -> KeyEvent.VK_4,
			Keyboard.KEY_5 -> KeyEvent.VK_5,
			Keyboard.KEY_6 -> KeyEvent.VK_6,
			Keyboard.KEY_7 -> KeyEvent.VK_7,
			Keyboard.KEY_8 -> KeyEvent.VK_8,
			Keyboard.KEY_9 -> KeyEvent.VK_9,
			Keyboard.KEY_0 -> KeyEvent.VK_0,
			Keyboard.KEY_MINUS -> KeyEvent.VK_MINUS,
			Keyboard.KEY_EQUALS -> KeyEvent.VK_EQUALS,
			Keyboard.KEY_TAB -> KeyEvent.VK_TAB,
			Keyboard.KEY_Q -> KeyEvent.VK_Q,
			Keyboard.KEY_W -> KeyEvent.VK_W,
			Keyboard.KEY_E -> KeyEvent.VK_E,
			Keyboard.KEY_R -> KeyEvent.VK_R,
			Keyboard.KEY_T -> KeyEvent.VK_T,
			Keyboard.KEY_Y -> KeyEvent.VK_Y,
			Keyboard.KEY_U -> KeyEvent.VK_U,
			Keyboard.KEY_I -> KeyEvent.VK_I,
			Keyboard.KEY_O -> KeyEvent.VK_O,
			Keyboard.KEY_P -> KeyEvent.VK_P,
			Keyboard.KEY_A -> KeyEvent.VK_A,
			Keyboard.KEY_S -> KeyEvent.VK_S,
			Keyboard.KEY_D -> KeyEvent.VK_D,
			Keyboard.KEY_F -> KeyEvent.VK_F,
			Keyboard.KEY_G -> KeyEvent.VK_G,
			Keyboard.KEY_H -> KeyEvent.VK_H,
			Keyboard.KEY_J -> KeyEvent.VK_J,
			Keyboard.KEY_K -> KeyEvent.VK_K,
			Keyboard.KEY_L -> KeyEvent.VK_L,
			Keyboard.KEY_SEMICOLON -> KeyEvent.VK_SEMICOLON,
			Keyboard.KEY_Z -> KeyEvent.VK_Z,
			Keyboard.KEY_X -> KeyEvent.VK_X,
			Keyboard.KEY_C -> KeyEvent.VK_C,
			Keyboard.KEY_V -> KeyEvent.VK_V,
			Keyboard.KEY_B -> KeyEvent.VK_B,
			Keyboard.KEY_N -> KeyEvent.VK_N,
			Keyboard.KEY_M -> KeyEvent.VK_M,
			Keyboard.KEY_COMMA -> KeyEvent.VK_COMMA,
			Keyboard.KEY_PERIOD -> KeyEvent.VK_PERIOD,
			Keyboard.KEY_SLASH -> KeyEvent.VK_SLASH,
			Keyboard.KEY_MULTIPLY -> KeyEvent.VK_MULTIPLY,
			Keyboard.KEY_SPACE -> KeyEvent.VK_SPACE,
			Keyboard.KEY_F1 -> KeyEvent.VK_F1,
			Keyboard.KEY_F2 -> KeyEvent.VK_F2,
			Keyboard.KEY_F3 -> KeyEvent.VK_F3,
			Keyboard.KEY_F4 -> KeyEvent.VK_F4,
			Keyboard.KEY_F5 -> KeyEvent.VK_F5,
			Keyboard.KEY_F6 -> KeyEvent.VK_F6,
			Keyboard.KEY_F7 -> KeyEvent.VK_F7,
			Keyboard.KEY_F8 -> KeyEvent.VK_F8,
			Keyboard.KEY_F9 -> KeyEvent.VK_F9,
			Keyboard.KEY_F10 -> KeyEvent.VK_F10,
			Keyboard.KEY_NUMPAD7 -> KeyEvent.VK_NUMPAD7,
			Keyboard.KEY_NUMPAD8 -> KeyEvent.VK_NUMPAD8,
			Keyboard.KEY_NUMPAD9 -> KeyEvent.VK_NUMPAD9,
			Keyboard.KEY_SUBTRACT -> KeyEvent.VK_SUBTRACT,
			Keyboard.KEY_NUMPAD4 -> KeyEvent.VK_NUMPAD4,
			Keyboard.KEY_NUMPAD5 -> KeyEvent.VK_NUMPAD5,
			Keyboard.KEY_NUMPAD6 -> KeyEvent.VK_NUMPAD6,
			Keyboard.KEY_ADD -> KeyEvent.VK_ADD,
			Keyboard.KEY_NUMPAD1 -> KeyEvent.VK_NUMPAD1,
			Keyboard.KEY_NUMPAD2 -> KeyEvent.VK_NUMPAD2,
			Keyboard.KEY_NUMPAD3 -> KeyEvent.VK_NUMPAD3,
			Keyboard.KEY_NUMPAD0 -> KeyEvent.VK_NUMPAD0,
			Keyboard.KEY_NUMPADCOMMA -> KeyEvent.VK_COMMA,
			Keyboard.KEY_NUMPADENTER -> KeyEvent.VK_ENTER,
			Keyboard.KEY_NUMPADEQUALS -> KeyEvent.VK_EQUALS,
			Keyboard.KEY_DECIMAL -> KeyEvent.VK_DECIMAL,
			Keyboard.KEY_F11 -> KeyEvent.VK_F11,
			Keyboard.KEY_F12 -> KeyEvent.VK_F12,
			Keyboard.KEY_F13 -> KeyEvent.VK_F13,
			Keyboard.KEY_F14 -> KeyEvent.VK_F14,
			Keyboard.KEY_F15 -> KeyEvent.VK_F15,
			Keyboard.KEY_F16 -> KeyEvent.VK_F16,
			Keyboard.KEY_F17 -> KeyEvent.VK_F17,
			Keyboard.KEY_F18 -> KeyEvent.VK_F18,
			Keyboard.KEY_KANA -> KeyEvent.VK_KANA,
			Keyboard.KEY_F19 -> KeyEvent.VK_F19,
			Keyboard.KEY_CONVERT -> KeyEvent.VK_CONVERT,
			Keyboard.KEY_CIRCUMFLEX -> KeyEvent.VK_CIRCUMFLEX,
			Keyboard.KEY_AT -> KeyEvent.VK_AT,
			Keyboard.KEY_COLON -> KeyEvent.VK_COLON,
			Keyboard.KEY_KANJI -> KeyEvent.VK_KANJI,
			Keyboard.KEY_STOP -> KeyEvent.VK_STOP,
			Keyboard.KEY_DIVIDE -> KeyEvent.VK_DIVIDE,
			Keyboard.KEY_PAUSE -> KeyEvent.VK_PAUSE,
			Keyboard.KEY_HOME -> KeyEvent.VK_HOME,
			Keyboard.KEY_UP -> KeyEvent.VK_UP,
			Keyboard.KEY_LEFT -> KeyEvent.VK_LEFT,
			Keyboard.KEY_RIGHT -> KeyEvent.VK_RIGHT,
			Keyboard.KEY_END -> KeyEvent.VK_END,
			Keyboard.KEY_DOWN -> KeyEvent.VK_DOWN,
			Keyboard.KEY_INSERT -> KeyEvent.VK_INSERT,
			Keyboard.KEY_DELETE -> KeyEvent.VK_DELETE,
			Keyboard.KEY_CLEAR -> KeyEvent.VK_CLEAR,
			Keyboard.KEY_LBRACKET -> KeyEvent.VK_OPEN_BRACKET,
			Keyboard.KEY_RBRACKET -> KeyEvent.VK_CLOSE_BRACKET,
			Keyboard.KEY_LSHIFT -> KeyEvent.VK_SHIFT,
			Keyboard.KEY_RSHIFT -> KeyEvent.VK_SHIFT,
			Keyboard.KEY_LCONTROL -> KeyEvent.VK_CONTROL,
			Keyboard.KEY_RCONTROL -> KeyEvent.VK_CONTROL,
			Keyboard.KEY_BACKSLASH -> KeyEvent.VK_BACK_SLASH
		)

		var lastMouse = Point(0,0)
		def getAndClear = {
			var result = Seq[InputEvent]()
			while (Keyboard.next) {
				var key = Keyboard.getEventKey()
				val key2 = keys.get(key)
				key2 match {
					case None => key = -1
					case Some(x) => key = x
				}
				if (key != -1) {
					if (Keyboard.getEventKeyState()) {
						result = result :+ KeyDownEvent(Key.KeyboardKey(key))
					} else {
						result = result :+ KeyUpEvent(Key.KeyboardKey(key))
					}
				}
			}
			val mouse = Point(Mouse.getX, Mouse.getY)
			if (mouse != lastMouse) {
				result = result :+ MouseMoveEvent(mouse)
			}
			lastMouse = mouse
			while (Mouse.next) {
				Mouse.getEventButton match {
					case -1 => Unit//It was a mouse move, see above
					case x =>
						if (Mouse.getEventButtonState) {
							result = result :+ KeyDownEvent(Key.MouseButton(x+1))
						} else {
							result = result :+ KeyUpEvent(Key.MouseButton(x+1))
						}
				}
			}
			if (Display.isCloseRequested)
				result = result :+ CloseRequestedEvent
			result
		}
	}

	def run(game: LWJGLGame) = {
		Display.setDisplayMode(new DisplayMode(game.width, game.height))
		Display.create()
		Display.setVSyncEnabled(true)
		glEnable(GL_TEXTURE_2D)
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
		glEnable(GL_BLEND)
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
		glViewport(0,0,game.width,game.height)
		glMatrixMode(GL_MODELVIEW)
		glMatrixMode(GL_PROJECTION)
		glLoadIdentity()
		glOrtho(0, game.width, game.height, 0, 1, -1)
		glMatrixMode(GL_MODELVIEW)
		var running = true
		var lastTime = Sys.getTime * 1000000000L / Sys.getTimerResolution
		var delta = nspf
		while (running) {
			glClear(GL_COLOR_BUFFER_BIT)
			game.step(gameengine.Input(EventListener.getAndClear :+ gameengine.TimePassEvent(delta))).foreach {
				case ControlUpdate.Quit =>
					running = false
			}
			game.render(new OutputImpl())
			Display.update()
			Display.sync(fps)
			val thisTime = Sys.getTime * 1000000000L / Sys.getTimerResolution
			delta = thisTime - lastTime
			lastTime = Sys.getTime * 1000000000L / Sys.getTimerResolution
		}
		Display.destroy()
	}
	
	class OutputImpl extends gameengine.Output {
		def withRotation[A](radians: Double)(body: => A): A = {
			glPushMatrix()
			glRotated(Math.toDegrees(radians), 0, 0, 1);
			val res = body
			glPopMatrix()
			res
		}
		def withScaling[A](scaleX: Double, scaleY: Double)(body: => A): A = {
			glPushMatrix()
			glScaled(scaleX, scaleY, 0);
			val res = body
			glPopMatrix()
			res
		}
		def withTranslation[A](deltaX: Double, deltaY: Double)(body: => A): A = {
			glPushMatrix()
			glTranslated(deltaX, deltaY, 0d);
			val res = body
			glPopMatrix()
			res
		}
		private var aaenable = true
		def withAntialiasing[A](enabled: Boolean)(body: => A): A = {
			val prevaaenable = aaenable
			aaenable = enabled
			val res = body
			aaenable = prevaaenable
			res
		}
		def drawFilledCircle(c: Color) {
			if (aaenable)
				glEnable(GL_POLYGON_SMOOTH)
			else
				glDisable(GL_POLYGON_SMOOTH)
			glColor4d(c.getRed/255d, c.getGreen/255d, c.getBlue/255d, c.getAlpha/255d)
			val precision = 100
			var lastx = 1d
			var lasty = 0d
			var dir = 0d
			glLineWidth(1)
			for (i <- 0 until precision) {
				val ldir = dir
				dir += 2*Math.PI/i
				val x = Math.cos(dir)
				val y = Math.sin(dir)
				glBegin(GL_LINES)
				glVertex2d(lastx, lasty)
				glVertex2d(x, y)
				glEnd()
				lastx = x
				lasty = y
			}
		}
		def drawFilledRect(c: Color) {
			if (aaenable)
				glEnable(GL_POLYGON_SMOOTH)
			else
				glDisable(GL_POLYGON_SMOOTH)
			glColor4d(c.getRed/255d, c.getGreen/255d, c.getBlue/255d, c.getAlpha/255d)
			glBegin(GL_QUADS)
			glTexCoord2f(0, 0)
			glVertex2d(0, 0)
			glTexCoord2f(1, 0)
			glVertex2d(1, 0)
			glTexCoord2f(1, 1)
			glVertex2d(1, 1)
			glTexCoord2f(0, 1)
			glVertex2d(0, 1)
			glEnd()
		}
		def drawCircle(c: Color) {
			if (aaenable)
				glEnable(GL_POLYGON_SMOOTH)
			else
				glDisable(GL_POLYGON_SMOOTH)
			glColor4d(c.getRed/255d, c.getGreen/255d, c.getBlue/255d, c.getAlpha/255d)
			val precision = 100
			var lastx = 1d
			var lasty = 0d
			var dir = 0d
			for (i <- 0 until precision) {
				dir += (2*Math.PI/i)
				val x = Math.cos(dir)
				val y = Math.sin(dir)
				glBegin(GL_TRIANGLES)
				glVertex2d(0d, 0d)
				glVertex2d(lastx, lasty)
				glVertex2d(x, y)
				glEnd()
				lastx = x
				lasty = y
			}
		}
		def drawRect(c: Color) {
			if (aaenable)
				glEnable(GL_POLYGON_SMOOTH)
			else
				glDisable(GL_POLYGON_SMOOTH)
			glColor4d(c.getRed/255d, c.getGreen/255d, c.getBlue/255d, c.getAlpha/255d)
			glLineWidth(1)
			glBegin(GL_LINES)
			glVertex2d(0, 0)
			glVertex2d(0, 1)
			glEnd()
			glBegin(GL_LINES)
			glVertex2d(0, 1)
			glVertex2d(1, 1)
			glEnd()
			glBegin(GL_LINES)
			glVertex2d(1, 1)
			glVertex2d(1, 0)
			glEnd()
			glBegin(GL_LINES)
			glVertex2d(1, 0)
			glVertex2d(0, 0)
			glEnd()
		}
		def draw(drawable: gameengine.Drawable) {
			drawable.draw(this)
		}
		def isAntialiasingEnabled = aaenable
	}
	
}