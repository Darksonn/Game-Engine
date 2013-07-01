package gameengine.net

import scala.collection.immutable.{List, Map}
import java.io.IOException

trait PacketFormat[T] {
	def send(obj: T, c: Connection): Unit
	//Receive wont block, if None is returned then either nothing have ben read or everything have ben unread again.
	def receive(c: Connection): Option[T]
	def blockReceive(c: Connection): T
}
object PacketFormat {
	def combine[A, B](p1: PacketFormat[A], p2: PacketFormat[B]): PacketFormat[(A, B)] = new PacketFormat[(A, B)] {
		def send(obj: (A, B), c: Connection): Unit = {
			p1.send(obj._1, c)
			p2.send(obj._2, c)
			c.flush
		}
		def receive(c: Connection): Option[(A, B)] = {
			val a = p1.receive(c)
			a match {
				case None =>
					None
				case Some(a2) =>
					val b = p2.receive(c)
					b match {
						case None =>
							p1.send(a2, Connection(c.in, c.unreadStream))
							None
						case Some(b2) =>
							return Some((a2, b2))
					}
			}
		}
		def blockReceive(c: Connection): (A, B) = {
			val a = p1.receive(c)
			val b = p2.receive(c)
			return (a, b)
		}
	}
}

object StringPacketFormat extends PacketFormat[String] {
	def send(str: String, c: Connection): Unit = {
		c.out.writeInt(str.length)
		for (ch <- str.toCharArray)
			c.out.writeChar(ch)
	}
	def receive(c: Connection): Option[String] = {
		if (c.readableBytes < 4)
			None
		val size = c.in.readInt
		if (c.readableBytes < size*2) {
			c.unreadStream.writeInt(size)
			None
		}
		val chars = new Array[Char](size)
		for (i <- 0 until size) {
			chars(i) = c.in.readChar
		}
		Some(new String(chars))
	}
	def blockReceive(c: Connection): String = {
		val size = c.in.readInt
		val chars = new Array[Char](size)
		for (i <- 0 until size) {
			chars(i) = c.in.readChar
		}
		new String(chars)
	}
}
object BytePacketFormat extends PacketFormat[Byte] {
	def send(b: Byte, c: Connection): Unit = {
		c.out.writeByte(b)
		c.flush
	}
	def receive(c: Connection): Option[Byte] = {
		if (c.readableBytes == 0)
			None
		Some(c.in.readByte())
	}
	def blockReceive(c: Connection): Byte = {
		c.in.readByte()
	}
}
object ShortPacketFormat extends PacketFormat[Short] {
	def send(short: Short, c: Connection): Unit = {
		c.out.writeShort(short)
		c.flush
	}
	def receive(c: Connection): Option[Short] = {
		if (c.readableBytes < 2)
			None
		Some(c.in.readShort())
	}
	def blockReceive(c: Connection): Short = {
		c.in.readShort()
	}
}
object IntPacketFormat extends PacketFormat[Int] {
	def send(i: Int, c: Connection): Unit = {
		c.out.writeInt(i)
		c.flush
	}
	def receive(c: Connection): Option[Int] = {
		if (c.readableBytes < 4)
			None
		Some(c.in.readInt())
	}
	def blockReceive(c: Connection): Int = {
		c.in.readInt()
	}
}
object LongPacketFormat extends PacketFormat[Long] {
	def send(i: Long, c: Connection): Unit = {
		c.out.writeLong(i)
		c.flush
	}
	def receive(c: Connection): Option[Long] = {
		if (c.readableBytes < 8)
			None
		Some(c.in.readLong())
	}
	def blockReceive(c: Connection): Long = {
		c.in.readLong()
	}
}
object FloatPacketFormat extends PacketFormat[Float] {
	def send(i: Float, c: Connection): Unit = {
		c.out.writeFloat(i)
		c.flush
	}
	def receive(c: Connection): Option[Float] = {
		if (c.readableBytes < 4)
			None
		Some(c.in.readFloat())
	}
	def blockReceive(c: Connection): Float = {
		c.in.readFloat()
	}
}
object DoublePacketFormat extends PacketFormat[Double] {
	def send(i: Double, c: Connection): Unit = {
		c.out.writeDouble(i)
		c.flush
	}
	def receive(c: Connection): Option[Double] = {
		if (c.readableBytes < 8)
			None
		Some(c.in.readDouble())
	}
	def blockReceive(c: Connection): Double = {
		c.in.readDouble()
	}
}
object BigIntPacketFormat extends PacketFormat[BigInt] {
	def send(i: BigInt, c: Connection): Unit = {
		val bytes = i.toByteArray()
		c.out.writeInt(bytes.length)
		c.out.write(bytes)
		c.flush
	}
	def receive(c: Connection): Option[BigInt] = {
		if (c.readableBytes < 4)
			None
		val length = c.in.readInt()
		if (c.readableBytes < length) {
			c.unreadStream.writeInt(length)
			None
		}
		val bytes = new Array[Byte](length)
		c.in.read(bytes)
		Some(new BigInt(new BigInteger(bytes)))
	}
	def blockReceive(c: Connection): BigInt = {
		val length = c.in.readInt()
		val bytes = new Array[Byte](length)
		c.in.read(bytes)
		new BigInt(new BigInteger(bytes))
	}
}
object BooleanPacketFormat extends PacketFormat[Boolean] {
	def send(i: Boolean, c: Connection): Unit = {
		c.out.writeByte(if (i) {1} else {0})
		c.flush
	}
	def receive(c: Connection): Option[Boolean] = {
		if (c.readableBytes == 0)
			None
		Some(c.in.readByte() == 0)
	}	
	def blockReceive(c: Connection): Boolean = {
		c.in.readBoolean()
	}
}
object CharPacketFormat extends PacketFormat[Char] {
	def send(i: Char, c: Connection): Unit = {
		c.out.writeChar(i)
		c.flush
	}
	def receive(c: Connection): Option[Char] = {
		if (c.readableBytes < 2)
			None
		Some(c.in.readChar())
	}
	def blockReceive(c: Connection): Char = {
		c.in.readChar()
	}
}
class MapPacketFormat[A,B](formatterA: PacketFormat[A], formatterB: PacketFormat[B]) extends PacketFormat[Map[A, B]] {
	def send(map: Map[A, B], c: Connection):Unit = {
		if (!map.hasDefiniteSize)
			throw new IllegalArgumentException("Can't send infinite maps.")
		c.out.writeInt(map.size)
		for ((key, value) <- map) {
			formatterA.send(key, c)
			formatterB.send(value, c)
		}
		c.flush
	}
	def receive(c: Connection): Option[Map[A, B]] = {
		var result = Map[A, B]()
		if (c.readableBytes < 4)
			None
		val l = c.in.readInt()
		for (_ <- 0 until l) {
			val k = formatterA.receive(c)
			k match {
				case None =>
					send(result, Connection(c.in, c.unreadStream))
					None
				case Some(k2) =>
					val v = formatterB.receive(c)
					v match {
						case None =>
							val c2 = Connection(c.in, c.unreadStream)
							send(result, c2)
							formatterA.send(k2, c2)
							None
						case Some(v2) =>
							result = Map(k2 -> v2) ++ result
					}
			}
		}
		Some(result)
	}
	def blockReceive(c: Connection): Map[A, B] = {
		var result = Map[A, B]()
		val l = c.in.readInt()
		for (_ <- 0 until l) {
			val k = formatterA.receive(c)
			val v = formatterB.receive(c)
			result = Map(k -> v) ++ result
		}
		result
	}
}
class ListPacketFormat[A](formatter: PacketFormat[A]) extends PacketFormat[List[A]] {
	def send(list: List[A], c: Connection):Unit = {
		if (!list.hasDefiniteSize)
			throw new IllegalArgumentException("Can't send infinite lists.")
		c.out.writeInt(list.length)
		for (x <- list) {
			formatter.send(x, c)
		}
		c.flush
	}
	def receive(c: Connection): Option[List[A]] = {
		var result = List[A]()
		if (c.readableBytes < 4)
			None
		val l = c.in.readInt()
		for (_ <- 0 until l) {
			val obj = formatter.receive(c)
			obj match {
				case None =>
					send(result.reverse(), Connection(c.in, c.unreadStream))
					None
				case Some(obj2) =>
					result = result ::: List(obj2)
			}
		}
		Some(result)
	}
	def blockReceive(c: Connection): List[A] = {
		var result = List[A]()
		val l = c.in.readInt()
		for (_ <- 0 until l) {
			val obj = formatterA.receive(c)
			result = result ::: List(obj)
		}
		result
	}
}
