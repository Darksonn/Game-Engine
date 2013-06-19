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

	class GameComponent(game: Game) extends JComponent {
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

		def windowDeactivated(ev: WindowEvent): Unit = {}
		def windowActivated(ev: WindowEvent): Unit = {}
		def windowDeiconified(ev: WindowEvent): Unit = {}
		def windowIconified(ev: WindowEvent): Unit = {}
		def windowClosed(ev: WindowEvent): Unit = {}
		def windowClosing(ev: WindowEvent): Unit = {}
		def windowOpened(ev: WindowEvent): Unit = {}

		def keyReleased(ev: KeyEvent) {
      push(KeyUpEvent(Key.KeyboardKey(ev.getKeyCode)))
    }
		def keyPressed(ev: KeyEvent) {
      push(KeyDownEvent(Key.KeyboardKey(ev.getKeyCode)))
    }
		def keyTyped(ev: KeyEvent) {
      push(KeyTypeEvent(Key.KeyboardKey(ev.getKeyCode)))
    }

		def mouseMoved(ev: MouseEvent) {
      push(MouseMoveEvent(Point(ev.getX, ev.getY)))
    }
		def mouseDragged(ev: MouseEvent): Unit = {}
		def mouseEntered(ev: MouseEvent): Unit = {}
		def mouseExited(ev: MouseEvent): Unit = {}

		def mouseReleased(ev: MouseEvent) {
      push(KeyUpEvent(Key.MouseButton(ev.getButton)))
    }
		def mousePressed(ev: MouseEvent) {
      push(KeyDownEvent(Key.MouseButton(ev.getButton)))
    }
		def mouseClicked(ev: MouseEvent) {
      push(MouseClickEvent(Key.MouseButton(ev.getButton), Point(ev.getX, ev.getY)))
    }
		
	}

	def run(game: gameengine.Game): Unit = {
		val window = new JFrame(game.title)
		val comp = new GameComponent(game)
		window.add(comp)
		window.setResizable(false)
		window.pack()
		window.pack()
		window.setLocationRelativeTo(null)
		window.setVisible(true)
		window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
		var running = true
		var lastTime = System.nanoTime()
		while (running) {
			game.step(todo) match {
				case Some(ControlUpdate.Quit) =>
					running = false
				case None =>
			}
			if (running) {
				val img = new BufferedImage(game.width, game.height, BufferedImage.TYPE_INT_ARGB)
				val gfx = img.createGraphics();
				game.render(new OutputImpl(gfx))
				comp.getGraphics.drawImage(img, 0, 0, comp)
			}
			val nowTime = System.nanoTime()
			Thread.sleep(((nspf - (nowTime - lastTime)) / 1000000L) max 1L)
		}
	}

	class OutputImpl(gfx: Graphics2D) extends gameengine.Output {
		def withRotation(radians: Double)(body: => Unit): Unit = {
			val transformation = gfx.getTransform()
			gfx.rotate(radians)
			body
			gfx.setTransform(transformation)
		}
		def withScaling(scaleX: Double, scaleY: Double)(body: => Unit): Unit = {
			val transformation = gfx.getTransform()
			gfx.scale(scaleX, scaleY)
			body
			gfx.setTransform(transformation)
		}
		def withTranslation(deltaX: Double, deltaY: Double)(body: => Unit): Unit = {
			val transformation = gfx.getTransform()
			gfx.translate(deltaX, deltaY)
			body
			gfx.setTransform(transformation)
		}
		def drawFilledCircle(c: Color): Unit = {
			gfx.setColor(c)
			gfx.fillOval(0, 0, 1, 1)
		}
		def drawFilledRect(c: Color): Unit = {
			gfx.setColor(c)
			gfx.fillRect(0, 0, 1, 1)
		}
		def drawCircle(c: Color): Unit = {
			gfx.setColor(c)
			gfx.drawOval(0, 0, 1, 1)
		}
		def drawRect(c: Color): Unit = {
			gfx.setColor(c)
			gfx.drawRect(0, 0, 1, 1)
		}
		def draw(drawable: Drawable): Unit = {
			drawable.draw(gfx)
		}
	}

}
