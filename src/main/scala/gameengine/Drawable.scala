package gameengine

import java.awt.Graphics2D

trait Drawable {
	protected def draw(gfx: Graphics2D): Unit
}
object Drawable {
	def loadImage(image: java.awt.Image): Drawable = new Drawable {
		def draw(gfx: Graphics2D): Unit = gfx.drawImage(image)
	}
}
class DrawableText(text: String, font: java.awt.Font, color: java.awt.Color) extends Drawable {
	protected def draw(gfx: Graphics2D): Unit = {
		gfx.setFont(font)
		gfx.setColor(color)
		gfx.drawString(text, 0, 0)
	}
}
