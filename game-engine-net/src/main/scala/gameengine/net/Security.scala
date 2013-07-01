package gameengine.net

import java.math.BigInteger
import java.util.Random
import java.security.SecureRandom

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
	final class XOREncryptedInputStream(in: java.io.InputStream, key: Array[Byte]) extends java.io.InputStream {
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
	final class XOREncryptedOutputStream(out: java.io.OutputStream, key: Array[Byte]) extends java.io.OutputStream {
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
}
