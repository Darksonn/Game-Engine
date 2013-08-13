package gameengine.demos

import gameengine._
import gameengine.styles.{ImperativeControlStyle, EventInputStyle, Game}

object ShooterTest extends Game with EventInputStyle with ImperativeControlStyle {

	val width = 640
	val height = 480
	val title = "Shooter test"
	val turnspd = 0.03
	val movespd = 1.0

	var particles = Seq[Particle]()
	var lp = false
	var rp = false
	var fwp = false

	object Player {
		var pos = new Vector2D(width/2d, height/2d)
		var dir = 0d
		def shoot {
			particles = particles :+ new Particle(pos, Vector2D.polar(dir, 3))
		}
		def turnClockwise(delta: Double) {
			dir = dir - delta
		}
		def turnCounterClockwise(delta: Double) {
			dir = dir + delta
		}
		def move(dist: Double) {
			pos = pos + Vector2D.polar(dir, dist)
		}
		def render(out: Output) {
			out.withTranslation(pos.x, pos.y) {
				out.withRotation(dir) {
					out.withScaling(10,10) {
						out.withTranslation(-0.5,-0.5) {
							out.drawFilledRect(java.awt.Color.WHITE)
							out.withTranslation(0.5,0.25) {
								out.withScaling(0.5,0.5) {
									out.drawFilledRect(java.awt.Color.RED)
								}
							}
						}
					}
				}
			}
		}
	}

	override def update(in: Input) = {
		var particlesToRemove = Seq[Particle]()
		for (p <- particles) {
			if (p.update) {
				particlesToRemove = particlesToRemove :+ p
			}
		}
		if (particlesToRemove.size > 0)
			particles = particles diff particlesToRemove
		if (lp)
			Player.turnCounterClockwise(turnspd)
		if (rp)
			Player.turnClockwise(turnspd)
		if (fwp)
			Player.move(movespd)
		for (event <- in.queue) {
			event match {
				case KeyDownEvent(key) =>
					key match {
						case Key.KeyboardKey(32) =>
							Player.shoot
						case Key.KeyboardKey(37) =>
							rp = true
						case Key.KeyboardKey(38) =>
							fwp = true
						case Key.KeyboardKey(39) =>
							lp = true
						case _ => Unit
					}
				case KeyUpEvent(key) =>	
					key match {
						case Key.KeyboardKey(37) =>
							rp = false
						case Key.KeyboardKey(38) =>
							fwp = false
						case Key.KeyboardKey(39) =>
							lp = false
						case _ => Unit
					}
				case _ => Unit
			}
		}
		Seq()
	}
	override def render(out: Output) {
		out.withScaling(width, height) {
			out.drawFilledRect(java.awt.Color.BLACK)
		}
		for (p <- particles) {
			p.render(out)
		}
		Player.render(out)
	}

	override def on = {
		case CloseRequestedEvent => quit()
	}
	
	class Particle(var pos: Vector2D, vel: Vector2D) {
		def update = {
			pos = pos + vel
			(pos.x < 0 || pos.y < 0 || pos.x > width || pos.y > height)
		}
		def render(out: Output) {
			out.withTranslation(pos.x, pos.y) {
				out.withScaling(5,5) {
					out.withTranslation(-0.5,-0.5) {
						out.drawFilledCircle(java.awt.Color.WHITE)
					}
				}
			}
		}
	}
}
