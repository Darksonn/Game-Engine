package gameengine.demos

import gameengine._
import gameengine.styles.{ImperativeControlStyle, Game, EventInputStyle}

object GravityTest extends Game with EventInputStyle with ImperativeControlStyle {

	val width = 640
	val height = 480
	val title = "Attracted particles!"

	var particles = Seq[Particle]()
	var mouse = Point(0,0)

	override def update(in: Input) = {
		for (p <- particles) {
			val particles2 = particles.diff(Seq(p))
			p.update(particles2)
			if (p.outsideBounds(width, height))
				particles = particles2
		}
		for (event <- in.queue) {
			event match {
				case KeyDownEvent(key) =>
					key match {
						case Key.LeftMouse =>
							particles = particles :+ Particle(mouse.toVector)
						case _ => Unit
					}
				case MouseMoveEvent(to) => mouse = to
				case _ => Unit
			}
		}
		Seq()
	}
	override def render(out: Output) {
		import out._
		withScaling(width, height) {
			drawFilledRect(java.awt.Color.BLACK)
		}
		for (p <- particles) {
			drawFilledRect(p.pos.x, p.pos.y, 3, 3, java.awt.Color.WHITE)
		}
	}

	override val on: PartialFunction[InputEvent, Unit] = {
		case CloseRequestedEvent => quit()
	}

	class Particle(var pos: Vector2D) {
		var movement = Vector2D(0,0)
		def update(particles: Seq[Particle]) {
			for (p <- particles) {
				val transformed = p.pos.transform(pos)
				val strength = Particle.gravityConstant / transformed.lengthSquared
				movement += transformed.normalize * strength
			}
			pos += movement
		}
		def outsideBounds(width: Int, height: Int) = pos.x < 0 || pos.x > width || pos.y > height || pos.y < 0
	}
	object Particle {
		val gravityConstant = 10
		def apply(pos: Vector2D) = new Particle(pos)
	}

}