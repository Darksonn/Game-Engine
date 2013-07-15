package gameengine.demos

import gameengine._
import gameengine.styles.{ImperativeControlStyle, Game, EventInputStyle}

object TowerDefence extends Game with EventInputStyle with ImperativeControlStyle {

	val width = 640
	val height = 480
	val title = "Tower Defence"

	val tilesX = 32
	val tilesY = 20
	val tileAmount = tilesX * tilesY
	val tileWidth = width/tilesX
	val tileHeight = (height-80) / 20

	val r = new java.util.Random

	var mobs = Seq[Mob]()

	var mouse = Point(0,0)

	var initialized = false
	var tiles = new Array[Tile](tileAmount)
	var shots = Seq[Shot]()
	val path = Seq[Point](Point(0,1)) :+ Point(30,1) :+ Point(30,5) :+ Point(1,5) :+ Point(1,9) :+ Point(30,9) :+ Point(30,13) :+ Point(1,13) :+ Point(1,17) :+ Point(31,17)

	def posFromNum(num: Int) = Point(num % tilesX, num / tilesX)
	def posFromPoint(pos: Point) = pos.x + pos.y * tilesX

	override def update(in: Input) = {
		if (!initialized) {
			initialized = true
			for (i <- 0 until tileAmount) {
				tiles(i) = EmptyTile
			}
			for (i <- 0 to 30) {
				tiles(posFromPoint(Point(i, 1))) = RoadTile
			}
			tiles(posFromPoint(Point(30, 2))) = RoadTile
			tiles(posFromPoint(Point(30, 3))) = RoadTile
			tiles(posFromPoint(Point(30, 4))) = RoadTile
			tiles(posFromPoint(Point(30, 5))) = RoadTile
			for (i <- 1 to 29) {
				tiles(posFromPoint(Point(i, 5))) = RoadTile
			}
			tiles(posFromPoint(Point(1, 6))) = RoadTile
			tiles(posFromPoint(Point(1, 7))) = RoadTile
			tiles(posFromPoint(Point(1, 8))) = RoadTile
			tiles(posFromPoint(Point(1, 9))) = RoadTile
			for (i <- 2 to 30) {
				tiles(posFromPoint(Point(i, 9))) = RoadTile
			}
			tiles(posFromPoint(Point(30, 10))) = RoadTile
			tiles(posFromPoint(Point(30, 11))) = RoadTile
			tiles(posFromPoint(Point(30, 12))) = RoadTile
			tiles(posFromPoint(Point(30, 13))) = RoadTile
			for (i <- 1 to 29) {
				tiles(posFromPoint(Point(i, 13))) = RoadTile
			}
			tiles(posFromPoint(Point(1, 14))) = RoadTile
			tiles(posFromPoint(Point(1, 15))) = RoadTile
			tiles(posFromPoint(Point(1, 16))) = RoadTile
			tiles(posFromPoint(Point(1, 17))) = RoadTile
			for (i <- 2 to 31) {
				tiles(posFromPoint(Point(i, 17))) = RoadTile
			}
		}

		var nextMobs = mobs
		for (mob <- mobs) {
			if (mob.health > 0) {
				mob.update
			} else {
				nextMobs = nextMobs diff Seq(mob)
			}
		}
		mobs = nextMobs
		for (i <- 0 until tileAmount) {
			tiles(i).update(posFromNum(i))
		}
		for (shot <- shots) {
			shot.update
		}

		for (event <- in.queue) {
			event match {
				case KeyDownEvent(key) =>
					key match {
						case Key.LeftMouse => tiles(posFromPoint(Point(mouse.x/tileWidth, mouse.y/tileHeight))) = Tower
						case _ => mobs = mobs :+ new DefaultMob
					}
				case MouseMoveEvent(pos) => mouse = pos
				case _ => Unit
			}
		}
		Seq()
	}
	override def render(out: Output) {
		import out._
		withScaling(width, height-80) {
			drawFilledRect(java.awt.Color.BLACK)
		}
		withTranslation(0, height-80) {
			withScaling(width, 80) {
				drawFilledRect(java.awt.Color.GRAY)
			}
		}
		
		for (i <- 0 until tileAmount) {
			withScaling(tileWidth, tileHeight) {
				val pos = posFromNum(i)
				withTranslation(pos.x, pos.y) {
					tiles(i).render(out)
				}
			}
		}
		for (mob <- mobs) {
			withTranslation(mob.pos.x, mob.pos.y) {
				withScaling(tileWidth, tileHeight) {	
					mob.render(out)
				}
			}
		}
		for (shot <- shots) {
			withTranslation(shot.pos.x, shot.pos.y) {
				withScaling(tileWidth, tileHeight) {
					shot.render(out)
				}
			}
		}
	}

	override val on: PartialFunction[InputEvent, Unit] = {
		case CloseRequestedEvent => quit()
	}

	trait Mob {	
		def health: Int
		def hurt(n: Int)
		def pos: Vector2D
		def render(out: Output)
		def update(): Unit
	}
	class DefaultMob extends Mob {
		private var color = new java.awt.Color(r.nextInt(255), r.nextInt(255), r.nextInt(255))
		private var loc = Vector2D(path(0).x * tileWidth, path(0).y * tileHeight)
		private var target = 1
		private val speed = 2
		private var hp = 20
		def health = hp
		def hurt(n: Int) = hp -= n
		def pos = loc
		def render(out: Output) {
			out.drawFilledCircle(color)
		}
		def update(): Unit = {
			val targ = Vector2D(path(target).x * tileWidth, path(target).y * tileHeight)
			val dir = targ.transform(loc)
			val dir2 = dir.normalize * speed
			loc += dir2
			if ((loc - targ).length < speed) {
				target += 1
				if (target > path.size-1) {
					hp = 0
				}
			}
		}
	}

	class Shot(var pos: Vector2D, val dir: Double) {
		def update() {
			for (i <- 0 until 20) {
				pos += Vector2D.polar(dir, 1)
				for (mob <- mobs) {
					if (pos.isInsideCube(mob.pos, tileWidth, tileHeight)) {
						mob.hurt(1)
						shots = shots diff Seq(this)
					}
				}
			}
			if (pos.x < 0 || pos.x > width || pos.y < 0 || pos.y > height) shots = shots diff Seq(this)
		}
		def render(out: Output) {
			out.drawFilledOval(0,0,0.1,0.1,java.awt.Color.YELLOW)
		}
	}

	trait Tile {
		def update(pos: Point): Unit
		def render(out: Output): Unit
	}
	object EmptyTile extends Tile {
		def update(pos: Point) {}
		def render(out: Output) {
			out.drawFilledRect(java.awt.Color.BLACK)
		}
	}
	object RoadTile extends Tile {
		def update(pos: Point) {}
		def render(out: Output) {
			out.drawFilledRect(new java.awt.Color(0xc0, 0xc0, 0xc0))
		}
	}
	object Tower extends Tile {
		private var tick = 0
		def update(pos: Point) {
			tick += 1
			if (tick % 50 == 0) {
				val tower = Vector2D(pos.x*tileWidth+tileWidth/2, pos.y*tileHeight+tileHeight/2)
				var closestMob: Mob = null
				var closestDist = Double.PositiveInfinity
				for (mob <- mobs) {
					val dist = mob.pos.transform(tower).length
					if (dist < closestDist) {
						closestMob = mob
						closestDist = dist
					}
				}
				if (closestMob != null)
					shoot(tower, closestMob.pos + Vector2D(tileWidth/2, tileHeight/2))
			}
		}
		def shoot(from: Vector2D, to: Vector2D) {
			shots = shots :+ new Shot(from, to.transform(from).angle)
		}
		def render(out: Output) {
			out.drawFilledRect(java.awt.Color.BLUE)
		}
	}
}
