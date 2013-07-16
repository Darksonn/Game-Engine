package gameengine.lwjgl

import gameengine.BaseGame
import java.io._
import java.util.zip.{ZipEntry, ZipInputStream}

trait LWJGLGame extends BaseGame {

	private val loadOrder32win = Seq("OpenAL32.dll", "jinput-raw.dll", "jinput-dx8.dll", "lwjgl.dll")
	private val loadOrder64win = Seq("OpenAL64.dll", "jinput-raw_64.dll", "jinput-dx8_64.dll", "lwjgl64.dll")
	private val loadOrder32linux = Seq("libopenal.so", "libjinput-linux.so", "liblwjgl.so")
	private val loadOrder64linux = Seq("libopenal64.so", "libjinput-linux64.so", "liblwjgl64.so")
	private val loadOrderosx = Seq("openal.dylib", "libjinput-osx.jnilib", "liblwjgl.jnilib")

	override def main(args: Array[String]) = {
		val urls = Thread.currentThread.getContextClassLoader.asInstanceOf[java.net.URLClassLoader].getURLs//.filter(Seq("jinput-dx8_64.dll", "jinput-dx8.dll", "jinput-raw_64.dll", "jinput-raw.dll", "lwjgl.dll", "lwjgl64.dll", "OpenAL32.dll", "OpenAL64.dll") contains _)
		val temp = File.createTempFile("xxx", "xxx")
		temp.delete
		val tempDir = new File(temp.getParentFile, "temp" + randomName)
		tempDir.mkdirs
		var libs = Seq[File]()
		for (url <- urls) {
			val jar = new File(url.toURI)
			val jarName = jar.getName
			val regex = ("(jinput|lwjgl)[-]platform[-][0-9]*(\\.[0-9]*)+[-]natives[-]" + getPlatform + "\\.jar").r
			val check = regex findFirstIn jarName
			if (check.nonEmpty)
				libs = libs ++ extract(jar, tempDir)
		}
		System.setProperty("org.lwjgl.librarypath", tempDir.getAbsolutePath)
		/*val sysPathsField = classOf[ClassLoader].getDeclaredField("sys_paths")
		sysPathsField.setAccessible(true)
		sysPathsField.set(null, null)*/
		for (load <- getLoadOrder) {
			System.load(new File(tempDir, load).getAbsolutePath)
		}
		Runtime.getRuntime().addShutdownHook(new Thread {
			override def run() {
				for (lib <- libs) {
					lib.delete
				}
				tempDir.delete
			}
		})
		impl.run(this)
	}

	private def getLoadOrder = {
		getPlatform match {
			case "linux" => 
				val bit = System.getProperty("sun.arch.data.model")
				if (bit == "64") loadOrder64linux
				else loadOrder32linux
			case "windows" => 
				val bit = System.getProperty("sun.arch.data.model")
				if (bit == "64") loadOrder64win
				else loadOrder32win
			case "osx" => loadOrderosx
			case _ => loadOrder32linux
		}
	}
	private def randomName(): String = (new java.util.Random).nextLong.toHexString
	private def extract(jarFile: File, extractLocation: File): Seq[File] = {
		var files = Seq[File]()
		val zinstream = new ZipInputStream(new FileInputStream(jarFile))
		var zentry = zinstream.getNextEntry
		var buf = new Array[Byte](1024)
		while (zentry != null) {
			val entryName = zentry.getName
			if (!entryName.contains("META-INF")) {
				val entryFile = new File(extractLocation, entryName)
				files = files :+ entryFile
				val outstream = new FileOutputStream(entryFile)
				var n = zinstream.read(buf, 0, 1024)
				while (n > -1) {
					outstream.write(buf, 0, n)
					n = zinstream.read(buf, 0, 1024)
				}
				outstream.close
				zinstream.closeEntry
			}
			zentry = zinstream.getNextEntry
		}
		zinstream.close
		files
	}

	private def getPlatform(): String = {
		val osName = System.getProperty("os.name").toLowerCase()
		if (osName.contains("linux")) return "linux"
		if (osName.contains("unix")) return "linux"
		if (osName.contains("solaris")) return "linux"
		if (osName.contains("sunos")) return "linux"
		if (osName.contains("win")) return "windows"
		if (osName.contains("mac")) return "osx"
		throw new RuntimeException("Unknown OS")
	}
	
}
