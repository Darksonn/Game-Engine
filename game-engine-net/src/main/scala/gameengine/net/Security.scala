package gameengine.net

import java.math.BigInteger
import java.util.Random
import java.security._
import java.io.OutputStream
import java.io.InputStream
import java.util.Arrays

object Security {
	def generatePrime() = new BigInt(BigInteger.probablePrime(512, new Random))
	def generatePrime(bigLength: Int) = new BigInt(BigInteger.probablePrime(bigLength, new Random))
	def generatePrime(rand: Random) = new BigInt(BigInteger.probablePrime(512, rand))
	def generatePrime(bigLength: Int, rand: Random) = new BigInt(BigInteger.probablePrime(bigLength, rand))
	def generateInt() = new BigInt(new BigInteger(512, new Random))
	def generateInt(bigLength: Int) = new BigInt(new BigInteger(bigLength, new Random))
	def generateInt(bigLength: Int, rand: Random) = new BigInt(new BigInteger(bigLength, rand))
	def generateInt(rand: Random) = new BigInt(new BigInteger(512, rand))
	//Returns the smallest value for x that results in (a^x) % n == 1
	def multiplicativeOrder(a: BigInt, n: BigInt) = {
		var k = 2
		while (a.modPow(k, n) != 1) {
			k += 1
		}
		k
	}
	def totient(n: BigInt): BigInt = {
		var r = new BigInt(BigInteger.ZERO)
		for (i <- (new BigInt(BigInteger.ONE)) to n) {
			if (gcd(i, n) == 1) {
				r += 1
			}
		}
		return r
	}
	def isCoprime(a: BigInt, b: BigInt): Boolean = gcd(a, b) == 1
	def gcd(a2: BigInt, b2: BigInt): BigInt = {
		var a = a2
		var b = b2
		while (b != 0) {
			val c = a
			a = b
			b = c % b
		}
		return a
	}
	def lcm(a: BigInt, b: BigInt) = (a*b)/gcd(a,b)
	final class XOREncryptedInputStream(in: InputStream, key: Array[Byte]) extends InputStream {
		private val rand = new SecureRandom(key.clone)
		def getUnderlyingStream = in
		def getKey = key
		override def available = in.available
		override def close = in.close
		override def mark(readLimit: Int) = in.mark(readLimit)
		override def markSupported = in.markSupported
		override def read = {
			val before = in.read
			val action = nextByte(rand)
			before ^ action
		}
		override def read(b: Array[Byte]) = {
			val read = in.read(b)
			for (i <- 0 until b.length) {
				val before = b(i)
				val action = nextByte(rand)
				b(i) = (before ^ action).byteValue
			}
			read
		}
		override def read(b: Array[Byte], off: Int, len: Int) = {
			val read = in.read(b, off, len)
			for (i <- off until off+len) {
				val before = b(i)
				val action = nextByte(rand)
				b(i) = (before ^ action).byteValue
			}
			read
		}
		override def reset = in.reset
		override def skip(n: Long) = in.skip(n)
		override def hashCode = in.hashCode ^ key.hashCode
		override def equals(that: Any) = {
			that match {
			case f: XOREncryptedInputStream => (in == f.getUnderlyingStream) && (key == f.getKey)
			case _ => false
			}
		}
	}
	private[Security] def nextByte(rand: SecureRandom) = {
		val bytes: Array[Byte] = new Array[Byte](1)
		rand.nextBytes(bytes)
		bytes(0)
	}
	final class XOREncryptedOutputStream(out: OutputStream, key: Array[Byte]) extends OutputStream {
		private val rand = new SecureRandom(key.clone)
		def getUnderlyingStream = out
		def getKey = key
		override def close = out.close
		override def flush = out.flush
		override def write(b: Int) = out.write(b ^ nextByte(rand))
		override def write(b: Array[Byte]) = {
			val bytes = b.clone
			for (i <- 0 until bytes.length) {
				val before = bytes(i)
				bytes(i) = (before ^ nextByte(rand)).byteValue
			}
			out.write(bytes)
		}
		override def write(b: Array[Byte], off: Int, len: Int) = write(java.util.Arrays.copyOfRange(b, off, off+len))
		override def hashCode = out.hashCode ^ key.hashCode
		override def equals(that: Any) = {
			that match {
			case f: XOREncryptedOutputStream => (out == f.getUnderlyingStream) && (key == f.getKey)
			case _ => false
			}
		}
	}
	def encryptedConnectionXOR(c: Connection, key: Array[Byte]) = Connection(new XOREncryptedInputStream(c.in, key), new XOREncryptedOutputStream(c.out, key))
	//Performs Diffie-Hellman Key exchange with the connection, resulting in this function returning a BigInt that this and the reciever of the connection both gets, but anything that listens to the send messages will not be able to find the key.
	//The other connection must use the counterpart of this method: generateKey2
	//Blocks until key is shared.
	def generateKey1(c: Connection): Array[Byte] = {
		val rand = new SecureRandom
		val p = generatePrime(512, rand)
		val g = generatePrime(511, rand)//A relative prime to p would do, but a prime number must be relatively prime to anything.
		val a = generateInt(new SecureRandom(rand.generateSeed(64)))
		val a2 = g.modPow(a, p)
		BigIntPacketFormat.send(p, c)
		BigIntPacketFormat.send(g, c)
		BigIntPacketFormat.send(a2, c)
		var b2 = BigIntPacketFormat.blockReceive(c)
		val s = b2.modPow(a, p)
		return s.toByteArray
	}
	//Performs Diffie-Hellman Key exchange with the connection, resulting in this function returning a BigInt that this and the reciever of the connection both gets, but anything that listens to the send messages will not be able to find the key.
	//The other connection must use the counterpart of this method: generateKey1
	//Blocks until key is shared.
	def generateKey2(c: Connection): Array[Byte] = {
		val rand = new SecureRandom
		val p = BigIntPacketFormat.blockReceive(c)
		val g = BigIntPacketFormat.blockReceive(c)
		val b = generateInt(new SecureRandom(rand.generateSeed(64)))
		val b2 = g.modPow(b, p)
		BigIntPacketFormat.send(b2, c)
		val a2 = BigIntPacketFormat.blockReceive(c)
		val s = a2.modPow(b, p)
		return s.toByteArray
	}

	class RSAInputStream(in: InputStream, prikey: RSAPrivateKey) extends InputStream {
		private var buf = new Array[Byte](0)
		private var offset = 0
		//Recieves so there is atleast min items in the buffer
		private def recieve(min: Int): Int = {
			if (buf.length-offset-min < buf.length) {
				val baos = new java.io.ByteArrayOutputStream
				if (offset != buf.length) {
					baos.write(buf, offset, buf.length-offset)
				}
				while (baos.size < min) {
					val lbytes = new Array[Byte](4)
					val lbr = in.read(lbytes)
					if (lbr != 4) {//No more data
						buf = baos.toByteArray
						baos.close
						offset = 0
						return buf.length
					}
					val len = ((0xff & lbytes(0)) << 24) + ((0xff & lbytes(1)) << 16) + ((0xff & lbytes(2)) << 8) + (lbytes(3) & 0xff)
					val chunkbytes = new Array[Byte](len)
					val cbl = in.read(chunkbytes)
					if (cbl != len)
						throw new java.io.IOException("Unexpected EOF: In the middle of RSA block")
					val rawbytes = prikey.decrypt(if (cbl == len) {chunkbytes} else {Arrays.copyOfRange(chunkbytes, 0, cbl)})
					baos.write(rawbytes)
				}
				offset = 0
				buf = baos.toByteArray
				baos.close
			}
			return buf.length
		}
		override
		def available: Int = {
			val x = Math.max(buf.length - offset, 0)
			if (x == 0) {
				if (in.available > 0) {
					return 1
				}
			}
			return x
		}
		override
		def close = in.close
		override
		def read: Int = {
			recieve(1)
			if (offset == buf.length)//No more data
				return -1
			val byte = buf(offset)
			offset += 1
			return byte
		}
		override
		def read(b: Array[Byte]) = read(b, 0, b.length)
		override
		def read(b: Array[Byte], off: Int, len: Int): Int = {
			val l = recieve(len)
			println(l + " " + offset)
			if (l == 0)
				return -1
			val target = Math.min(offset+len, l)
			val canread = Math.max(target-offset,0)
			for (i <- offset until target) {
				b(i-offset+off) = buf(i)
			}
			offset = target
			return canread
		}
		override
		def skip(n: Long): Long = {
			if (offset+n > buf.length) {
				var c: Long = 0
				while (c < n) {
					val lbytes = new Array[Byte](4)
					val lbr = in.read(lbytes)
					if (lbr != 4) {//No more data
						return c + lbr
					}
					val len = ((0xff & lbytes(0)) << 24) + ((0xff & lbytes(1)) << 16) + ((0xff & lbytes(2)) << 8) + (lbytes(3) & 0xff)
					val chunkbytes = new Array[Byte](len)
					val cbl = in.read(chunkbytes)
					if (cbl != len)
						throw new java.io.IOException("Unexpected EOF: In the middle of RSA block")
					val rawbytes = prikey.decrypt(if (cbl == len) {chunkbytes} else {Arrays.copyOfRange(chunkbytes, 0, cbl)})
					c += rawbytes.length
					if (c >= n) {
						if (c == n) {
							buf = new Array(0)
							offset = 0
						} else {
							buf = Arrays.copyOfRange(rawbytes, (rawbytes.length-c-n).intValue, rawbytes.length)
						}
					}
				}
			} else {
				offset = (offset+n).intValue
			}
			return n
		}
	}
	class RSAOutputStream(out: OutputStream, pubkey: RSAPublicKey) extends OutputStream {
		private val buf = new Array[Byte](pubkey.getBlockSize)
		private var offset = 0

		def sendBuffer {
			if (offset > 0) {
				val encrypted = pubkey.encrypt(Arrays.copyOfRange(buf, 0, offset))
				val l = encrypted.length
				val lb = new Array[Byte](4)
				lb(0) = (l >>> 24).byteValue
				lb(1) = (l >>> 16).byteValue
				lb(2) = (l >>> 8).byteValue
				lb(3) = l.byteValue
				out.write(lb)
				out.write(encrypted)
				offset = 0
			}
		}
		override
		def close {
			sendBuffer
			out.close
		}
		override
		def flush {
			sendBuffer
			out.flush
		}
		override
		def write(b: Int) {
			if (offset >= pubkey.getBlockSize)
				sendBuffer
			buf(offset) = b.byteValue
			offset += 1
		}
		override
		def write(b: Array[Byte]) {
			write(b, 0, b.length)
		}
		override
		def write(b: Array[Byte], off: Int, len: Int) {
			for (i <- off until len) {
				buf(offset) = b(i)
				offset += 1
				if (offset >= pubkey.getBlockSize)
					sendBuffer
			}
		}
	}
	object RSA {
		def generateKeypair(bits: Int): (RSAPublicKey, RSAPrivateKey) = {
			val gen = KeyPairGenerator.getInstance("RSA")
			gen.initialize(bits)
			val keypair = gen.generateKeyPair()
			(new RSAPublicKey(keypair.getPublic.asInstanceOf[java.security.interfaces.RSAPublicKey]), new RSAPrivateKey(keypair.getPrivate.asInstanceOf[java.security.interfaces.RSAPrivateKey]))
		}
	}

	class RSAPublicKey(private val pk: java.security.interfaces.RSAPublicKey) {
		private val cipher = javax.crypto.Cipher.getInstance("RSA")
		private var cipherinited = false

		def encrypt(data: Array[Byte]) = {
			if (data.length > getBlockSize)
				throw new IllegalArgumentException("data longer than " + getBlockSize + " bytes")
			if (!cipherinited) {
				cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, pk)
				cipherinited = true
			}
			cipher.doFinal(data)
		}
		def getBlockSize = pk.getModulus.bitCount/4-11
		def getBytes: Array[Byte] = pk.getEncoded
		def writeTo(out: OutputStream) {
			val pubkeybytes = getBytes
			val pkbl = pubkeybytes.length
			val pkblb = new Array[Byte](4)
			pkblb(0) = (pkbl >>> 24).byteValue
			pkblb(1) = (pkbl >>> 16).byteValue
			pkblb(2) = (pkbl >>> 8).byteValue
			pkblb(3) = pkbl.byteValue
			out.write(pkblb)
			out.write(pubkeybytes)
		}
	}
	object RSAPublicKey {
		def fromBytes(bytes: Array[Byte]) = {
			val keyFactory = KeyFactory.getInstance("RSA")
			val publicKeySpec = new java.security.spec.X509EncodedKeySpec(bytes)
			new RSAPublicKey(keyFactory.generatePublic(publicKeySpec).asInstanceOf[java.security.interfaces.RSAPublicKey])
		}
		def readFrom(in: InputStream) = {
			val lbytes = new Array[Byte](4)
			in.read(lbytes)
			val len = ((0xff & lbytes(0)) << 24) + ((0xff & lbytes(1)) << 16) + ((0xff & lbytes(2)) << 8) + (lbytes(3) & 0xff)
			val bytes = new Array[Byte](len)
			in.read(bytes)
			fromBytes(bytes)
		}
	}
	class RSAPrivateKey(private val pk: java.security.interfaces.RSAPrivateKey) {
		private val cipher = javax.crypto.Cipher.getInstance("RSA")
		private var cipherinited = false

		def decrypt(data: Array[Byte]) = {
			if (!cipherinited) {
			cipher.init(javax.crypto.Cipher.DECRYPT_MODE, pk);
				cipherinited = true
			}
			cipher.doFinal(data)
		}
		def getBlockSize = pk.getModulus.bitCount/4-11
		def getBytes: Array[Byte] = pk.getEncoded
		def writeTo(out: OutputStream) {
			val prikeybytes = getBytes
			val pkbl = prikeybytes.length
			val pkblb = new Array[Byte](4)
			pkblb(0) = (pkbl >>> 24).byteValue
			pkblb(1) = (pkbl >>> 16).byteValue
			pkblb(2) = (pkbl >>> 8).byteValue
			pkblb(3) = pkbl.byteValue
			out.write(pkblb)
			out.write(prikeybytes)
		}
	}
	object RSAPrivateKey {
		def fromBytes(bytes: Array[Byte]) = {
			val keyFactory = KeyFactory.getInstance("RSA")
			val privateKeySpec = new java.security.spec.PKCS8EncodedKeySpec(bytes)
			new RSAPrivateKey(keyFactory.generatePrivate(privateKeySpec).asInstanceOf[java.security.interfaces.RSAPrivateKey])
		}
		def readFrom(in: InputStream) = {
			val lbytes = new Array[Byte](4)
			in.read(lbytes)
			val len = ((0xff & lbytes(0)) << 24) + ((0xff & lbytes(1)) << 16) + ((0xff & lbytes(2)) << 8) + (lbytes(3) & 0xff)
			val bytes = new Array[Byte](len)
			in.read(bytes)
			fromBytes(bytes)
		}
	}

}