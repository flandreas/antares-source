package ch.scorpion.antares.view.inout

import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Focusable
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorViewContainer

/**
 * Displays a keyboard for entering digits into a [CircuitInOutView] during simulation.
 *
 * Note that this object shouldn't request the focus (and therefore can't receive key events)
 * because focus should remain in [CircuitInOutView] for advancing digits after entering values.
 */
class CircuitInOutKeyboard(
	private val circuitInOutView: CircuitInOutView,
	private val view: DrawingView<*>,
	private val signalHandler: SignalHandler,
	location: Point2D
) : ActorViewContainer<Drawable>(location, useLocation = true), Focusable {

	companion object {
		private const val INSET = 5
		private const val BUTTON_DISTANCE = 3
		private const val BUTTON_SIZE = 20
		private const val KEYBOARD_SIZE = 2 * INSET + 4 * BUTTON_SIZE + 3 * BUTTON_DISTANCE
	}

	private val bounds = Rectangle2D(location.x, location.y, KEYBOARD_SIZE.toDouble(), KEYBOARD_SIZE.toDouble())

	/** ---- [DrawableContainer] */

	private val actorInteractionHandler = Handler()

	override fun contains(x: Double, y: Double): Boolean = bounds.contains(x, y)

	override fun draw(context: DrawContext) {
		super.draw(context)
		context.g.color = Color.GREEN
		context.g.draw(bounds)
	}

	override val boundingBox: RectangularShape get() = Rectangle2D(bounds)

	init {
		buildHexadecimalUI()
	}

	/** ---- [ActorViewContainer] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
		actorInteractionHandler

	/** ---- [Focusable] */

	override val isFocusable: Boolean get() = true

	/** ---- [CircuitInOutKeyboard] */

	private fun buildHexadecimalUI() {
		var x = INSET
		var y = INSET
		for (i in 1..16) {
			add(KeyButton(Point2D(x, y), BitOperation.HEX_CHAR[i - 1], BitOperation.HEY_KEY[i - 1], BUTTON_SIZE, ::buttonClickHandler))
			x += BUTTON_SIZE + BUTTON_DISTANCE
			if (i.mod(4) == 0) {
				x = INSET
				y += BUTTON_SIZE + BUTTON_DISTANCE
			}
		}
	}

	private fun buttonClickHandler(key: Int) {
		circuitInOutView.consumeKey(key, signalHandler)
	}

	private fun removeFromView() {
		view.animationContainer.remove(this)
		view.animationContainer.validate()
	}

	private inner class Handler : InputEventHandlerAdapter<ActorInteractionContext>() {

		override fun mousePressed(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
			if (!bounds.contains(context.x, context.y)) {
				removeFromView()
				return null
			}
			return this
		}

		override fun mouseReleased(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext> {
			return this
		}

		override fun mouseMoved(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext> {
			return this
		}

		override fun mouseClicked(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext> {
			// Delegate to possible buttons, but keep "modal" control
			super@CircuitInOutKeyboard.getActorInteractionHandler(context).mouseClicked(context)
			return this
		}
	}
}