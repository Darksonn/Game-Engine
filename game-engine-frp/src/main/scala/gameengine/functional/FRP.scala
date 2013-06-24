package gameengine

import scalaz._
import scalaz.syntax._
import scalaz.effect._

package object functional {
	
	type NextT[M[+_], +A] = Kleisli[M, Input, A]
	type SignalT[M[+_], +A] = Cofree[({type l[+T]=NextT[M, T]})#l, A]
	type EventuallyT[M[+_], +A] = Free[({type l[+T]=NextT[M, T]})#l, A]

	type Picture[+A] = Kleisli[IO, Output, A]

	type Color = java.awt.Color

	def SignalT[M[+_]: Monad, A](x: A, r: NextT[M, SignalT[M, A]]) = Cofree[({type l[+T]=NextT[M, T]})#l, A](x, r)

	def MousePosition[M[+_]: Monad]: SignalT[M, Point] = {
		def mp(p: Point): SignalT[M, Point] =
			SignalT(p, Kleisli(in => Monad[M].point(mp(in.queue.reverse.collectFirst{case MouseMoveEvent(p) => p}.getOrElse(p)))))
		mp(Point(0, 0))
	}
	def CloseRequested[M[+_]: Monad]: SignalT[M, Boolean] = {
		def cr: NextT[M, SignalT[M, Boolean]] = Kleisli(in => Monad[M].point(SignalT(in.queue.contains((_: InputEvent) == CloseRequestedEvent), cr)))
		SignalT(false, cr)
	}

	def Circle(col: Color): Picture[Unit] = Kleisli(out => IO(out.drawFilledCircle(col)))
	def Square(col: Color): Picture[Unit] = Kleisli(out => IO(out.drawFilledRect(col)))

	implicit class PictureOps[+A](val internal: Picture[A]) extends AnyVal {
		def rotated(theta: Double): Picture[A] =
			Kleisli(out => out.withRotation(theta) { internal(out) })
		def scaled(scaleX: Double, scaleY: Double): Picture[A] =
			Kleisli(out => out.withScaling(scaleX, scaleY) { internal(out) })
		def translated(translateX: Double, translateY: Double): Picture[A] =
			Kleisli(out => out.withTranslation(translateX, translateY) { internal(out) })
	}

}