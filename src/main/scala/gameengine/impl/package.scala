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

	class Output(gfx: Graphics2D) extends gameengine.Output {
		def withRotation(radians: Double)(body: => Unit): Unit = {
			val transformation = gfx.getTransform()
			gfx.rotate(radians)
			body
			gfx.setTransform(transformation)
		}
		def withScaling(scaleX: Double, scaleX: Double)(body: => Unit): Unit = {
			val transformation = gfx.getTransform()
			gfx.scale(radians)
			body
			gfx.setTransform(transformation)
		}
		def withTranslation(deltaX: Double, deltaY: Double)(body: => Unit): Unit = {
			val transformation = gfx.getTransform()
			gfx.translate(radians)
			body
			gfx.setTransform(transformation)
		}
		def drawFilledCircle(c: Color): Unit = {
			gfx.fillOval(0, 0, 1, 1)
		}
		def drawFilledRect(c: Color): Unit = {
			gfx.fillRect(0, 0, 1, 1)
		}
		def drawCircle(c: Color): Unit = {
			gfx.drawOval(0, 0, 1, 1)
		}
		def drawRect(c: Color): Unit = {
			gfx.drawRect(0, 0, 1, 1)
		}
		def draw(drawable: Drawable): Unit = {
			drawable.draw(gfx)
		}
	}

}
