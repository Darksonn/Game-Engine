package gameengine.net

class Server(port: Int) {
	private val server = new java.net.ServerSocket(port, 128)

	//Blocks until it recieves a connection from a client.
	//When a connection is recieved, it is returned.
	//If stop() is called this method throws a SocketException
	def accept(): Connection = {
		val socket = server.accept()
		return Connection(socket)
	}
	def stop(): Unit = server.close()

}
