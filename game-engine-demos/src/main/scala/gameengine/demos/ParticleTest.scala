package gameengine.demos

import gameengine._
import gameengine.styles.{ImperativeControlStyle, Game, EventInputStyle}

object FallingParticlesTest extends Game with EventInputStyle with ImperativeControlStyle {

	val width = 640
	val height = 480
	val title = "Falling particles!"

	var particles = Seq[Particle]()
	val particleCount = 50
	val r = new java.util.Random

	private def addParticles(amount: Int) {
		var toAdd = Seq[Particle]()
		for (i <- 0 until amount) {
			toAdd = toAdd :+ (Particle(Vector2D(r.nextGaussian*width, 0)))
		}
		particles = particles ++ toAdd
	}

	override def update(in: Input) = {
		for (p <- particles) {
			p.update
			if (p.outsideBounds(width, height))
				particles = particles.diff(Seq(p))
		}
		addParticles(r.nextInt(particleCount))
		Seq()
	}
	override def render(out: Output) {
		import out._
		withScaling(width, height) {
			drawFilledRect(Color.black)
		}
		for (p <- particles) {
			drawFilledRect(p.pos.x, p.pos.y, 3, 3, Color.white)
		}
	}

	override val on: PartialFunction[InputEvent, Unit] = {
		case CloseRequestedEvent => quit()
	}

	class Particle(var pos: Vector2D) {
		var gravity = 0.0
		def update() {
			gravity += Particle.gravityConstant
			pos = Vector2D(pos.x, pos.y+gravity)
		}
		def outsideBounds(width: Int, height: Int) = pos.x < 0 || pos.x > width || pos.y > height
	}
	object Particle {
		val gravityConstant = 0.3
		def apply(pos: Vector2D) = new Particle(pos)
	}

}
