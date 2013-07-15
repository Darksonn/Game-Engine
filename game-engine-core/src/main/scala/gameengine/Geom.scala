package gameengine

case class Point(x: Int, y: Int) {
	def toVector = Vector2D(this)
}
final class Vector2D(val x: Double, val y: Double) {
	def length = Math.sqrt(x * x + y * y)
	def lengthSquared = length*length
	def angle = Math.atan2(y,x)
	def +(other: Vector2D) = Vector2D(x + other.x, y + other.y)
	def -(other: Vector2D) = Vector2D(x - other.x, y - other.y)
	def *(other: Vector2D) = Vector2D(x * other.x - y * other.y, y * other.x + x * other.y)
	def /(other: Vector2D) = Vector2D((x * other.x + y * other.y) / (other.x * other.x + other.y * other.y), (y * other.x - x * other.y) / (other.x * other.x + other.y * other.y))
	def *(factor: Double) = Vector2D(x * factor, y * factor)
	def normalize = Vector2D(Math.cos(angle), Math.sin(angle))
	def transform(center: Vector2D) = Vector2D(x - center.x, y - center.y)
	def toPoint = Point(x.intValue, y.intValue)
	def changeX(deltaX: Double) = Vector2D(x+deltaX, y)
	def changeY(deltaY: Double) = Vector2D(x, y+deltaY)
	override def toString = "Vector2D(" + x + ", " + y + ")"
}
object Vector2D {
	def apply(pos: Point) = new Vector2D(pos.x, pos.y)
	def apply(x: Double, y: Double) = new Vector2D(x, y)
	def polar(angle: Double, length: Double) = new Vector2D(Math.cos(angle), Math.sin(angle)) * length
}
