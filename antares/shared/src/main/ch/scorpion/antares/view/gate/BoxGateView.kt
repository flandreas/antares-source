package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.edit.Look
import ch.scorpion.antares.view.OrientableLabeledRectangularVerticeView
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import kotlin.math.floor
import kotlin.math.max

/**
 * A box-like [VerticeView] that automatically adjusts its size according to its [DigitalPortView]s, and
 * that arranges their locations according to their [Direction]s.
 *
 * The location of a [BoxGateView] is at the connection point of the first output at the east side. Note that
 * adding and removing [PortView]s does **not** update the layout. Explicitly call
 * [updateLayout] after all [PortView]s have been added or removed.
 *
 * @property minWidth the minimum width in [Look.SCALE] units
 */
open class BoxGateView<T : Vertice>(
	styleProvider: StyleProvider,
	text: String,
	vertice: T,
	private val minWidth: Int = DEF_MIN_WIDTH
) : OrientableLabeledRectangularVerticeView<T>(styleProvider, text,vertice) {

	companion object {
		private const val DEF_MIN_WIDTH = 6
		private const val MIN_HEIGHT = 8
		private const val PIN_INSET = 2

		/** The distance between [Port]s if the number of [Port]s is smaller than three. */
		const val BIG_PORT_DISTANCE = 4

		/** The distance between [Port]s if the number of [Port]s is bigger than two. */
		const val SMALL_PORT_DISTANCE = 2
	}

	private val effMinWidth: Float get() = minWidth * scale

	private val effMinHeight: Float get() = MIN_HEIGHT * scale

	private val effPinInset: Float get() = PIN_INSET * scale

	/** ---- [Drawable] interface */

	override fun drawImpl(context: DrawContext) {
		val oldColor = context.g.color

		drawImplBeforeBorder(context)
		drawShape(context, getApplicableForegroundColor(context), getApplicableBackgroundColor(context), stroke)
		drawImplAfterBorder(context)

		context.g.color = oldColor
	}

	/**
	 * Draws the box shape of this [BoxGateView] within a translated and rotated context.
	 * Can be overwritten to draw non-box like shapes.
	 */
	open fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
		drawBoxShape(context, foregroundColor, backgroundColor, stroke)
	}

	fun drawBoxShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke, text: String? = null) {
		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fillRect(xInt, yInt, widthInt, heightInt)
			}
		}

		context.g.color = backgroundColor
		context.g.fillRect(xInt, yInt, widthInt, heightInt)

		if (this is CustomShapeContent) {
			drawCustomShapeContent(context, foregroundColor, backgroundColor)
		}

		context.g.color = foregroundColor
		context.g.stroke = stroke
		context.g.drawRect(xInt, yInt, widthInt, heightInt)

		drawLabelText(context, text)
	}

	/** ---- [AbstractRectangularVerticeView] */

	override val storeSize: Boolean get() = false

	/** ---- [BoxGateView] */

	open fun createInputPortView(inputPort: Port<DigitalSignal>, portLabelPosition: PortLabelPosition = PortLabelPosition.INTERNAL): DigitalPortView =
		DigitalPortView(
			styleProvider = styleProvider,
			port = inputPort,
			direction = Direction.WEST,
			portLabelPosition = portLabelPosition)

	open fun createOutputPortView(outputPort: Port<DigitalSignal>, portLabelPosition: PortLabelPosition = PortLabelPosition.EXTERNAL): DigitalPortView =
		DigitalPortView(
			styleProvider = styleProvider,
			port = outputPort,
			direction = Direction.EAST,
			portLabelPosition = portLabelPosition)

	/**
	 * Updates the layout of this [BoxGateView] by calculating the required box size and by positioning all
	 * [PortView]s around the box according to their [Direction].
	 */
	fun updateLayout() {
		invalidate()

		val eastPins = getPortViewsOfDirection(Direction.EAST)
		val northPins = getPortViewsOfDirection(Direction.NORTH)
		val westPins = getPortViewsOfDirection(Direction.WEST)
		val southPins = getPortViewsOfDirection(Direction.SOUTH)

		val vPinCount = max(northPins.size, southPins.size)
		val vPinArea: Float = if (vPinCount == 0) 0f else (2 * effPinInset).toInt() + max(0, (vPinCount - 1)) * portDistance(vPinCount)
		val boxWidth: Float = max(vPinArea, effMinWidth)

		val hPinCount = max(westPins.size, eastPins.size)
		val hPinArea = 2 * effPinInset + max(0, (hPinCount - 1)) * portDistance(hPinCount)
		val boxHeight: Float = max(hPinArea, effMinHeight)

        // Layout PortViews relative to the upper left corner of the box

        var pinY: Float = if (westPins.size == 1) boxHeight / 2 else effPinInset
		for (westPin in westPins) {
			westPin.setLocation(0.0, h(pinY))
			pinY += portDistance(westPins.size)
		}

		pinY = if (eastPins.size == 1) boxHeight / 2 else effPinInset
		for (eastPin in eastPins) {
			eastPin.setLocation(w(boxWidth), h(pinY))
			pinY += portDistance(eastPins.size)
		}

        var pinX: Float = if (northPins.size == 1) boxWidth / 2 else effPinInset
		for (northPin in northPins) {
			northPin.setLocation(w(pinX), 0.0)
			pinX += portDistance(northPins.size)
		}

		// TODO: Should this be boxWidth below?
		pinX = if (southPins.size == 1) boxHeight / 2 else effPinInset
		for (southPin in southPins) {
			southPin.setLocation(w(pinX), h(boxHeight))
			pinX += portDistance(southPins.size)
		}

		// Translate the box and all PortViews relative to the first eastern PortView
		// TODO This strategy doesn't work if there is no east pin at all
		val origin = eastPins[0].location.add(eastPins[0].unconnectedLength.toDouble(), 0.0)
		setBounds(-origin.x, -origin.y, floor(w(boxWidth)), floor(h(boxHeight)))
		for (portView in getPortViews()) {
			portView.location = portView.location.subtract(origin)
		}

		labelStyle.updateLabel(this)

		updateBoxes()
		invalidate()

		update()
	}

	private fun portDistance(portCount: Int): Float {
		if (portCount <= 2) {
			return BIG_PORT_DISTANCE * scale
		}
		return SMALL_PORT_DISTANCE * scale
	}
}
