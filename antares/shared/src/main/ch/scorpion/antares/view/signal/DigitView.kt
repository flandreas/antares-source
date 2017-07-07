package ch.scorpion.antares.view.signal

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.graphics.FontStyle
import ch.scorpion.jabbah.draw.style.Themes

/**
 * Displays a single binary digit of a [DigitalSignal] as text.
 */
class DigitView(
    val representation: DigitalSignalRepresentation,
    val index: Int,
    x: Double,
    y: Double
) : AbstractRectangle(x.toDouble(), y.toDouble(), WIDTH.toDouble(), HEIGHT.toDouble()) {

    companion object {
        val WIDTH = 20
        val HEIGHT = 20
        val FONT = FontImpl("SansSerif", FontStyle.PLAIN.value, (2.0 * Look.SCALE).toInt())
    }

    /** Controls whether this [DigitView] has the focus and should draw a focus border.*/
    var hasFocus: Boolean = false

    private val label = Label(
        text = "",
        font = FONT,
        color = Themes.get<AntaresTheme>().zero.textColor,
        horizontalAlignment = Label.HorizontalAlignment.CENTER,
        verticalAlignment = Label.VerticalAlignment.CENTER,
        location = Point2D(WIDTH / 2, HEIGHT / 2))

    /** The [DigitalSignal] whose digit at [index] is displayed by this [DigitView]. */
    private var signalDigit: DigitalSignal = Word.of(false)
        set(value) {
            field = value
            label.text = representation.represent(field)
            invalidate()
        }

    init {
        setBounds(x, y, WIDTH.toDouble(), HEIGHT.toDouble())
    }

    /** ---- [RectangularDrawable] */

    override val lineWidth: Double get() = 0.0

    override fun setBounds(x: Double, y: Double, w: Double, h: Double) {
        super.setBounds(x, y, w, h)
        label.location = Point2D(bounds.centerX, bounds.centerY)
    }

    /** ---- [AbstractDrawable] */

    override fun draw(context: DrawContext) {
        draw(context, true)
    }

    fun draw(context: DrawContext, isOn: Boolean) {
        val oldColor = context.g.color
		val oldStroke = context.g.stroke

        if (isOn) {
            context.g.color = signalDigit.getColor().foregroundColor
            context.g.fillRect(xInt + 1, yInt, WIDTH - 2, HEIGHT - 1)
        } else {
            context.g.color = context.choose(Themes.get<AntaresTheme>().annotation.color).foregroundColor
            context.g.stroke = Themes.get<AntaresTheme>().annotation.stroke
            context.g.drawRect(xInt + 1, yInt, WIDTH - 2, HEIGHT - 1)
        }
        label.color = if (isOn) {
            signalDigit.getColor().textColor
        } else if (context.useContextColors) {
            context.color!!.textColor
        } else {
            oldColor
        }

		label.draw(context)
        if (hasFocus) {
            drawFocus(context)
        }

		context.g.color = oldColor
		context.g.stroke = oldStroke
    }

    private fun drawFocus(context: DrawContext) {
        context.g.color = Themes.get<AntaresTheme>().focus.color.foregroundColor
        context.g.stroke = Themes.get<AntaresTheme>().focus.stroke
        context.g.drawRect(xInt, yInt - 1, WIDTH, HEIGHT + 1)
    }

    /** ---- [DigitView] */

    fun setSignal(signal: DigitalSignal) {
        signalDigit = representation.signalAt(signal, index)
    }
}