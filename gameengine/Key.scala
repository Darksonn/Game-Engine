sealed trait Key
object MiddleMouse extends Key
object LeftMouse extends Key
object RightMouse extends Key
case class KeyboardKey(code: Int) extends Key