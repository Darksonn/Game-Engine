package gameengine.demos
import gameengine.net.{Server, Connection, PacketFormat, Security}
object NetTest {
	def main(args: Array[String]) = {
		println("Please enter the part of this test you would like to run:")
		println("1: Start the server for the Diffie-Hellman Key Exchange test")
		println("2: Start the client for the Diffie-Hellman Key Exchange test")
		val ln = readLine()
		ln match {
			case "1" =>
				val server = new Server(2149)
				println("A server have ben started, use the client function in this program to start a client that will connect to this server, and see the equal numbers being printed here and in the client.")
				val c = server.accept()
				println(new BigInt(new java.math.BigInteger(Security.generateKey1(c))))
			case "2" =>
				println("Connecting to server run by the server function in this program.")
				val c = Connection("localhost", 2149)
				println(new BigInt(new java.math.BigInteger(Security.generateKey2(c))))
			case _ =>
				println("Illegal input")
		}
	}
}
