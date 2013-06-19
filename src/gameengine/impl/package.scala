package gameengine

import javax.swing._
import java.awt.{Color, Graphics, Graphics2D, Dimension}
import java.awt.event._

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

		def windowDeactivated(ev: WindowEvent): Unit = {}
		def windowActivated(ev: WindowEvent): Unit = {}
		def windowDeiconified(ev: WindowEvent): Unit = {}
		def windowIconified(ev: WindowEvent): Unit = {}
		def windowClosed(ev: WindowEvent): Unit = {}
		def windowClosing(ev: WindowEvent): Unit = {}
		def windowOpened(ev: WindowEvent): Unit = {}

		def keyReleased(ev: KeyEvent): Unit = {}
		def keyPressed(ev: KeyEvent): Unit = {}
		def keyTyped(ev: KeyEvent): Unit = {}

		def mouseMoved(ev: MouseEvent): Unit = {}
		def mouseDragged(ev: MouseEvent): Unit = {}
		def mouseEntered(ev: MouseEvent): Unit = {}
		def mouseExited(ev: MouseEvent): Unit = {}

		def mouseReleased(ev: MouseEvent): Unit = {}
		def mousePressed(ev: MouseEvent): Unit = {}
		def mouseClicked(ev: MouseEvent): Unit = {}
		
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