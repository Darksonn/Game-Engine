package gameengine

final case class Color(red: Int, green: Int, blue: Int, alpha: Int) {
	if (red < 0 || red > 255) {
		throw new IllegalArgumentException("red " + red + " is out of bounds 0-255")
	}
	if (green < 0 || green > 255) {
		throw new IllegalArgumentException("green " + green + " is out of bounds 0-255")
	}
	if (blue < 0 || blue > 255) {
		throw new IllegalArgumentException("blue " + blue + " is out of bounds 0-255")
	}
	if (alpha < 0 || alpha > 255) {
		throw new IllegalArgumentException("alpha " + alpha + " is out of bounds 0-255")
	}
	def isOpaque = alpha == 255
	def isTransparent = alpha == 0
}
object Color {
	def apply(red: Int, green: Int, blue: Int): Color = Color(red, green, blue, 255)
	def apply(str: String): Color = {
		if (str.startsWith("Color(") && str.endsWith(")")) {
			val comma1 = str.indexOf(",")
			val comma2 = str.indexOf(",", comma1)
			var comma3 = str.indexOf(",", comma2)
			val a: Int = if (comma3 == -1) {
				comma3 = str.length
				255
			} else {
				Integer.valueOf(str.substring(comma3, str.length - 1).trim())
			}
			val r = Integer.valueOf(str.substring(6,comma1 - 1).trim())
			val g = Integer.valueOf(str.substring(comma1,comma2 - 1).trim())
			val b = Integer.valueOf(str.substring(comma1,comma3 - 1).trim())
			return Color(r,g,b,a)
		} else {
			val r = Integer.valueOf(str.substring(0,2), 16)
			val g = Integer.valueOf(str.substring(2,4), 16)
			val b = Integer.valueOf(str.substring(4,6), 16)
			val a: Int = if (str.length == 6) {255} else {Integer.valueOf(str.substring(6,8), 16)}
			return Color(r,g,b,a)
		}
	}
	val black = Color(0,0,0)
	val blue = Color(0,0,255)
	val cyan = Color(0,255,255)
	val darkGray = Color(170,170,170)
	val gray = Color(128,128,128)
	val green = Color(0,255,0)
	val lightGray = Color(85,85,85)
	val orange = Color(255,165,0)
	val purple = Color(255,0,255)
	val red = Color(255,0,0)
	val white = Color(255,255,255)
	val yellow = Color(255,255,0)
}
