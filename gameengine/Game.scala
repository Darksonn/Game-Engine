package gameengine

trait Game {
	
	def step(in: Input): Maybe[ControlUpdate]
	def render(gfx: Output): Unit

}

trait ControlUpdate
object ControlUpdate {
	object None extends ControlUpdate
	object Quit extends ControlUpdate
}

trait Input {
	def closeRequested: Boolean
}
trait Output {
	
}