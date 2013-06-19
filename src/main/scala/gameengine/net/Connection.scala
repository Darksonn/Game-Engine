package gameengine.net

import java.io.{DataInputStream, DataOutputStream}

class Connection(input: DataInputStream, output: DataOutputStream) {
	def in = input
	def out = output
	def close() = {
		in.close()
		out.close()
	}
	def flush() = {
		out.flush()
	}
}
object Connection {
	def apply(host: String, port: Int): Connection = {
		val socket = new java.net.Socket(host, port)
		return Connection(socket)
	}
	def apply(socket: java.net.Socket): Connection = {
		return new Connection(new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream())) {
			override
			def close() = {
				in.close
				out.close
				socket.close
			}
		}
	}
	def apply(input: DataInputStream, output: DataOutputStream): Connection = new Connection(input, output)
}
