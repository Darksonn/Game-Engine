package gameengine.demos

// to use LWJGL instead of swing, simply use LWJGLGame instead of BaseGame (or in conjunction with it)
object TextNImageTestLWJGL extends TextNImageTest with gameengine.lwjgl.LWJGLGame
object AvoidTheTopGameLWJGL extends AvoidTheTopGame with gameengine.lwjgl.LWJGLGame
object EventPrinterLWJGL extends EventPrinter with gameengine.lwjgl.LWJGLGame