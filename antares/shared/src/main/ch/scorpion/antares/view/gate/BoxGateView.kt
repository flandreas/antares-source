package ch.scorpion.antares.view.gate

import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.draw.graphics.Stroke

/**
 * A box-like [VerticeView] that automatically adjusts its size according to its [DigitalPortView]s, and
 * that arranges their locations according to their [Direction]s.
 *
 * The location of a [BoxGateView] is at the connection point of the first output at the east side. Note that
 * adding and removing [PortView]s does **not** update the layout. Explicitly call
 * [updateLayout] after all [PortView]s have been added or removed.
 */
open class BoxGateView<T : Vertice>(
    styleProvider: StyleProvider,
    text: String,
    baseResourceKey: String,
    vertice: T
) : DigitalComponentView<T>(styleProvider, baseResourceKey, vertice) {

    companion object {
        val MIN_WIDTH = 6
        val MIN_HEIGHT = 8
        val PIN_INSET = 2

        /** The distance between [Port]s if the number of [Port]s is smaller than three. */
        val BIG_PORT_DISTANCE = 4

        /** The distance between [Port]s if the number of [Port]s is bigger than two. */
        val SMALL_PORT_DISTANCE = 2
    }

    /** The text displayed inside the box representing the name of the [Vertice]. */
    protected val label: Label?

    init {
        if (StringUtils.isNotEmpty(text)) {
            label = Label(
                text = text,
                font = font,
                horizontalAlignment = Label.HorizontalAlignment.CENTER,
                verticalAlignment = Label.VerticalAlignment.CENTER,
                location = Point2D(),
                rotationDisplayStrategy = Label.RotationDisplayStrategy.KEEP_HORIZONTAL)
        } else {
            label = null
        }
    }

    /** ---- [Drawable] interface */

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)
        val oldColor = context.g.color
        if (context.useContextColors) {
            drawShape(context, getColorWithTransparency(context.color!!.foregroundColor), context.color!!.backgroundColor, stroke)
        } else {
            drawShape(context, getColorWithTransparency(foregroundColor), backgroundColor, stroke)
        }
        context.g.color = oldColor
    }

    open fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
        drawEuropeanShape(context, foregroundColor, backgroundColor)
    }

    fun drawEuropeanShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
		context.g.color = backgroundColor
		context.g.fillRect(xInt, yInt, widthInt, heigthInt)

		context.g.color = foregroundColor

		context.g.stroke = stroke
		context.g.drawRect(xInt, yInt, widthInt, heigthInt)

        label?.draw(context)
    }

    /** ---- [Component] */

    override var rotation: Rotation
        get() = super.rotation
        set(value) {
            super.rotation = value
            positionLabel()
        }

    /** ---- [AbstractRectangularVerticeView] */

    override val storeSize: Boolean get() = false

    /** ---- [BoxGateView] */

    var labelText: String?
        get() = label?.text
        set(value) {
            label?.let{
                invalidate()
                label.text = value ?: ""
                invalidate()
                validate()
            }
        }

    /**
     * Updates the layout of this [BoxGateView] by calculating the required box size and by positioning all
     * [PortView]s around the box according to their [Direction].
     */
    protected fun updateLayout() {
        invalidate()

        val eastPins = getPortViewsOfDirection(Direction.EAST)
        val northPins = getPortViewsOfDirection(Direction.NORTH)
        val westPins = getPortViewsOfDirection(Direction.WEST)
        val southPins = getPortViewsOfDirection(Direction.SOUTH)

        val vPinCount = Math.max(northPins.size, southPins.size)
        val boxWidth = Math.max(2 * PIN_INSET + Math.max(0, (vPinCount - 1)) * portDistance(vPinCount), MIN_WIDTH)

        val hPinCount = Math.max(westPins.size, eastPins.size)
		val boxHeight = Math.max(2 * PIN_INSET + Math.max(0, (hPinCount - 1)) * portDistance(hPinCount), MIN_HEIGHT)

        // Layout PortViews relative to the upper left corner of the box

        var pinY: Int

        pinY = if (westPins.size == 1) boxHeight / 2 else PIN_INSET
        for (westPin in westPins) {
            westPin.setLocation(0.0, h(pinY))
            pinY += portDistance(westPins.size)
        }

        pinY = if (eastPins.size == 1) boxHeight / 2 else PIN_INSET
        for (eastPin in eastPins) {
            eastPin.setLocation(w(boxWidth), h(pinY))
            pinY += portDistance(eastPins.size)
        }

        var pinX: Int

        pinX = if (northPins.size == 1) boxWidth / 2 else PIN_INSET
        for (northPin in northPins) {
            northPin.setLocation(w(pinX), 0.0)
            pinX += portDistance(northPins.size)
        }

        pinX = if (southPins.size == 1) boxHeight / 2 else PIN_INSET
        for (southPin in southPins) {
            southPin.setLocation(w(pinX), h(boxHeight))
            pinX += portDistance(southPins.size)
        }

        // Translate the box and all PortViews relative to the first eastern PortView
        // TODO This strategy doesn't work if there is not east pin at all
        val origin = eastPins[0].location.add(eastPins[0].length.toDouble(), 0.0)
        setBounds(-origin.x, -origin.y, w(boxWidth), h(boxHeight))
        for (portView in getPortViews()) {
            portView.location = portView.location.subtract(origin)
        }

        positionLabel()

        updateBoxes()
        invalidate()
    }

    private fun portDistance(portCount: Int): Int {
        if (portCount <= 2) {
            return BIG_PORT_DISTANCE
        }
        return SMALL_PORT_DISTANCE
    }

    /**
     * Positions the [Label] within the box by centering it horizontally and placing it at one-third of the
     * height.
     */
    private fun positionLabel() {
        label?.let {
            label.ownerRotation = rotation
            label.location = when(rotation) {
                Rotation.R0 -> Point2D(x + width / 2, y + height / 3)
                Rotation.R180 -> Point2D(x + width / 2, y + 2 * height / 3)
                Rotation.R90 -> Point2D(x + 2 * width / 3, y + height / 2)
                Rotation.R270 -> Point2D(x + width / 3, y + height / 2)
            }
        }
    }
}