trait Game {
	
	def step(): Maybe[ControlUpdate]
	def render(gfx: Graphics): Unit

}