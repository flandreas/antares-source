package io.antarescircuit.antares.view.signal

import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.model.signal.DigitalSignalRepresentation
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.*
import io.antarescircuit.jabbah.draw.graphics.*
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment

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
	    private val INACTIVE_TEXT = RichTextDrawable.of("-", FONT)
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

    /**
     * @param textColor enforces the text color from the outside context in special situations,
     * else uses the default coloring depending on the signal
     */
    fun draw(
        context: DrawContext,
        isOn: Boolean,
        inactive: Boolean,
        textColor: Color? = null,
        focusColor: Color? = null,
        enforceSingleBitColor: Boolean = false
    ) {
        val oldColor = context.g.color
		val oldStroke = context.g.stroke

        val signalColor = if (enforceSingleBitColor) signalDigit.bits[0].color else signalDigit.color

        if (isOn) {
	        if (drawBox) {
		        context.g.color = if (inactive) {
			        transparent.applyTo(disabledColor.foregroundColor)
		        } else {
			        transparent.applyTo(signalColor.foregroundColor)
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

        label.color = textColor
            ?: transparent.applyTo(when {
                isOn -> signalColor.textColor
                context.useContextColors -> context.color!!.textColor
                else -> oldColor
            })

	    if (isOn && inactive) {
		    label.draw(INACTIVE_TEXT, context)
	    } else {
		    label.draw(context)
	    }

        if (hasFocus) {
            drawFocus(context, focusColor)
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

    private fun drawFocus(context: DrawContext, focusColor: Color?) {
        context.g.color = focusColor ?: transparent.applyTo(Themes.get<AntaresTheme>().focus.color.foregroundColor)
        context.g.stroke = Themes.get<AntaresTheme>().focus.stroke
        context.g.drawRect(xInt, yInt - 1, WIDTH, HEIGHT + 1)
    }

    /** ---- [DigitView] */

    fun setSignal(signal: DigitalSignal) {
        signalDigit = representation.signalAt(signal, index)
    }
}