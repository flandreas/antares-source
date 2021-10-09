package ch.scorpion.antares.view.inout

import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Focusable
import ch.scorpion.jabbah.edit.model.text.CharacterDrawableButtonRenderer
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorDrawableButton
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
	location: Point2D,
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	private val styleType: StyleType = StyleType.FIGURE
) : ActorViewContainer<Drawable>(location, useLocation = true), Focusable {

	companion object {
		private const val INSET = 5
		private const val BUTTON_DISTANCE = 3
		private const val BUTTON_SIZE = 25
		private const val KEYBOARD_SIZE = 2 * INSET + 4 * BUTTON_SIZE + 3 * BUTTON_DISTANCE
	}

	private val bounds = Rectangle2D(location.x, location.y, KEYBOARD_SIZE.toDouble(), KEYBOARD_SIZE.toDouble())

	private val style = styleProvider.getStyle(styleType)

	/** ---- [DrawableContainer] */

	private val actorInteractionHandler = Handler()

	override fun contains(x: Double, y: Double): Boolean = bounds.contains(x, y)

	override fun draw(context: DrawContext) {
		context.g.color = style.color.backgroundColor
		context.g.fill(bounds)
		context.g.color = style.color.foregroundColor
		context.g.stroke = style.stroke
		context.g.draw(bounds)

		super.draw(context)
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
			add(ActorDrawableButton<EditInputEventContext>(
				location = Point2D(x, y),
				styleType = styleType,
				renderer = CharacterDrawableButtonRenderer(
					character = BitOperation.HEX_CHAR[i - 1],
					size = BUTTON_SIZE,
					style = styleProvider.getStyle(styleType)),
				actorAction = { buttonClickHandler(BitOperation.HEY_KEY[i - 1]) }
			))
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

		private var hoverHandler: ActorInteractionHandler? = null

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
			// Delegate to possible buttons, but keep "modal" control
			hoverHandler = hoverHandler?.mouseMoved(context)
				?: super@CircuitInOutKeyboard.getActorInteractionHandler(context).mouseMoved(context)
			return this
		}

		override fun mouseClicked(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext> {
			// Delegate to possible buttons, but keep "modal" control
			super@CircuitInOutKeyboard.getActorInteractionHandler(context).mouseClicked(context)
			return this
		}
	}
}