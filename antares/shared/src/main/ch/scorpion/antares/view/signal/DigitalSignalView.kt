package ch.scorpion.antares.view.signal

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.CurrentDigitalSignalNotation
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.jabbah.edit.Look
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RoundRectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.polyline.CompactablePolyline
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment

/**
 * A graphical, circular representation of a [DigitalSignal].
 * The location is defined as the center of the circle's bounding [RoundRectangle2D].
 */
class DigitalSignalView(
	val signal: DigitalSignal,
	val bitWidth: BitWidth,
	representation: DigitalSignalRepresentation
) : AbstractRectangle(RoundRectangle2D(0.0, 0.0, 0.0, 0.0, ARCH_SIZE, ARCH_SIZE)) {

	companion object {
		private val FONT = FontImpl(LogicalFontFamily.SANS_SERIF, FontStyle.PLAIN.value, (2.0 * Look.SCALE).toInt())
		private const val V_INSET = 3
		private const val H_INSET = 6
		private const val ARCH_SIZE = 12.0
	}

	/**
	 * Accumulates the segment [Point2D]s that this [DigitalSignalView] visits while simulation. Note that its
	 * bounding box is NOT part of this [DigitalSignalView]'s bounding box. Clients that use the
	 * [CompactablePolyline] must accommodate its bounding box accordingly.
	 */
	val orthoPolyline = CompactablePolyline()

	private val label: Label

	init {
		val text = CurrentDigitalSignalNotation.notation.notate(signal, representation)
		val textSize = TextRenderInfoFactory.measureSingleLineText(text, FONT).textBounds

		label = Label(
			text = text,
			font = FONT,
			color = signal.color.textColor,
			horizontalAlignment = HorizontalAlignment.CENTER,
			verticalAlignment = VerticalAlignment.CENTER,
			location = Point2D(0, 0))

		setBounds(0.0, 0.0, textSize.width + 2 * H_INSET, textSize.height + 2 * V_INSET)
	}

	/** ---- [AbstractRectangle] */

	override fun draw(context: DrawContext) {
		drawRectangle(context, signal.color.backgroundColor, signal.color.foregroundColor, Themes.get<AntaresTheme>().annotation.stroke)
		context.translated(location) { label.draw(it) }
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