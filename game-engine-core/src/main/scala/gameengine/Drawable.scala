package gameengine

import gameengine.{Output => Out}
import java.awt.image.BufferedImage
import java.awt.geom.AffineTransform

trait Drawable {
	protected[gameengine] def draw(out: Out)
}
object Drawable {
	def loadImage(image: BufferedImage): Drawable = new Drawable {
		def draw(out: Out) {
			val pixels = new Array[Int](image.getWidth * image.getHeight)
			image.getRGB(0, 0, image.getWidth, image.getHeight, pixels, 0, image.getWidth)
			val pixelWidth = 1.0/image.getWidth
			val pixelHeight = 1.0/image.getHeight
			for (y <- 0 until image.getHeight) {
				for (x <- 0 until image.getWidth) {
					val pixel = pixels(y * image.getWidth + x)
					val red = (pixel >> 16) & 0xFF
					val green = (pixel >> 8) & 0xFF
					val blue = pixel & 0xFF
					val alpha = (pixel >> 24) & 0xFF
					if (alpha > 0) {//Dont bother drawing anything if it's transparent
						val c = new java.awt.Color(red, green, blue, alpha)
						out.drawFilledRect(x*pixelWidth, y*pixelHeight, pixelWidth, pixelHeight, c)
					}
				}
			}
		}
	}
	def loadImage(path: String): Drawable = loadImage(classOf[Drawable].getResource(if (path.startsWith("/")) path else "/" + path))
	def loadImage(url: java.net.URL): Drawable = loadImage(javax.imageio.ImageIO.read(url))
}
/**
 * A drawable that contains the specified string with the specified font and color
 * The size of the font has no effect on drawn string, use scaling to change the size afterwards
 */
class DrawableText(s: String, font: java.awt.Font, color: java.awt.Color) extends Drawable {
	protected[gameengine] def draw(out: Out) {
		val f = font.deriveFont(30f)//(java.awt.geom.AffineTransform.getScaleInstance(15,15))
		val g2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics
		val vect = f.createGlyphVector(g2.getFontRenderContext, s)
		val shape = vect.getOutline(0f, -vect.getVisualBounds.getY.floatValue)
		g2.dispose
		val bounds = shape.getBounds2D
		val img = new BufferedImage(Math.ceil(bounds.getWidth).toInt, Math.ceil(bounds.getHeight).toInt, BufferedImage.TYPE_4BYTE_ABGR)
		val gfx = img.createGraphics
		gfx.setColor(color)
		gfx.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
		gfx.fill(shape)
		gfx.dispose
		val drawable = Drawable.loadImage(img)
		out.withScaling(img.getWidth.doubleValue / img.getHeight, 1) {
			out.draw(drawable)
		}
	}
}
