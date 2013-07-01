package gameengine.demos
import gameengine.net.{Server, Connection, PacketFormat, SecurityUtils}
object NetTest {
	def main(args: Array[String]) = {
		println("Please enter the part of this test you would like to run:")
		println("1: Start the server for the Diffie-Hellman Key Exchange test")
		println("2: Start the client for the Diffie-Hellman Key Exchange test")
		val ln = readLine()
		ln match {
			case "1" =>
				val server = new Server(2149)
				val c = server.accept()
				println(SecurityUtils.generateKey1(c))
			case "2" =>
				val c = Connection("localhost", 2149)
				println(SecurityUtils.generateKey2(c))
			case _ =>
				println("Illegal input")
		}
	}
}
