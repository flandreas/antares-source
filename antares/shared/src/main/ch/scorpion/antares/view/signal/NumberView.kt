package ch.scorpion.antares.view.signal

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import kotlin.math.max

/**
 * Displays a digital number with individual [DigitView]s.
 */
class NumberView(
	representation: DigitalSignalRepresentation,
	bitWidth: BitWidth,
	drawDigitBorder: Boolean = true,
	drawBox: Boolean = true
) : AbstractRectangle(), Transparent {

	companion object {
		private const val DIGIT_GROUP_GAP = 5
		private const val BYTE_LABEL_HOR_GAP = 0
	}

	/** Contains the individual digit views, starting with the lowest priority bit at index 0. */
	private val digitViews = mutableListOf<DigitView>()

	/** Contains the [Label]s that designate the index of the displayed byte within the entire signal value. */
	private val byteIndexLabels = mutableListOf<Label>()

	/** The index of the [digitViews] that has the focus, or `null` if none has the focus. */
	var focusIndex: Int? = null
		private set

	init {
		buildUI(representation, bitWidth, drawDigitBorder, drawBox)
	}

	/** ---- [Transparent] */

	private val transparent = TransparentImpl(this)

	override var transparency: Int
		get() = transparent.transparency
		set(value) {
			transparent.transparency = value
			digitViews.forEach { it.transparency = value }
		}

	/** ---- [AbstractRectangle] */

	override val lineWidth: Double get() = 0.0

	override fun draw(context: DrawContext) {
		draw(context, true)
	}

	fun draw(context: DrawContext, isOn: Boolean, inactive: Boolean = false) {
		context.g.translate(x, y)
		for (digitView in digitViews) {
			digitView.draw(context, isOn, inactive)
		}
		drawByteIndexLabels(context)
		context.g.translate(-x, -y)
	}

	private fun drawByteIndexLabels(context: DrawContext) {
		if (context.useContextColors) {
			context.g.color = transparent.applyTo(context.color!!.textColor)
		}
		for (label in byteIndexLabels) {
			label.draw(context)
		}
	}

	/** ---- [NumberView] */

	/** Returns the number of [DigitView] that this [NumberView] displays.*/
	val digitCount: Int get() = digitViews.size

	fun clear() {
		digitViews.clear()
	}

	fun setSignal(signal: DigitalSignal) {
		invalidate()
		digitViews.forEach { it.setSignal(signal) }
		validate()
	}

	/**
	 * Returns the index of the digit at the specified relative coordinates.
	 * @param x the relative x-coordinate
	 * @param y the relative y-coordinate
	 * @return the index of the digit at the specified relative coordinates, if any. The first index is 0.
	 */
	fun getDigitIndexAt(x: Double, y: Double): Int? {
		for ((i, digitView) in digitViews.withIndex()) {
			if (digitView.contains(x, y)) {
				return i
			}
		}
		return null
	}

	fun focusGained() {
		updateFocusIndex(digitCount - 1)
	}

	fun focusLost() {
		updateFocusIndex(null)
	}

	fun transferFocusRight() {
		if (focusIndex != null) {
			updateFocusIndex(if (focusIndex == 0) digitCount - 1 else focusIndex!! - 1)
		}
	}

	fun transferFocusLeft() {
		if (focusIndex != null) {
			updateFocusIndex(if (focusIndex == digitCount - 1) 0 else focusIndex!! + 1)
		}
	}

	fun setFocusTo(newFocusIndex: Int) {
		if (newFocusIndex != focusIndex) {
			updateFocusIndex(newFocusIndex)
		}
	}

	private fun updateFocusIndex(newIndex: Int?) {
		if (focusIndex != null) {
			digitViews[focusIndex!!].hasFocus = false
		}
		focusIndex = newIndex
		if (focusIndex != null) {
			digitViews[focusIndex!!].hasFocus = true
		}
	}

	private fun buildUI(
		representation: DigitalSignalRepresentation,
		bitWidth: BitWidth,
		drawDigitBorder: Boolean,
		drawBox: Boolean
	) {
		val bounds = Rectangle2D(0, 0, 0, 0)
		var x = 0.0
		var y = 0.0
		var byteLabelColumnWidth = 0.0

		val maxDigitPerRow = 2 * representation.digitGroupSize
		val digitViewCount = representation.digitCount(bitWidth)

		// First row inset
		if (digitViewCount > maxDigitPerRow && digitViewCount % maxDigitPerRow != 0) {
			val insetDigitCount = maxDigitPerRow - (digitViewCount % maxDigitPerRow)
			var inset = insetDigitCount * DigitView.WIDTH
			if (insetDigitCount >= representation.digitGroupSize) {
				inset += DIGIT_GROUP_GAP
			}
			x = inset.toDouble()
		}

		for (i in digitViewCount - 1 downTo 0) {
			val digitView = DigitView(representation, i, x, y, drawDigitBorder, drawBox)
			digitViews.add(0, digitView)
			x += digitView.width.toInt()

			if (digitViewCount > maxDigitPerRow && i % maxDigitPerRow == 0) {
				// Row break
				val label = Label(
					text = i.toString(),
					location = Point2D(x + BYTE_LABEL_HOR_GAP, y + digitView.height),
					font = Look.EXT_PIN_FONT,
					color = Themes.get<AntaresTheme>().vertice.color.textColor,
					horizontalAlignment = HorizontalAlignment.LEFT,
					verticalAlignment = VerticalAlignment.BOTTOM)
				byteIndexLabels.add(label)
				byteLabelColumnWidth = max(byteLabelColumnWidth, label.boundingBox.width)
				x = 0.0
				y += digitView.height.toInt()
			} else if (i % representation.digitGroupSize == 0 && i > 0) {
				x += DIGIT_GROUP_GAP
			}

			bounds.add(digitView.bounds)
		}
		bounds.expandLeftBy(byteLabelColumnWidth)
		setBounds(bounds)
	}
}