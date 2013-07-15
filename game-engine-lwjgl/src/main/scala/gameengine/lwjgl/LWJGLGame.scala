package gameengine.lwjgl

import gameengine.BaseGame
import java.io._
import java.util.zip.{ZipEntry, ZipInputStream}

trait LWJGLGame extends BaseGame {


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
			var extractThisFile = false
			if (true) {
				val regex = ("lwjgl[-]platform[-][0-9]*(\\.[0-9]*)+[-]natives[-]" + getPlatform + "\\.jar").r
				val check = regex findFirstIn jarName
				check match {
					case None => Unit
					case Some(_) => extractThisFile = true
				}
			}
			if (true) {
				val regex = ("jinput[-]platform[-][0-9]*(\\.[0-9]*)+[-]natives[-]" + getPlatform + "\\.jar").r
				val check = regex findFirstIn jarName
				check match {
					case None => Unit
					case Some(_) => extractThisFile = true
				}
			}
			if (jarName.startsWith("jinput-platform-")) {
				val cut = jarName.substring(16)
				if (cut == ("-natives-" + getPlatform + ".jar")) {
					extractThisFile = true
				}
			}
			if (extractThisFile) {
				libs = libs ++ extract(jar, tempDir)
			}
		}
		for (lib <- libs) {
			try {
				System.load(lib.getAbsolutePath)
				println(lib)
			} catch {
				case ule: java.lang.UnsatisfiedLinkError => Unit//We loaded the one for 64 bit if this jvm is 32 or the other way around, we try loading both so ignore.
			}
		}
		System.setProperty("java.library.path", System.getProperty("java.library.path") + ";" + tempDir.getAbsolutePath)
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
