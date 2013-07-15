package gameengine.lwjgl

import org.lwjgl.opengl.GL11._
import org.lwjgl._
import java.awt.Color
import org.lwjgl.opengl._

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
		while (!Display.isCloseRequested()) {
			glClear(GL_COLOR_BUFFER_BIT)
			game.render(new OutputImpl())
			updateFps()
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
			glRotated(Math.toDegrees(radians), 0f, 0f, 1f);
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
		def drawFilledCircle(c: Color) {
			glColor3d(c.getRed/255d, c.getGreen/255d, c.getBlue/255d)
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
			glColor3d(c.getRed/255d, c.getGreen/255d, c.getBlue/255d)
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
			throw new RuntimeException("NYI");
			//drawable.draw(gfx)
		}
	}
	
}