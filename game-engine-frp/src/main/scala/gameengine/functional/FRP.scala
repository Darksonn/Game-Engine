package gameengine

import language._
import dk.tailcalled.pfp._

package object functional {
	
	trait Next[+A] {
		def apply(in: Input): A
	}
	trait Signal[+A] {
		val now: A
		def later: Next[Signal[A]]
	}
	sealed trait Eventually[+A] // wonder if this will ever get used...
	case object Eventually {
		case class Now[+A](now: A) extends Eventually[A]
		case class Later[+A](later: Eventually[A]) extends Eventually[A]
	}
	trait Picture[A] { self => // generic because why not?
		def paint(gfx: Output): A
		def scaled(sx: Double, sy: Double): Picture[A] = new Picture[A] {
			def paint(gfx: Output) = gfx.withScaling(sx, sy) { self.paint(gfx) }
		}
		def rotated(theta: Double): Picture[A] = new Picture[A] {
			def paint(gfx: Output) = gfx.withRotation(theta) { self.paint(gfx) }
		}
		def translated(tx: Double, ty: Double): Picture[A] = new Picture[A] {
			def paint(gfx: Output) = gfx.withTranslation(tx, ty) { self.paint(gfx) }
		}
	}
	def square(col: gameengine.Color): Picture[Unit] = new Picture[Unit] {
		def paint(gfx: Output) = { gfx.drawFilledRect(col); () }
	}

	implicit object NextInstances extends Monoidal[Next] {
		def lift[A, B](f: A => B): Next[A] => Next[B] = (n) => new Next[B] {
			def apply(in: Input): B = f(n(in))
		}
		val unit = new Next[Unit] { def apply(in: Input) = () }
		def pair[A, B](a: Next[A], b: Next[B]) = new Next[(A, B)] { def apply(in: Input) = (a(in), b(in)) }
	}
	implicit object SignalInstances extends Monoidal[Signal] with Comonad[Signal] {
		def lift[A, B](f: A => B): Signal[A] => Signal[B] = (s) => new Signal[B] {
			val now = f(s.now)
			def later = lift(f).on(s.later)
		}
		val unit: Signal[Unit] = new Signal[Unit] {
			val now = ()
			def later = unit.point[Next]
		}
		def pair[A, B](a: Signal[A], b: Signal[B]) = new Signal[(A, B)] {
			val now = (a.now, b.now)
			def later = (pair[A, B] _).on(a.later, b.later)
		}
		def extract[A](a: Signal[A]): A = a.now
		def duplicate[A](a: Signal[A]): Signal[Signal[A]] = new Signal[Signal[A]] {
			val now = a
			def later = a.later.map(_.duplicate)
		}
	}
	implicit object PictureInstances extends Monad[Picture] {
		def lift[A, B](f: A => B): Picture[A] => Picture[B] = (p) => new Picture[B] {
			def paint(gfx: Output) = f(p.paint(gfx))
		}
		val unit = new Picture[Unit] { def paint(gfx: Output) = () }
		def join[A](a: Picture[Picture[A]]) = new Picture[A] {
			def paint(gfx: Output) = a.paint(gfx).paint(gfx)
		}
	}
	def eventsInit(ev: Vector[InputEvent]): Signal[Vector[InputEvent]] = new Signal[Vector[InputEvent]] {
		val now = ev
		def later = new Next[Signal[Vector[InputEvent]]] { def apply(in: Input) = eventsInit(Vector(in.queue: _*)) }
		// TODO: change repr to Vector
	}
	val events = eventsInit(Vector())
	def unfoldSignal[S](seed: S)(f: S => S): Signal[S] = new Signal[S] {
		val now = seed
		def later = unfoldSignal(f(seed))(f).point[Next]
	}
	def always[S](value: S): Signal[S] = new Signal[S] {
		val now = value
		def later = (this: Signal[S]).point[Next]
	}

}