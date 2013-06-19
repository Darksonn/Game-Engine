package gameengine.net

import scala.collection.immutable.List
import java.io.IOException

trait PacketFormat[T] {
	def send(obj: T, c: Connection): Unit
	def receive(c: Connection): T
}
object PacketFormat {
	def combine[A, B](p1: PacketFormat[A], p2: PacketFormat[B]): PacketFormat[(A, B)] = new PacketFormat[(A, B)] {
		def send(obj: (A, B), c: Connection): Unit = {
			p1.send(obj._1, c)
			p2.send(obj._2, c)
			c.flush()
		}
		def receive(c: Connection): (A, B) = {
			val a = p1.receive(c)
			val b = p2.receive(c)
			return (a, b)
		}
	}
}

object StringPacketFormat extends PacketFormat[String] {
	def send(str: String, c: Connection): Unit = {
		c.out.writeByte(20)
		c.out.writeUTF(str)
		c.flush()
	}
	def receive(c: Connection): String = {
		val id: Byte = c.in.readByte()
		if (id != 20)
			throw new IOException("Recieved unexpected data type.")
		c.in.readUTF()
	}
}
object BytePacketFormat extends PacketFormat[Byte] {
	def send(b: Byte, c: Connection): Unit = {
		c.out.writeByte(b)
		c.flush()
	}
	def receive(c: Connection): Byte = c.in.readByte()
}
object ShortPacketFormat extends PacketFormat[Short] {
	def send(short: Short, c: Connection): Unit = {
		c.out.writeShort(short)
		c.flush()
	}
	def receive(c: Connection): Short = c.in.readShort()
}
object IntPacketFormat extends PacketFormat[Int] {
	def send(i: Int, c: Connection): Unit = {
		c.out.writeInt(i)
		c.flush()
	}
	def receive(c: Connection): Int = c.in.readInt()
}
object FloatPacketFormat extends PacketFormat[Float] {
	def send(i: Float, c: Connection): Unit = {
		c.out.writeFloat(i)
		c.flush()
	}
	def receive(c: Connection): Float = c.in.readFloat()
}
object DoublePacketFormat extends PacketFormat[Double] {
	def send(i: Double, c: Connection): Unit = {
		c.out.writeDouble(i)
		c.flush()
	}
	def receive(c: Connection): Double = c.in.readDouble()
}
object BooleanPacketFormat extends PacketFormat[Boolean] {
	def send(i: Boolean, c: Connection): Unit = {
		c.out.writeBoolean(i)
		c.flush()
	}
	def receive(c: Connection): Boolean = c.in.readBoolean()
}
class ListPacketFormat[A,B<:PacketFormat[A]](formatter: B) extends PacketFormat[List[A]] {
	def send(list: List[A], c: Connection):Unit = {
		c.out.writeByte(21)
		if (!list.hasDefiniteSize)
			throw new IllegalArgumentException("Can't send infinite lists.")
		c.out.writeInt(list.length)
		for (x <- list) {
			formatter.send(x, c)
		}
	}
	def receive(c: Connection): List[A] = {
		val id: Byte = c.in.readByte()
		if (id != 21)
			throw new IOException("Recieved unexpected data type.")
		var result = List[A]()
		val l = c.in.readInt()
		for (_ <- 0 to l) {
			val obj = formatter.receive(c)
			result = List(obj) ::: result
		}
		return result
	}
}
