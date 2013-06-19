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
