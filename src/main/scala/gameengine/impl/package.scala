package gameengine

import javax.swing._
import java.awt.{Color, Graphics, Graphics2D, Dimension}
import java.awt.event.{InputEvent => _, _}
import java.util.concurrent.atomic.AtomicReference

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
				game.render(todo)
			}
			val nowTime = System.nanoTime()
			Thread.sleep(((nspf - (nowTime - lastTime)) / 1000000L) max 1L)
		}
	}

}