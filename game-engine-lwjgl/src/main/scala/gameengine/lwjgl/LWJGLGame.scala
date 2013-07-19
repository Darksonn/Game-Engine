package gameengine.lwjgl

import gameengine.BaseGame
import java.io._
import java.util.zip.{ZipEntry, ZipInputStream}

object LWJGLLoader {

	var isLoaded = false

	private val loadOrder32win = Seq("OpenAL32.dll", "jinput-raw.dll", "jinput-dx8.dll", "lwjgl.dll")
	private val loadOrder64win = Seq("OpenAL64.dll", "jinput-raw_64.dll", "jinput-dx8_64.dll", "lwjgl64.dll")
	private val loadOrder32linux = Seq("libopenal.so", "libjinput-linux.so", "liblwjgl.so")
	private val loadOrder64linux = Seq("libopenal64.so", "libjinput-linux64.so", "liblwjgl64.so")
	private val loadOrderosx = Seq("openal.dylib", "libjinput-osx.jnilib", "liblwjgl.jnilib")


	def load = {
		if (!isLoaded) {
			var tmpDirproperty = System.getProperty("java.io.tmpdir")
			if (tmpDirproperty == null)
				tmpDirproperty = "."
			val tempDir = new File(new File(tmpDirproperty), "gameengine-lwjgl-lib-temp")
			extractLibs(tempDir)
			System.setProperty("org.lwjgl.librarypath", tempDir.getAbsolutePath)
			for (load <- getLoadOrder) {
				System.load(new File(tempDir, load).getAbsolutePath)
			}
		}
	}

	private def extractLibs(tempDir: File) = {
		javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName())
		val urls = Thread.currentThread.getContextClassLoader.asInstanceOf[java.net.URLClassLoader].getURLs
		tempDir.mkdirs
		val loadOrder = getLoadOrder
		for (url <- urls) {
			val jar = new File(url.toURI)
			val jarName = jar.getName
			val regex = ("(jinput|lwjgl)[-]platform[-][0-9]*(\\.[0-9]*)+[-]natives[-]" + getPlatform + "\\.jar").r
			val check = regex findFirstIn jarName
			if (check.nonEmpty)
				extract(jar, tempDir, loadOrder)
		}
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
	private def extract(jarFile: File, extractLocation: File, names: Seq[String]): Seq[File] = {
		var files = Seq[File]()
		val zinstream = new ZipInputStream(new FileInputStream(jarFile))
		var zentry = zinstream.getNextEntry
		var buf = new Array[Byte](1024)
		while (zentry != null) {
			val entryName = zentry.getName
			if (names.contains(entryName)) {
				val entryFile = new File(extractLocation, entryName)
				if (entryFile.exists) {
					entryFile.delete
				}
				if (!entryFile.exists) {//If delete didn't delete, we assume it is in use
					entryFile.createNewFile
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

trait LWJGLGame extends BaseGame {

	override def main(args: Array[String]) = {
		LWJGLLoader.load
		impl.run(this)
	}

	
}
