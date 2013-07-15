package gameengine.lwjgl

import gameengine.BaseGame

trait LWJGLGame extends BaseGame {
	
	override def main(args: Array[String]) = {
		println(System.getProperty("java.library.path"))
		impl.run(this)
	}
	
}