package gameengine

case class Point(x: Int, y: Int) {
	def toVector = Vector2D(this)
}
final class Vector2D(val x: Double, val y: Double) {
	def length = Math.sqrt(x * x + y * y)
	def lengthSquared = {
		val l = length
		l*l
	}
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
	def distanceTo(other: Vector2D) = transform(other).length
	def isInsideCube(pos: Vector2D, width: Double, height: Double) = {
		if (x > pos.x) {
			if (x < pos.x+width) {
				if (y > pos.y) {
					if (y < pos.y+height) {
						true
					}
				}
			}
		}
		false
	}
	override def equals(that: Any) = {
		that match {
			case t: Vector2D =>
				(transform(t).length) < 1e-100
			case _ =>
				false
		}
	}
	override def toString = "Vector2D(" + x + ", " + y + ")"
	def toByteArray = {
		val xData = java.lang.Double.doubleToRawLongBits(x)
		val yData = java.lang.Double.doubleToRawLongBits(y)
		val arr = new Array[Byte](16)
		arr(0) =  (xData & 0xff).byteValue
		arr(1) =  ((xData >>> 8)  & 0xff).byteValue
		arr(2) =  ((xData >>> 16) & 0xff).byteValue
		arr(3) =  ((xData >>> 24) & 0xff).byteValue
		arr(4) =  ((xData >>> 32) & 0xff).byteValue
		arr(5) =  ((xData >>> 40) & 0xff).byteValue
		arr(6) =  ((xData >>> 48) & 0xff).byteValue
		arr(7) =  ((xData >>> 56) & 0xff).byteValue
		arr(8) =  (yData & 0xff).byteValue
		arr(9) =  ((yData >>> 8)  & 0xff).byteValue
		arr(10) = ((yData >>> 16) & 0xff).byteValue
		arr(11) = ((yData >>> 24) & 0xff).byteValue
		arr(12) = ((yData >>> 32) & 0xff).byteValue
		arr(13) = ((yData >>> 40) & 0xff).byteValue
		arr(14) = ((yData >>> 48) & 0xff).byteValue
		arr(15) = ((yData >>> 56) & 0xff).byteValue
		arr
	}
	def write(out: java.io.OutputStream) = {
		val arr = toByteArray
		out.write(arr)
	}
}
object Vector2D {
	def apply(pos: Point) = new Vector2D(pos.x, pos.y)
	def apply(x: Double, y: Double) = new Vector2D(x, y)
	def polar(angle: Double, length: Double) = new Vector2D(Math.cos(angle), Math.sin(angle)) * length
	def fromByteArray(arr: Array[Byte], off: Int): Vector2D = {
		if (off+16 > arr.length)
			throw new IllegalArgumentException("16 bytes needed for Vector2D, " + (arr.length-off) + " available.")
		val xData = (arr(off) & 0xff) | ((arr(off+1) & 0xff) << 8) | ((arr(off+2) & 0xff) << 16) | ((arr(off+3) & 0xff).longValue << 24) | ((arr(off+4) & 0xff).longValue << 32) | ((arr(off+5) & 0xff).longValue << 40) | ((arr(off+6) & 0xff).longValue << 48) | ((arr(off+7) & 0xff).longValue << 56)
		val yData = (arr(off+8) & 0xff) | ((arr(off+9) & 0xff) << 8) | ((arr(off+10) & 0xff) << 16) | ((arr(off+11) & 0xff).longValue << 24) | ((arr(off+12) & 0xff).longValue << 32) | ((arr(off+13) & 0xff).longValue << 40) | ((arr(off+14) & 0xff).longValue << 48) | ((arr(off+15) & 0xff).longValue << 56)
		return Vector2D(java.lang.Double.longBitsToDouble(xData), java.lang.Double.longBitsToDouble(yData))
	}
	def fromByteArray(arr: Array[Byte]): Vector2D = {
		return fromByteArray(arr, 0)
	}
	def parse(str2: String): Vector2D = {
		var str = str2
		if (str.startsWith("Vector2D"))
			str = str.substring(8)
		if (str.startsWith("("))
			str = str.substring(1)
		if (str.endsWith(")"))
			str = str.substring(0, str.length-1)
		val index = str.indexOf(",")
		val x = java.lang.Double.parseDouble(str.substring(0,index-1))
		str = str.substring(index)
		if (str.startsWith(" "))
			str = str.substring(1)
		val y = java.lang.Double.parseDouble(str)
		return Vector2D(x, y)
	}
	def read(in: java.io.InputStream): Vector2D = {
		val arr = new Array[Byte](16)
		in.read(arr)
		return fromByteArray(arr)
	}
}