import gameengine.net.{Server, Connection, PacketFormat}
object NetTest {
	def main(args: Array[String]) = {
		val server = new Server(1000)
		var serverC: Connection = null
		new Thread {
			override def run() = {
				serverC = server.accept()
			}
			}.start()
		val client = Connection("localhost", 1000)
		println(BytePacketFormat.read(client));
	}
}
