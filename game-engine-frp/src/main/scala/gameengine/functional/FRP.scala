package gameengine

import language._

package object functional {
	
	trait Next[+A] {
		def apply(in: Input): A
	}
	trait Signal[+A] {
		def now: A
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
	def square(col: java.awt.Color): Picture[Unit] = new Picture[Unit] {
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
			def now = f(s.now)
			def later = lift(f).on(s.later)
		}
		val unit: Signal[Unit] = new Signal[Unit] {
			def now = ()
			def later = unit.point[Next]
		}
		def pair[A, B](a: Signal[A], b: Signal[B]) = new Signal[(A, B)] {
			def now = (a.now, b.now)
			def later = (pair[A, B] _).on(a.later, b.later)
		}
		def extract[A](a: Signal[A]): A = a.now
		def duplicate[A](a: Signal[A]): Signal[Signal[A]] = new Signal[Signal[A]] {
			def now = a
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
		def now = ev
		def later = new Next[Signal[Vector[InputEvent]]] { def apply(in: Input) = eventsInit(Vector(in.queue: _*)) }
		// TODO: change repr to Vector
	}
	val events = eventsInit(Vector())
	def unfoldSignal[S](seed: S)(f: S => S): Signal[S] = new Signal[S] {
		def now = seed
		def later = unfoldSignal(f(seed))(f).point[Next]
	}
	def always[S](value: S): Signal[S] = new Signal[S] {
		def now = value
		def later = (this: Signal[S]).point[Next]
	}

	// TODO: make new project for all of these:
	implicit class Apply[F[_], A](val u: F[A]) extends AnyVal {
		def map[B](f: A => B)(implicit functor: Functor[F]): F[B] = functor.lift(f)(u)
		def fmap[B](f: A => B)(implicit functor: Functor[F]): F[B] = functor.lift(f)(u)
		def flatMap[B](f: A => F[B])(implicit monad: Monad[F]): F[B] = monad.bind(f)(u)
		def bind[B](f: A => F[B])(implicit monad: Monad[F]): F[B] = monad.bind(f)(u)
		def extend[B](f: F[A] => B)(implicit comonad: Comonad[F]): F[B] = comonad.extend(f)(u)
		def pair[B](v: F[B])(implicit monoidal: Monoidal[F]): F[(A, B)] = monoidal.pair(u, v)
		def extract(implicit comonad: Comonad[F]): A = comonad.extract(u)
		def duplicate(implicit comonad: Comonad[F]): F[F[A]] = comonad.duplicate(u)
	}
	implicit class Apply2[F[_], A](val u: F[F[A]]) extends AnyVal {
		def join(implicit monad: Monad[F]): F[A] = monad.join(u)
	}
	implicit class Id[A](val u: A) extends AnyVal {
		def point[F[_]](implicit monoidal: Monoidal[F]): F[A] = monoidal.point(u)
		def ++(v: A)(implicit monoid: Monoid[A]) = monoid.append(u, v)
	}
	implicit class BooleanOps(val u: Boolean) extends AnyVal {
		def guard[A](a: A)(implicit monoid: Monoid[A]) = if (u) a else unit[A]
	}
	def unit[A](implicit monoid: Monoid[A]) = monoid.unit
	implicit class Func1[A, B](val f: A => B) extends AnyVal {
		def lift[F[_]](implicit functor: Functor[F]): F[A] => F[B] = functor.lift(f)
		def on[F[_]](a: F[A])(implicit functor: Functor[F]): F[B] = a.map(f)
	}
	implicit class Func2[A, B, C](val f: (A, B) => C) extends AnyVal {
		def on[F[_]](a: F[A], b: F[B])(implicit monoidal: Monoidal[F]): F[C] = (a pair b).map(f.tupled)
	}
	trait Semigroup[M] {
		def append(m: M, n: M): M
	}
	trait Monoid[M] extends Semigroup[M] {
		val unit: M
	}
	trait Functor[F[_]] {
		def lift[A, B](f: A => B): F[A] => F[B]
	}
	trait Monoidal[F[_]] extends Functor[F] {
		val unit: F[Unit]
		def point[A](a: A): F[A] = unit.fmap(_ => a)(this)
		def pair[A, B](a: F[A], b: F[B]): F[(A, B)]
	}
	trait Monad[M[_]] extends Monoidal[M] {
		def join[A](v: M[M[A]]): M[A]
		def pair[A, B](a: M[A], b: M[B]) = a.fmap(av => b.fmap(bv => (av, bv))(this))(this).join(this)
		def bind[A, B](f: A => M[B]): M[A] => M[B] = lift(f) andThen (join[B] _)
	}
	trait Comonad[W[_]] extends Functor[W] {
		def extract[A](a: W[A]): A
		def duplicate[A](a: W[A]): W[W[A]]
		def extend[A, B](f: W[A] => B): W[A] => W[B] = (duplicate[A] _) andThen lift(f)
	}

	implicit def monoidFromMonoidal[F[_], M]
		(implicit monoidal: Monoidal[F], monoid: Monoid[M]): Monoid[F[M]] = new MonoidFromMonoidal
	class MonoidFromMonoidal[F[_], M](implicit monoidal: Monoidal[F], monoid: Monoid[M]) extends Monoid[F[M]] {
		def append(m: F[M], n: F[M]) = (monoid.append _).on(m, n)
		val unit = monoid.unit.point[F]
	}
	implicit def vectorInstances[A] = new VectorInstances[A]
	class VectorInstances[A] extends Monoid[Vector[A]] {
		def append(m: Vector[A], n: Vector[A]) = m ++ n
		val unit = Vector()
	}
	implicit object UnitInstances extends Monoid[Unit] {
		def append(m: Unit, n: Unit) = ()
		val unit = ()
	}
	implicit def pairMonoid[A, B](implicit a: Monoid[A], b: Monoid[B]): Monoid[(A, B)] = new PairMonoid
	class PairMonoid[A, B](implicit a: Monoid[A], b: Monoid[B]) extends Monoid[(A, B)] {
		def append(m: (A, B), n: (A, B)) = (m._1 ++ n._1, m._2 ++ n._2)
		val unit = (a.unit, b.unit)
	}

}