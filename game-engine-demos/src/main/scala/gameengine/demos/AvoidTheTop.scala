package gameengine.demos

import gameengine._
import gameengine.styles.{ImperativeControlStyle, Game, EventInputStyle}
import java.io.File

object AvoidTheTopGame extends Game with EventInputStyle with ImperativeControlStyle {

	val datafile = new File(new File(System.getProperty("user.home", ".")), "avoidthetop.data")

	val width = 640
	val height = 480
	val title = "Avoid the top!"

	var player = Vector2D(width/2, 20)
	val r = new java.util.Random
	var platforms = Seq[Platform]()
	var rightPressed = false
	var leftPressed = false
	var paused = false
	var count = 0
	var startTime: Long = -1
	var gamestate = -1
	var maxScore: Long = 0
	var lastScore: Long = 0

	override def update(in: Input) = {
		gamestate match {
			case -1 =>
				if (datafile.exists) {
					val in = new java.io.DataInputStream(new java.io.FileInputStream(datafile))
					maxScore = in.readLong
					lastScore = in.readLong
					in.close
				}
				gamestate = 0
			case 0 =>
				for (event <- in.queue) {
					event match {
						case KeyDownEvent(key) =>
							key match {
								case Key.LeftMouse =>
									gamestate = 1
								case _ => Unit
							}
						case _ => Unit
					}
				}
			case 1 => 
				if (startTime == -1) {
					startTime = System.currentTimeMillis
				}
				for (event <- in.queue) {
					event match {
						case KeyDownEvent(key) =>
							key match {
								case Key.KeyboardKey(code) =>
									code match {
										case 37 => leftPressed = true
										case 39 => rightPressed = true
										case 80 => paused = !paused
										case _ => Unit
									}
								case _ => Unit
							}
						case KeyUpEvent(key) =>
							key match {
								case Key.KeyboardKey(code) =>
									code match {
										case 37 => leftPressed = false
										case 39 => rightPressed = false
										case _ => Unit
									}
								case _ => Unit
							}
						case _ => Unit
					}
				}
				if (!paused) {
					if (rightPressed) player = player.changeX(5)
					if (leftPressed) player = player.changeX(-5)

					if (player.y < height-20) player = player.changeY(2)
					var newPlatforms = platforms
					for (p <- platforms) {
						if (p.removeMe) newPlatforms = newPlatforms diff Seq(p)
						if (p.touches(player)) player = player.changeY(-3)
						p.update
					}
					platforms = newPlatforms
					if (count % (30 * (1.0 / Platform.platformSpeed)) == 0) {
						platforms = platforms :+ Platform(r.nextInt(width-Platform.holeWidth-10), height)
					}
					count += 1
					if (player.y < 0) {
						val score = System.currentTimeMillis - startTime
						startTime = -1
						gamestate = -1
						rightPressed = false
						leftPressed = false
						platforms = Seq[Platform]()
						player = Vector2D(width/2, 20)
						count = 0

						val out = new java.io.DataOutputStream(new java.io.FileOutputStream(datafile))
						out.writeLong(if (maxScore > score) {maxScore} else {score})
						out.writeLong(score)
						out.close
					}
				}
		}
		Seq()
	}
	override def render(out: Output) {
		import out._
		withScaling(width, height) {
			drawFilledRect(java.awt.Color.BLACK)
		}
		gamestate match {
			case 0 =>
				withTranslation(10,20) {
					withScaling(20,20) {
						draw(new DrawableText("Best score: " + (maxScore/1000.0) + " seconds", new java.awt.Font("Monospaced", 12, 1), java.awt.Color.WHITE))
					}
				}
				withTranslation(10,80) {
					withScaling(20,20) {
						draw(new DrawableText("Click anywhere to play", new java.awt.Font("Monospaced", 12, 1), java.awt.Color.WHITE))
					}
				}
				withTranslation(10,50) {
					withScaling(20,20) {
						draw(new DrawableText("Last score: " + (lastScore/1000.0) + " seconds", new java.awt.Font("Monospaced", 12, 1), java.awt.Color.WHITE))
					}
				}
			case 1 =>
				for (p <- platforms) {
					drawFilledRect(0, p.y, p.hole, Platform.platformHeight, java.awt.Color.WHITE)
					drawFilledRect(p.hole+Platform.holeWidth, p.y, width-p.hole+Platform.holeWidth, Platform.platformHeight, java.awt.Color.WHITE)
				}
				drawFilledRect(player.x, player.y-4, 3, 3, java.awt.Color.RED)
			case _ => Unit
		}
	}

	override val on: PartialFunction[InputEvent, Unit] = {
		case CloseRequestedEvent => quit()
	}

	class Platform(val hole: Double, var y: Double) {

		def update() {
			y -= Platform.platformSpeed
		}
		def removeMe = y < -Platform.platformHeight
		def touches(pos: Vector2D): Boolean = {
			if (pos.y > y) {
				if (pos.y-3 < y) {
					if (pos.x > hole) {
						if (pos.x < hole+Platform.holeWidth) {
							return false
						} else {
							return true
						}
					} else {
						return true
					}
				}
			}
			return false
		}

	}
	object Platform {
		val platformSpeed = 0.75
		val holeWidth = 20
		val platformHeight = 10
		def apply(hole: Double, y: Double) = new Platform(hole, y)
	}

}
