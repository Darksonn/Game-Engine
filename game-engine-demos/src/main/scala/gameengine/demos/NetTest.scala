package gameengine.demos
import gameengine.net.{Server, Connection, PacketFormat, Security}
object NetTest {
	def main(args: Array[String]) = {
		println("Please enter the part of this test you would like to run:")
		println("1: Start the server for the Diffie-Hellman Key Exchange test")
		println("2: Start the client for the Diffie-Hellman Key Exchange test")
		println("3: RSA encrypt file")
		println("4: RSA decrypt file")
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
			case "3" =>
				val f = readLine()
				val from = f + "\\" + "testin.zip"
				val target = f + "\\" + "test"
				val keys = readKeypair(f)
				keys match {
					case (pubkey, prikey) =>
						val fromStream = new java.io.FileInputStream(from)
						val toStream = new Security.RSAOutputStream(new java.io.FileOutputStream(target), pubkey)
						var c = 0
						while (fromStream.available > 0) {
							val bytes = new Array[Byte](512)
							val len = fromStream.read(bytes)
							toStream.write(bytes, 0, len)
							c += 1
						}
						fromStream.close
						toStream.flush
						toStream.close
					case _ => throw new Error
				}
			case "4" =>
				val f = readLine()
				val from = f + "\\" + "test"
				val target = f + "\\" + "test.zip"
				val keys = readKeypair(f)
				keys match {
					case (pubkey, prikey) =>
						val fromStream = new Security.RSAInputStream(new java.io.FileInputStream(from), prikey)
						val toStream = new java.io.FileOutputStream(target)
						while (fromStream.available > 0) {
							val bytes = new Array[Byte](512)
							val len = fromStream.read(bytes)
							//println(len)
							toStream.write(bytes, 0, len)
						}
						fromStream.close
						toStream.flush
						toStream.close
					case _ => throw new Error
				}
			case _ =>
				println("Illegal input")
		}
	}
	def readKeypair(s: String): (Security.RSAPublicKey, Security.RSAPrivateKey) = {
		val pub = new java.io.File(new java.io.File(s), "pub.key")
		val pri = new java.io.File(new java.io.File(s), "pri.key")
		if (pub.exists) {
			return (Security.RSAPublicKey.readFrom(new java.io.FileInputStream(pub)), Security.RSAPrivateKey.readFrom(new java.io.FileInputStream(pri)))
		} else {
			val keypair = Security.RSA.generateKeypair(1024)
			keypair match {
				case (pubkey, prikey) =>
					val x1 = new java.io.FileOutputStream(pub)
					val x2 = new java.io.FileOutputStream(pri)
					pubkey.writeTo(x1)
					prikey.writeTo(x2)
					x1.close
					x2.close
				case _ => throw new Error
			}
			return keypair
		}
	}
}
