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
	def connect(host: String, port: Int): Connection = {
		val conn = new java.net.Socket(host, port)
		return new Connection(new DataInputStream(conn.getInputStream()), new DataOutputStream(conn.getOutputStream())) {
			override
			def close() = {
				in.close
				out.close
				conn.close
			}
		}
	}
}
