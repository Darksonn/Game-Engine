package gameengine.net

import java.math.BigInteger
import java.util.Random

object SecurityUtils {
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
	//Performs Diffie-Hellman Key exchange with the connection, resulting in this function returning a BigInt that this and the reciever of the connection both gets, but anything that listens to the send messages will not be able to find the key.
	//The other connection must use the counterpart of this method: generateKey2
	//Blocks until key is shared.
	def generateKey1(c: Connection): BigInt = {
		val rand = new java.security.SecureRandom
		val p = generatePrime(512, rand)
		val g = generatePrime(511, rand)
		val a = generateInt(new java.security.SecureRandom(rand.generateSeed(64)))
		val a2 = g.modPow(a, p)
		BigIntPacketFormat.send(p, c)
		BigIntPacketFormat.send(g, c)
		BigIntPacketFormat.send(a2, c)
		var b2 = BigIntPacketFormat.blockReceive(c)
		val s = b2.modPow(a, p)
		return s
	}
	//Performs Diffie-Hellman Key exchange with the connection, resulting in this function returning a BigInt that this and the reciever of the connection both gets, but anything that listens to the send messages will not be able to find the key.
	//The other connection must use the counterpart of this method: generateKey1
	//Blocks until key is shared.
	def generateKey2(c: Connection): BigInt = {
		val rand = new java.security.SecureRandom
		val p = BigIntPacketFormat.blockReceive(c)
		val g = BigIntPacketFormat.blockReceive(c)
		val b = generateInt(new java.security.SecureRandom(rand.generateSeed(64)))
		val b2 = g.modPow(b, p)
		BigIntPacketFormat.send(b2, c)
		val a2 = BigIntPacketFormat.blockReceive(c)
		val s = a2.modPow(b, p)
		return s
	}
}
