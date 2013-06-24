package gameengine.states

import gameengine.styles.{Style, Game}
import gameengine.{Output, ControlUpdate, Input}

import scala.util.control.Breaks.{breakable, break}

trait GameState extends Game {
	def bubbleStep: Boolean = false
	def bubbleDraw: Boolean = true
}

trait GameStateStyle extends Style { this: GameState =>
}

/**
 * @note Updating is done front-to-back
 *       Rendering is done back-to-front, but bubbling is still determined front-to-back
 */
trait GameStateStack extends GameStateStyle { this: GameState =>
	/**
	 * The stack's children, ordered from front to back
	 */
	def children: Seq[GameState]

	override def bubbleStep = children.forall(_.bubbleStep)

	private def propagate[B](updateF: Seq[GameState] => Seq[GameState], splitF: GameState => Boolean)(f: GameState => Seq[B]): Seq[B] = {
		val (bubbling, nonBubbling) = children.span(splitF(_))
		val toUpdate = updateF(bubbling ++ nonBubbling.headOption)
		toUpdate.flatMap(f)
	}

	final override def preUpdate(input: Input) { propagate(identity _, _.bubbleStep) { s => s.preUpdate(input); Seq() } }
	final override def update(input: Input): Seq[ControlUpdate] = propagate(identity _, _.bubbleStep)(_.update(input))
	final override def postUpdate(): Seq[ControlUpdate] = propagate(identity _, _.bubbleStep)(_.postUpdate())

	final override def render(output: Output) {	propagate(_.reverse, _.bubbleDraw) { s => s.render(output); Seq() }	}
}