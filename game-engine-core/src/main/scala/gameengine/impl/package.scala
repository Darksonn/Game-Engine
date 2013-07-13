package gameengine

import javax.swing._
import java.awt.{Color, Graphics, Graphics2D, Dimension}
import java.awt.event.{InputEvent => _, _}
import java.util.concurrent.atomic.AtomicReference
import java.awt.image.BufferedImage

import gameengine.impl.pimps._

package object impl {

	val todo = null
	val fps = 60
	val nspf = 1000000000L / fps

	class GameComponent(game: BaseGame) extends JComponent {
		setPreferredSize(new Dimension(game.width, game.height))
	}

	class EverythingListener
			extends MouseListener
			with MouseMotionListener
			with KeyListener
			with WindowListener {

		private val events = new AtomicReference(Seq[InputEvent]())
		private def push(e: InputEvent) {
			events.transform(_ :+ e)
		}
		def getAndClear(): Seq[InputEvent] = events.getAndSet(Seq())

		def windowDeactivated(ev: WindowEvent) {}
		def windowActivated(ev: WindowEvent) {}
		def windowDeiconified(ev: WindowEvent) {}
		def windowIconified(ev: WindowEvent) {}
		def windowClosed(ev: WindowEvent) {}
		def windowClosing(ev: WindowEvent) { push(CloseRequestedEvent) }
		def windowOpened(ev: WindowEvent) {}

		def keyReleased(ev: KeyEvent) { push(KeyUpEvent(Key.KeyboardKey(ev.getKeyCode))) }
		def keyPressed(ev: KeyEvent) { push(KeyDownEvent(Key.KeyboardKey(ev.getKeyCode))) }
		def keyTyped(ev: KeyEvent) { push(KeyTypeEvent(Key.KeyboardKey(ev.getKeyCode))) }

		def mouseMoved(ev: MouseEvent) { push(MouseMoveEvent(Point(ev.getX, ev.getY))) }
		def mouseDragged(ev: MouseEvent) { push(MouseMoveEvent(Point(ev.getX, ev.getY))) }
		def mouseEntered(ev: MouseEvent) {}
		def mouseExited(ev: MouseEvent) {}

		def mouseReleased(ev: MouseEvent) { push(KeyUpEvent(Key.MouseButton(ev.getButton))) }
		def mousePressed(ev: MouseEvent) { push(KeyDownEvent(Key.MouseButton(ev.getButton))) }
		def mouseClicked(ev: MouseEvent) { push(MouseClickEvent(Key.MouseButton(ev.getButton), Point(ev.getX, ev.getY))) }

	}

	def run(game: gameengine.BaseGame) {
		val window = new JFrame(game.title)
		val comp = new GameComponent(game)

		val eventListener = new EverythingListener
		window.addKeyListener(eventListener)
		comp.addMouseListener(eventListener)
		comp.addMouseMotionListener(eventListener)
		window.addWindowListener(eventListener)

		window.add(comp)
		window.setResizable(false)
		window.pack()
		window.pack()
		if (comp.getWidth != game.width || comp.getHeight != game.height) {
			println("Warning! Window has size (" + comp.getWidth + ", " + comp.getHeight + "), which is not the required size.")
		}
		window.setLocationRelativeTo(null)
		window.setVisible(true)
		window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
		var running = true
		var lastTime = System.nanoTime()
		while (running) {
			val events = Input(eventListener.getAndClear())
			game.step(events).foreach {
				case ControlUpdate.Quit =>
					running = false
			}
			if (running) {
				val img = new BufferedImage(game.width, game.height, BufferedImage.TYPE_INT_ARGB)
				val gfx = img.createGraphics()
				game.render(new OutputImpl(gfx))
				comp.getGraphics.drawImage(img, 0, 0, comp)
			}
			val nowTime = System.nanoTime()
			Thread.sleep(((nspf - (nowTime - lastTime)) / 1000000L) max 1L)
			lastTime = System.nanoTime()
		}
		window.dispose()
	}

	class OutputImpl(gfx: Graphics2D) extends gameengine.Output {
		def withRotation[A](radians: Double)(body: => A): A = {
			val transformation = gfx.getTransform()
			gfx.rotate(radians)
			val res = body
			gfx.setTransform(transformation)
			res
		}
		def withScaling[A](scaleX: Double, scaleY: Double)(body: => A): A = {
			val transformation = gfx.getTransform()
			gfx.scale(scaleX, scaleY)
			val res = body
			gfx.setTransform(transformation)
			res
		}
		def withTranslation[A](deltaX: Double, deltaY: Double)(body: => A): A = {
			val transformation = gfx.getTransform()
			gfx.translate(deltaX, deltaY)
			val res = body
			gfx.setTransform(transformation)
			res
		}
		def drawFilledCircle(c: Color) {
			gfx.setColor(c)
			gfx.fillOval(0, 0, 1, 1)
		}
		def drawFilledRect(c: Color) {
			gfx.setColor(c)
			gfx.fillRect(0, 0, 1, 1)
		}
		def drawCircle(c: Color) {
			gfx.setColor(c)
			gfx.drawOval(0, 0, 1, 1)
		}
		def drawRect(c: Color) {
			gfx.setColor(c)
			gfx.drawRect(0, 0, 1, 1)
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
		def draw(drawable: Drawable) {
			drawable.draw(gfx)
		}
	}

}
