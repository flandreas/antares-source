package ch.scorpion.antares.view.signal

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.edit.model.polyline.OrthoPolyline
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RoundRectangle2D
import ch.scorpion.jabbah.draw.graphics.FontFamily
import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.graphics.FontStyle
import ch.scorpion.jabbah.draw.graphics.TextRenderInfoFactory
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import kotlin.math.ceil
import kotlin.math.max

/**
 * A graphical, circular representation of a [DigitalSignal].
 * The location is defined as the center of the circle's bounding [RoundRectangle2D].
 */
class DigitalSignalView(
	val signal: DigitalSignal,
	val bitWidth: BitWidth,
	representation: DigitalSignalRepresentation
) : AbstractRectangle(RoundRectangle2D(0.0, 0.0, calcWidth(bitWidth, representation), calcHeight(), ARCH_SIZE, ARCH_SIZE)), Locatable {

	companion object {
		val FONT = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, (2.0 * Look.SCALE).toInt())
		const val V_INSET = 3
		const val H_INSET = 4
		const val ARCH_SIZE = 12.0

		fun calcHeight(): Double {
			return FONT.size + 2.0 * V_INSET
		}

		fun calcWidth(bitWidth: BitWidth, representation: DigitalSignalRepresentation): Double {
			val digitCount = max(1, bitWidth.width / representation.bits())
			val textRenderInfo = TextRenderInfoFactory.measureSingleLineText("0".repeat(digitCount), FONT)
			return ceil(textRenderInfo.textBounds.width).toInt() + 2.0 * H_INSET
		}
	}

	/**
	 * Accumulates the segment [Point2D]s that this [DigitalSignalView] visits while simulation. Note that its
	 * bounding box is NOT part of this [DigitalSignalView]'s bounding box. Clients that use the
	 * [OrthoPolyline] must accommodate its bounding box accordingly.
	 */
	val orthoPolyline = OrthoPolyline()

	private val label = Label(
		text = representation.represent(signal),
		font = FONT,
		color = signal.getColor().textColor,
		horizontalAlignment = HorizontalAlignment.CENTER,
		verticalAlignment = VerticalAlignment.CENTER,
		location = Point2D(0, 0))

	/** ---- [AbstractRectangle] */

	override fun draw(context: DrawContext) {
		drawRectangle(context, signal.getColor().backgroundColor, signal.getColor().foregroundColor, Themes.get<AntaresTheme>().annotation.stroke)
		context.g.translate(location.x, location.y)
		label.draw(context)
		context.g.translate(-location.x, -location.y)
	}

	override val lineWidth: Double get() = Themes.get<AntaresTheme>().annotation.stroke.width.toDouble()

	/** ---- [Locatable] */

	override var location: Point2D
		get() = Point2D(bounds.centerX, bounds.centerY)
		set(value) {
			setBounds(value.x - bounds.width / 2, value.y - bounds.height / 2, bounds.width, bounds.height)
			orthoPolyline.add(value)
		}
}