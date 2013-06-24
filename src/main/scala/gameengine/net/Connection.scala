package gameengine.net

import java.io.{DataInputStream, DataOutputStream, InputStream, OutputStream}

trait Connection {
	def in: DataInputStream
	def out: DataOutputStream
	def close: Unit
	def flush: Unit
	def readableBytes: Int
	def unread(byte: Byte): Unit
	def unread(bytes: Array[Byte]): Unit
	def unreadStream: DataOutputStream
}
sealed class UnreadOutputStream(conn: Connection) extends OutputStream {
	def write(b: Int) = conn.unread(b.byteValue)
	def write(b: Array[Byte]) = conn.unread(b)
	def write(b: Array[Byte], off: Int, len: Int) = conn.unread(b, off, len)
}
object Connection {
	def apply(host: String, port: Int): Connection = {
		val socket = new java.net.Socket(host, port)
		return Connection(socket)
	}
	def apply(socket: java.net.Socket): Connection = {
		val input = socket.getInputStream()
		val output = socket.getOutputStream()
		val input2 = new java.io.PushbackInputStream(input)
		val input3 = new java.io.DataInputStream(input2)
		val output2: DataOutputStream = new DataOutputStream(output)
		new Connection {
			def in: DataInputStream = input3
			def out: DataOutputStream = output2
			def close: Unit = {
				in.close
				out.close
				socket.close
			}
			def readableData: Int = in.available()
			def flush: Unit = out.flush
			def unread(byte: Byte): Unit = input2.unread(byte)
			def unread(bytes: Array[Byte]): Unit = input2.unread(bytes)
			def unread(bytes: Array[Byte], off: Int, len: Int): Unit = input2.unread(bytes, off, len)
			def unreadStream: OutputStream = new DataOutputStream(new UnreadOutputStream(this))
		}
	}
	def apply(input: InputStream, output: OutputStream): Connection = {
		val input2 = new java.io.PushbackInputStream(input)
		val input3 = new java.io.DataInputStream(input2)
		val output2: DataOutputStream = new DataOutputStream(output)
		new Connection {
			def in: DataInputStream = input3
			def out: DataOutputStream = output2
			def close: Unit = {
				in.close
				out.close
			}
			def flush: Unit = out.flush
			def unread(byte: Byte): Unit = input2.unread(byte)
			def unread(bytes: Array[Byte]): Unit = input2.unread(bytes)
			def unreadStream: OutputStream = new DataOutputStream(new UnreadOutputStream(this))
		}
	}
}
