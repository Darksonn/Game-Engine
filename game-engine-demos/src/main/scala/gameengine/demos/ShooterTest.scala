package gameengine.demos

import gameengine._
import gameengine.styles.{ImperativeControlStyle, EventInputStyle, Game}
import java.io.File

object ShooterTest extends Game with EventInputStyle with ImperativeControlStyle {

	val datafile = new File(new File(System.getProperty("java.io.tmpdir")), "shooter.data")
	println("Data file located at: " + datafile.getAbsolutePath)

	val width = 640
	val height = 480
	val title = "Shooter test"
	val turnspd = 0.06
	val movespd = 1.0

	val defents = Seq[Entity]()
	var entities = defents
	var entitiesToRemove = Seq[Entity]()
	var entitiesToAdd = Seq[Entity]()
	var lp = false
	var rp = false
	var fwp = false
	var bwp = false
	var paused = false
	var shotCooldown = 0l
	var gamestate = -1
	var mobSpawnCountdown = 0
	var mobSpawnCountdownMax = 2000
	var kills = 0l
	var bestKills = 0l

	object Player {
		var pos = new Vector2D(width/2d, height/2d)
		var dir = 0d
		var hp = 20
		val mhp = 20
		def shoot {
			addEntity(new PlBullet(pos, Vector2D.polar(dir, 3)))
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
							out.drawFilledRect(Color.white)
							out.withTranslation(0.5,0.25) {
								out.withScaling(0.5,0.5) {
									out.drawFilledRect(Color.red)
								}
							}
						}
					}
				}
			}
		}
	}

	def saveScores {
		val out = new java.io.DataOutputStream(new java.io.FileOutputStream(datafile))
		bestKills = if (bestKills > kills) {bestKills} else {kills}
		out.writeLong(bestKills)
		out.writeLong(kills)
		out.close
	}
	def loadScores {
		if (datafile.exists) {
			val in = new java.io.DataInputStream(new java.io.FileInputStream(datafile))
			bestKills = in.readLong
			kills = in.readLong
			in.close
		} else {
			bestKills = 0
			kills = 0
		}
	}

	def removeEntity(e: Entity) {
		entitiesToRemove = entitiesToRemove :+ e
	}
	def addEntity(e: Entity) {
		entitiesToAdd = entitiesToAdd :+ e
	}

	override def update(in: Input) = {
		gamestate match {
			case -2 =>
				saveScores
				gamestate = 0
			case -1 =>
				loadScores
				gamestate = 0
			case 0 =>
				for (event <- in.queue) {
					event match {
						case KeyDownEvent(Key.KeyboardKey(27)) =>//esc
							quit()
						case KeyDownEvent(_) =>
							gamestate = 1
							Player.hp = Player.mhp
							entities = defents
							lp = false
							rp = false
							fwp = false
							bwp = false
							mobSpawnCountdown = 0
							kills = 0
						case _ => Unit
					}
				}
			case 1 =>
				if (paused) {
					for (event <- in.queue) {
						event match {
							case KeyDownEvent(key) =>
								key match {
									case Key.KeyboardKey(37) =>//right
										rp = true
									case Key.KeyboardKey(38) =>//up
										fwp = true
									case Key.KeyboardKey(39) =>//left
										lp = true
									case Key.KeyboardKey(40) =>//down
										bwp = true
									case Key.KeyboardKey(80) =>//p
										paused = !paused
									case _ => Unit
									}
							case KeyUpEvent(key) =>	
								key match {
									case Key.KeyboardKey(37) =>//right
										rp = false
									case Key.KeyboardKey(38) =>//up
										fwp = false
									case Key.KeyboardKey(39) =>//left
										lp = false
									case Key.KeyboardKey(40) =>//down
										bwp = false
									case _ => Unit
								}
							case _ => Unit
						}
					}
				} else {
					mobSpawnCountdown -= 1
					if (mobSpawnCountdown <= 0) {
						val r = new java.util.Random
						addEntity(new Mob(Vector2D(r.nextDouble * width, r.nextDouble * height), 4 + r.nextInt(2)))
						mobSpawnCountdownMax = Math.max(750, mobSpawnCountdownMax-80)
						mobSpawnCountdown = mobSpawnCountdownMax
					}
					if (Player.hp <= 0)
						gamestate = -2
					for (e <- entities) {
						e.update
					}
					if (lp)
						Player.turnCounterClockwise(turnspd)
					if (rp)
						Player.turnClockwise(turnspd)
					if (fwp)
						Player.move(movespd)
					if (bwp)
						Player.move(movespd * (-7d) / 10d)
					for (event <- in.queue) {
						event match {
							case TimePassEvent(ns) =>
								shotCooldown = if (shotCooldown < ns) {0} else {shotCooldown - ns}
							case KeyDownEvent(key) =>
								key match {
									case Key.KeyboardKey(27) =>//esc
										gamestate = -2
									case Key.KeyboardKey(32) =>//space
										if (shotCooldown <= 0) {
											Player.shoot
											shotCooldown = 200000000l
										}
									case Key.KeyboardKey(37) =>//right
										rp = true
									case Key.KeyboardKey(38) =>//up
										fwp = true
									case Key.KeyboardKey(39) =>//left
										lp = true
									case Key.KeyboardKey(40) =>//down
										bwp = true
									case Key.KeyboardKey(80) =>//p
										paused = !paused
									case Key.KeyboardKey(83) =>//s
										mobSpawnCountdown = 0
									case _ => Unit
								}
							case KeyUpEvent(key) =>	
								key match {
									case Key.KeyboardKey(37) =>//right
										rp = false
									case Key.KeyboardKey(38) =>//up
										fwp = false
									case Key.KeyboardKey(39) =>//left
										lp = false
									case Key.KeyboardKey(40) =>//down
										bwp = false
									case _ => Unit
								}
							case _ => Unit
						}
					}
					if (entitiesToRemove.size > 0) {
						entities = entities diff entitiesToRemove
						entitiesToRemove = Seq[Entity]()
					}
					if (entitiesToAdd.size > 0) {
						entities = entities ++ entitiesToAdd
						entitiesToAdd = Seq[Entity]()
					}
				}
			case _ => -1
		}
		Seq()
	}
	val menuText = new DrawableText("Press any key to start the game", new java.awt.Font("Monospaced", 12, 1), Color.white)
	val pausedText = new DrawableText("Paused", new java.awt.Font("Monospaced", 12, 1), Color.white)
	override def render(out: Output) {
		out.withScaling(width, height) {
			out.drawFilledRect(Color.black)
		}
		gamestate match {
			case 0 =>
				out.withTranslation(10,20) {
					out.withScaling(20,20) {
						out.draw(new DrawableText("Best score: " + bestKills + " kills", new java.awt.Font("Monospaced", 12, 1), Color.white))
					}
				}
				out.withTranslation(10,50) {
					out.withScaling(20,20) {
						out.draw(new DrawableText("Last score: " + kills + " kills", new java.awt.Font("Monospaced", 12, 1), Color.white))
					}
				}
				out.withTranslation(10,80) {
					out.withScaling(20,20) {
						out.draw(menuText)
					}
				}
			case 1 =>
				out.withTranslation(0, height-3) {
					out.withScaling(width/Player.mhp.doubleValue*Player.hp, 3) {
						out.drawFilledRect(Color.red)
					}
				}
				out.withScaling(width/mobSpawnCountdownMax.doubleValue*mobSpawnCountdown, 3) {
					out.drawFilledRect(Color.white)
				}
				for (e <- entities) {
					e.render(out)
				}
				Player.render(out)
				if (paused) {
					out.withTranslation(width/2d, height/2d) {
						out.withScaling(20,20) {
							out.withTranslation(-3, -0.5) {
								out.draw(pausedText)
							}
						}
					}
				}
			case _ => Unit
		}
	}

	override def on = {
		case CloseRequestedEvent => quit()
	}
	
	trait Entity {
		def pos: Vector2D
		def update
		def render(out: Output)
	}
	class PlBullet(var posi: Vector2D, vel: Vector2D) extends Entity {
		def update = {
			posi = pos + vel
			if (pos.x < 0 || pos.y < 0 || pos.x > width || pos.y > height) {
				removeEntity(this)
			}
		}
		def render(out: Output) {
			out.withTranslation(pos.x, pos.y) {
				out.withScaling(5,5) {
					out.withTranslation(-0.5,-0.5) {
						out.drawFilledCircle(Color.white)
					}
				}
			}
		}
		def pos = posi
	}
	class MbBullet(var posi: Vector2D, vel: Vector2D, str: Int) extends Entity {
		def update = {
			posi = pos + vel
			if (pos.distanceTo(Player.pos) < 5) {
				Player.hp -= str
				removeEntity(this)
			}
			if (pos.x < 0 || pos.y < 0 || pos.x > width || pos.y > height) {
				removeEntity(this)
			}
		}
		def render(out: Output) {
			out.withTranslation(pos.x, pos.y) {
				out.withScaling(5,5) {
					out.withTranslation(-0.5,-0.5) {
						out.drawFilledCircle(Color.red)
					}
				}
			}
		}
		def pos = posi		
	}
	class Mob(var posi: Vector2D, var hp: Int) extends Entity {
		private val mhp = hp
		private var vel: Vector2D = null
		private var moves = 0
		private var cooldown = 30
		private var showHealth = 0
		private val maxShowHealth = 100
		private val r = new java.util.Random
		def newTarget {
			moves = 70 + r.nextInt(61)
			val target = Vector2D(r.nextDouble * width, r.nextDouble * height)
			vel = target.transform(pos).normalize
		}
		def move {
			if (moves <= 0)
				newTarget
			moves -= 1
			posi = pos + vel
			if (pos.isInsideCube(Vector2D(0,0), width, height))
				newTarget
		}
		def update = {
			move
			if (cooldown <= 0) {
				addEntity(new MbBullet(pos, Player.pos.transform(pos).normalize, 1))
				cooldown = 27 + r.nextInt(7)
			} else {
				cooldown -= 1
			}
			showHealth -= 1
			for (e <- entities) {
				if (e.isInstanceOf[PlBullet]) {
					var dist = pos.transform(e.pos).length
					if (dist < 15) {
						removeEntity(e)
						hp -= 1
						showHealth = maxShowHealth
						if (hp <= 0) {
							removeEntity(this)
							kills += 1
						}
					}
				}
			}
		}
		def render(out: Output) {
			out.withTranslation(pos.x, pos.y) {
				out.withScaling(30,30) {
					out.withTranslation(-0.5,-0.5) {
						out.drawFilledCircle(Color.blue)
					}
				}
				out.withScaling(15,15) {
					out.withTranslation(-0.5,-0.5) {
						out.drawFilledCircle(Color.red)
					}
				}
				out.withScaling(10,10) {
					out.withTranslation(-0.5,-0.5) {
						out.drawFilledCircle(Color.orange)
					}
				}
				out.withScaling(5,5) {
					out.withTranslation(-0.5,-0.5) {
						out.drawFilledCircle(Color.white)
					}
				}
				if (showHealth > 0) {
					val l = 30d/mhp*hp
					out.withTranslation(l/(-2), -30) {
						out.withScaling(l, 5) {
							out.drawFilledRect(Color.red)
						}
					}
				}
			}
		}
		def pos = posi
	}
}
