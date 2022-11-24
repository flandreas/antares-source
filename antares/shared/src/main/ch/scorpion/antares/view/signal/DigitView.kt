package ch.scorpion.antares.view.signal

import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.text.FormattedText
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment

/**
 * Displays a single digit of a [DigitalSignal] as text.
 *
 * @param index the index of an entire signal's digit this [DigitView] display, where 0 is the least significant index
 * @param x the x coordinate of the upper-left corner
 * @param y the y coordinate of the upper-left corner
 */
class DigitView(
    val representation: DigitalSignalRepresentation,
    val index: Int,
    x: Double,
    y: Double,
    private val drawBorder: Boolean = true,
    private val drawBox: Boolean = true
) : AbstractRectangle(x, y, WIDTH.toDouble(), HEIGHT.toDouble()), Transparent {

    companion object {
        const val WIDTH = 20
        const val HEIGHT = 20
        val FONT = FontImpl(LogicalFontFamily.SANS_SERIF, FontStyle.PLAIN.value, (2.0 * Look.SCALE).toInt())
	    private val INACTIVE_TEXT = FormattedText("-", textWithOverline = "-")
    }

    /** Controls whether this [DigitView] has the focus and should draw a focus border.*/
    var hasFocus: Boolean = false

    private val label = Label(
        text = "",
        font = FONT,
        color = Themes.get<AntaresTheme>().zero.textColor,
        horizontalAlignment = HorizontalAlignment.CENTER,
        verticalAlignment = VerticalAlignment.CENTER,
        location = Point2D(WIDTH / 2, HEIGHT / 2))

    /** The [DigitalSignal] whose digit at [index] is displayed by this [DigitView]. */
    var signalDigit: DigitalSignal = DigitalSignalFactory.of(false)
        private set(value) {
            field = value
            label.text = representation.represent(field)
            invalidate()
        }

    init {
        setBounds(x, y, WIDTH.toDouble(), HEIGHT.toDouble())
    }

	/** ---- [Transparent] */

	private val transparent = TransparentImpl(this)

	override var transparency: Int
		get() = transparent.transparency
		set(value) { transparent.transparency = value }

	/** ---- [AbstractRectangle] */

    override val lineWidth: Double get() = 0.0

    override fun setBounds(x: Double, y: Double, w: Double, h: Double) {
        super.setBounds(x, y, w, h)
        label.location = Point2D(bounds.centerX, bounds.centerY)
    }

    /** ---- [AbstractDrawable] */

    override fun draw(context: DrawContext) {
        draw(context, isOn = true, inactive = false)
    }

    fun draw(context: DrawContext, isOn: Boolean, inactive: Boolean) {
        val oldColor = context.g.color
		val oldStroke = context.g.stroke

        if (isOn) {
	        if (drawBox) {
		        context.g.color = if (inactive) {
			        transparent.applyTo(disabledColor.foregroundColor)
		        } else {
			        transparent.applyTo(signalDigit.color.foregroundColor)
		        }
		        context.g.fillRect(xInt + 1, yInt, WIDTH - 2, HEIGHT - 1)
	        }
        } else {
            if (drawBorder) {
                context.g.color = transparent.applyTo(context.choose(Themes.get<AntaresTheme>().annotation.color).foregroundColor)
                context.g.stroke = Themes.get<AntaresTheme>().annotation.stroke
                context.g.drawRect(xInt + 1, yInt, WIDTH - 2, HEIGHT - 1)
            }
        }
        label.color = transparent.applyTo(when {
            isOn -> signalDigit.color.textColor
            context.useContextColors -> context.color!!.textColor
            else -> oldColor
        })

	    if (isOn && inactive) {
		    label.draw(INACTIVE_TEXT, context)
	    } else {
		    label.draw(context)
	    }

        if (hasFocus) {
            drawFocus(context)
        }

		context.g.color = oldColor
		context.g.stroke = oldStroke
    }

	private val disabledColor: CompositeColor get() =
		if (signalDigit.bitWidth.width > 1) {
			Themes.get<AntaresTheme>().undefined
		} else {
			Bit.Undefined.color
		}

	private fun drawInactive(context: DrawContext) {
		context.g.color = Look.inactiveColor
		context.g.fillRect(xInt + 1, yInt, WIDTH - 2, HEIGHT - 1)
	}

    private fun drawFocus(context: DrawContext) {
        context.g.color = transparent.applyTo(Themes.get<AntaresTheme>().focus.color.foregroundColor)
        context.g.stroke = Themes.get<AntaresTheme>().focus.stroke
        context.g.drawRect(xInt, yInt - 1, WIDTH, HEIGHT + 1)
    }

    /** ---- [DigitView] */

    fun setSignal(signal: DigitalSignal) {
        signalDigit = representation.signalAt(signal, index)
    }
}