package gameengine

import javax.swing._
import java.awt.{Color, Graphics, Graphics2D}

package object impl {

	def run(game: gameengine.Game): Unit = {
		val window = new JFrame(game.title)
		window.setLocationRelativeTo(null)
	}

}