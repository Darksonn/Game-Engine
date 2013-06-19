package gameengine

trait Drawable {
	def draw(gfx: Graphics2D): Unit
}
class DrawableImage(image: java.awt.Image) extends Drawable {
	def draw(gfx: Graphics2D): Unit = gfx.drawImage(image)
}
class DrawableText(text: String, font: java.awt.Font, color: java.awt.Color) extends Drawable {
	def draw(gfx: Graphics2D): Unit = {
		gfx.setFont(font)
		gfx.setColor(color)
		gfx.drawString(text, 0, 0)
	}
}
