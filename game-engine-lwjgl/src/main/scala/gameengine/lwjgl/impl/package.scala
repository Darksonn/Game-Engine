package gameengine.lwjgl

import org.lwjgl.opengl.GL11._
import org.lwjgl._
import java.awt.Color
import org.lwjgl.opengl._
import gameengine.{Point, InputEvent, ControlUpdate, CloseRequestedEvent, KeyDownEvent, KeyUpEvent, MouseMoveEvent, Key}
import org.lwjgl.input.{Mouse, Keyboard}

package object impl {

	var lastFrame: Long = -1
	var lastFps: Long = -1
	var fps: Long = -1
	
	def getTime = (Sys.getTime() * 1000) / Sys.getTimerResolution()
	def getDelta = {
		if (lastFrame == -1) -1
		val cT = getTime
		(getTime - lastFrame).intValue
	}
	def updateFps() = {
		if (getTime - lastFps > 1000) {
			if (getTime - lastFps > 2000) {
				val seconds = (getTime - lastFps) / 1000
				lastFps += seconds*1000
				println((1d/seconds) + " fps")
			} else {
				lastFps += 1000
				println(fps + " fps");
			}
			fps = 0
		}
		fps += 1
	}

	object EventListener {

		val keys = Map(
			0 -> 153,
			1 -> 27,
			2 -> 49,
			3 -> 50,
			4 -> 51,
			5 -> 52,
			6 -> 53,
			7 -> 54,
			8 -> 55,
			9 -> 56,
			10 -> 57,
			11 -> 48,
			12 -> 45,
			13 -> 521,
			14 -> 8,
			16 -> 81,
			17 -> 87,
			18 -> 69,
			19 -> 82,
			20 -> 84,
			21 -> 89,
			22 -> 85,
			23 -> 73,
			24 -> 79,
			25 -> 80,
			26 -> 129,
			28 -> 10,
			29 -> 17,
			30 -> 65,
			31 -> 83,
			32 -> 68,
			33 -> 70,
			34 -> 71,
			35 -> 72,
			36 -> 74,
			37 -> 75,
			38 -> 76,
			42 -> 16,
			44 -> 90,
			45 -> 88,
			46 -> 67,
			47 -> 86,
			48 -> 66,
			49 -> 78,
			50 -> 77,
			51 -> 44,
			52 -> 46,
			53 -> 222,
			54 -> 16,
			55 -> 106,
			56 -> 18,
			57 -> 32,
			58 -> 20,
			59 -> 112,
			60 -> 113,
			61 -> 114,
			62 -> 115,
			63 -> 116,
			64 -> 117,
			65 -> 118,
			66 -> 119,
			67 -> 120,
			68 -> 121,
			70 -> 145,
			71 -> 103,
			72 -> 104,
			73 -> 105,
			74 -> 109,
			75 -> 100,
			76 -> 101,
			77 -> 102,
			78 -> 107,
			79 -> 97,
			80 -> 98,
			81 -> 99,
			82 -> 96,
			83 -> 110,
			87 -> 122,
			88 -> 123,
			157 -> 17,
			181 -> 111,
			183 -> 155,
			197 -> 19,
			199 -> 36,
			200 -> 38,
			201 -> 33,
			203 -> 37,
			205 -> 39,
			208 -> 40,
			207 -> 35,
			209 -> 34,
			211 -> 127,
			220 -> 524,
			221 -> 525

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
		while (running) {
			glClear(GL_COLOR_BUFFER_BIT)
			game.step(gameengine.Input(EventListener.getAndClear)).foreach {
				case ControlUpdate.Quit =>
					running = false
			}
			game.render(new OutputImpl())
			//updateFps()
			lastFrame = getTime
			Display.update()
			Display.sync(60)
		}
		Display.destroy()
		lastFrame = -1
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
		def drawFilledOval(x: Double, y: Double, w: Double, h: Double, c: Color) {
			withTranslation(x, y) {
				withScaling(w, h) {
					drawFilledCircle(c)
				}
			}
		}
		def drawFilledRect(x: Double, y: Double, w: Double, h: Double, c: Color) {
			withTranslation(x, y) {
				withScaling(w, h) {
					drawFilledRect(c)
				}
			}
		}
		def drawOval(x: Double, y: Double, w: Double, h: Double, c: Color) {
			withTranslation(x, y) {
				withScaling(w, h) {
					drawCircle(c)
				}
			}
		}
		def drawRect(x: Double, y: Double, w: Double, h: Double, c: Color) {
			withTranslation(x, y) {
				withScaling(w, h) {
					drawRect(c)
				}
			}
		}
		def draw(drawable: gameengine.Drawable) {
			drawable.draw(this)
		}
		def isAntialiasingEnabled = aaenable
	}
	
}