package gameengine

import java.awt.Graphics2D

trait Drawable {
	protected[gameengine] def draw(gfx: Graphics2D)
}
object Drawable {
	def loadImage(image: java.awt.Image): Drawable = new Drawable {
		def draw(gfx: Graphics2D) { gfx.drawImage(image, 0, 0, null) }
	}
	def loadImage(path: String): Drawable = loadImage(classOf[Drawable].getResource(if (path.startsWith("/")) path else "/" + path))
	def loadImage(url: java.net.URL): Drawable = loadImage(javax.imageio.ImageIO.read(url))
}
class DrawableText(text: String, font: java.awt.Font, color: java.awt.Color) extends Drawable {
	protected[gameengine] def draw(gfx: Graphics2D) {
		gfx.setFont(font)
		gfx.setColor(color)
		gfx.drawString(text, 0, 0)
	}
}
