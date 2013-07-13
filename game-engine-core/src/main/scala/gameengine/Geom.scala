package gameengine

case class Point(x: Int, y: Int)
/**
 * Can function like a normal FloatingPoint but has some manipulation functions
 */
final class Vector2D(val x: Double, val y: Double) {
	def length = Math.sqrt(x*x + y*y)
	def angle = Math.atan2(y,x)
	def +(other: Vector2D) = Vector2D(x+other.x, y+other.y)
	def -(other: Vector2D) = Vector2D(x-other.x, y-other.y)
	def *(other: Vector2D) = Vector2D(x*other.x-y*other.y, y*other.x+x*other.y)
	def /(other: Vector2D) = Vector2D((x*other.x+y*other.y)/(c*c+d*d), (y*other.x-x*other.y)/(c*c+d*d))
	def *(factor: Double) = Vector2D(x*factor, y*factor)
	def normalize = Vector2D(Math.cos(angle), Math.sin(angle))
}
object Vector2D {
	def apply(x: Double, y: Double) = new Vector2D(x, y)
	def polar(angle: Double, length: Double) = Vector2D(Math.cos(angle), Math.sin(angle)) * length
}
