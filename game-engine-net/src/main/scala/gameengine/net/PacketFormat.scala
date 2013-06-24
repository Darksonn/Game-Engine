package gameengine.net

import scala.collection.immutable.{List, Map}
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
			c.flush
		}
		def receive(c: Connection): (A, B) = {
			val a = p1.receive(c)
			val b = p2.receive(c)
			return (a, b)
		}
	}
}
//Don't use several unblocking readers on the same connection at once.
//When queueRead is called, starts a thread, and listens on the connection until something is received and puts it in a buffer for retrival with read
class UnblockingPacketFormatReader[A](formatter: PacketFormat[A], c: Connection) {
	private var buffer: List[A] = List()
	private var reader: UnblockerThread = null
	class UnblockerThread extends Thread {
		override
		def run() {
			buffer = buffer ++ List(formatter.receive(c))
			initialized = false
		}
	}
	private def init() = {
		if (!initialized) {
			if (reader == null)
				reader = new UnblockerThread
			reader.setDaemon(true)
			reader.start()
		}
		initialized = true
	}
	private var initialized = false
	def queueRead(): Unit = {
		init()
	}
	//true if this reader is waiting for the connection to return something.
	def isReadWaiting = initialized
	//Returns a option, none if nothing is buffered, the next value in the buffer, if something is buffered.
	def read(): Option[A] = {
		if (buffer.isEmpty)
			return None
		val x = buffer(0)
		buffer = buffer.tail
		return Option(x)
	}
}

object StringPacketFormat extends PacketFormat[String] {
	def send(str: String, c: Connection): Unit = {
		c.out.writeUTF(str)
		c.flush
	}
	def receive(c: Connection): String = c.in.readUTF()
}
object BytePacketFormat extends PacketFormat[Byte] {
	def send(b: Byte, c: Connection): Unit = {
		c.out.writeByte(b)
		c.flush
	}
	def receive(c: Connection): Byte = c.in.readByte()
}
object ShortPacketFormat extends PacketFormat[Short] {
	def send(short: Short, c: Connection): Unit = {
		c.out.writeShort(short)
		c.flush
	}
	def receive(c: Connection): Short = c.in.readShort()
}
object IntPacketFormat extends PacketFormat[Int] {
	def send(i: Int, c: Connection): Unit = {
		c.out.writeInt(i)
		c.flush
	}
	def receive(c: Connection): Int = c.in.readInt()
}
object FloatPacketFormat extends PacketFormat[Float] {
	def send(i: Float, c: Connection): Unit = {
		c.out.writeFloat(i)
		c.flush
	}
	def receive(c: Connection): Float = c.in.readFloat()
}
object DoublePacketFormat extends PacketFormat[Double] {
	def send(i: Double, c: Connection): Unit = {
		c.out.writeDouble(i)
		c.flush
	}
	def receive(c: Connection): Double = c.in.readDouble()
}
object BooleanPacketFormat extends PacketFormat[Boolean] {
	def send(i: Boolean, c: Connection): Unit = {
		c.out.writeBoolean(i)
		c.flush
	}
	def receive(c: Connection): Boolean = c.in.readBoolean()
}
object CharPacketFormat extends PacketFormat[Char] {
	def send(i: Char, c: Connection): Unit = {
		c.out.writeChar(i)
		c.flush
	}
	def receive(c: Connection): Char = c.in.readChar()
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
	def receive(c: Connection): Map[A, B] = {
		var result = Map[A, B]()
		val l = c.in.readInt()
		for (_ <- 0 to l) {
			val k = formatterA.receive(c)
			val v = formatterB.receive(c)
			result = Map(k -> v) ++ result
		}
		return result
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
	def receive(c: Connection): List[A] = {
		var result = List[A]()
		val l = c.in.readInt()
		for (_ <- 0 to l) {
			val obj = formatter.receive(c)
			result = List(obj) ::: result
		}
		return result
	}
}
