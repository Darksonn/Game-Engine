package gameengine.net

import java.io.{DataInputStream, DataOutputStream, InputStream, OutputStream}

trait Connection {
	def in: DataInputStream
	def out: DataOutputStream
	def close: Unit
	def flush: Unit
	def unread(byte: Byte): Unit
	def unread(bytes: Array[Byte]): Unit
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
			def flush: Unit = out.flush
			def unread(byte: Byte): Unit = input2.unread(byte)
			def unread(bytes: Array[Byte]): Unit = input2.unread(bytes)
			def unread(bytes: Array[Byte], off: Int, len: Int): Unit = input2.unread(bytes, off, len)
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
		}
	}
}
