package gameengine.net

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
			throw new java.io.IOException("Recieved unexpected data type.")
		c.in.readUTF()
	}
}
