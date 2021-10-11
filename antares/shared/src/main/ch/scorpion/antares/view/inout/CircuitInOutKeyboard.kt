package ch.scorpion.antares.view.inout

import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.DrawableAttendantPositioner
import ch.scorpion.jabbah.draw.style.*
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Focusable
import ch.scorpion.jabbah.edit.model.text.TextDrawableButtonRenderer
import ch.scorpion.jabbah.execution.actor.ActorDrawableButton
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorViewContainer
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder

/**
 * Displays a keyboard for entering digits into a [CircuitInOutView] during simulation.
 *
 * Note that this object shouldn't request the focus (and therefore can't receive key events)
 * because focus should remain in [CircuitInOutView] for advancing digits after entering values.
 */
class CircuitInOutKeyboard(
	private val circuitInOutView: CircuitInOutView,
	view: View<*>,
	private val contextHolder: GraphApplicationContextHolder,
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	private val styleType: StyleType = StyleType.FIGURE
) : ActorViewContainer<Drawable>(useLocation = true), Focusable {

	companion object {
		private const val VERTICAL_DISTANCE = 0
		private const val MARGIN = 10
		private const val PADDING = 5
		private const val BUTTON_GAP = 3
		private const val BUTTON_SIZE = 25
		private const val KEYBOARD_WIDTH = 2 * MARGIN + 2 * PADDING + 4 * BUTTON_SIZE + 3 * BUTTON_GAP
		private const val KEYBOARD_HEIGHT = 2 * MARGIN + 2 * PADDING + 5 * BUTTON_SIZE + 4 * BUTTON_GAP
	}

	private val bounds: Rectangle2D

	private val style: Style get() = styleProvider.getStyle(styleType)

	init {
		location = DrawableAttendantPositioner.position(
			Dimension2D(KEYBOARD_WIDTH, KEYBOARD_HEIGHT), circuitInOutView.boundingBox,
			view,
			preferredBelow = true,
			VERTICAL_DISTANCE)
		bounds = Rectangle2D(location, Dimension2D(KEYBOARD_WIDTH, KEYBOARD_HEIGHT))
	}

	/** ---- [DrawableContainer] */

	private val actorInteractionHandler = Handler()

	override fun contains(x: Double, y: Double): Boolean = bounds.contains(x, y)

	override fun draw(context: DrawContext) {
		// Draw margin area
		context.g.color = Themes.get<AntaresTheme>().background.color.foregroundColor
		context.g.fill(bounds)

		// Draw keyboard background
		context.g.color = style.color.backgroundColor
		context.g.fillRect(location.xInt + MARGIN, location.yInt + MARGIN, KEYBOARD_WIDTH - 2 * MARGIN, KEYBOARD_HEIGHT - 2 * MARGIN)

		context.g.color = style.color.foregroundColor
		context.g.stroke = style.stroke
		context.g.drawRect(location.xInt + MARGIN, location.yInt + MARGIN, KEYBOARD_WIDTH - 2 * MARGIN, KEYBOARD_HEIGHT - 2 * MARGIN)

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
		var x = MARGIN + PADDING
		var y = MARGIN + PADDING
		for (i in 1..18) {
			val button = ActorDrawableButton<EditInputEventContext>(
				location = Point2D(x, y),
				styleType = styleType,
				renderer = TextDrawableButtonRenderer(
					text = BitOperation.HEX_CHAR[i - 1].toString(),
					dimension = Dimension2D(BUTTON_SIZE, BUTTON_SIZE),
					style = styleProvider.getStyle(styleType)),
				actorAction = { buttonClickHandler(BitOperation.HEY_KEY[i - 1]) },
				round = true
			)
			button.enabled = circuitInOutView.signalRepresentation == DigitalSignalRepresentation.HEXADECIMAL || i <= 10
			add(button)

			x += BUTTON_SIZE + BUTTON_GAP
			if (i.mod(4) == 0) {
				x = MARGIN + PADDING
				y += BUTTON_SIZE + BUTTON_GAP
			}
		}
		val clearButton = ActorDrawableButton<EditInputEventContext>(
			location = Point2D(x, y),
			styleType = styleType,
			renderer = TextDrawableButtonRenderer(
				text = "Clear",
				dimension = Dimension2D(2 * BUTTON_SIZE + BUTTON_GAP, BUTTON_SIZE),
				style = styleProvider.getStyle(styleType)),
			actorAction = { clearHandler() },
			round = true
		)
		add(clearButton)
	}

	private fun buttonClickHandler(key: Int) {
		circuitInOutView.consumeKey(key, contextHolder)
	}

	private fun clearHandler() {
		circuitInOutView.clearByUser(contextHolder.scheduler)
	}

	private inner class Handler : InputEventHandlerAdapter<ActorInteractionContext>() {

		private var hoverHandler: ActorInteractionHandler? = null

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