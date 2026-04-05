package io.antarescircuit.antares.view.signal

import io.antarescircuit.antares.model.signal.*
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.base.event.PropertyOwner
import io.antarescircuit.jabbah.base.event.PropertyOwnerImpl
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.AbstractRectangle
import io.antarescircuit.jabbah.draw.drawable.Transparent
import io.antarescircuit.jabbah.draw.drawable.TransparentImpl
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment.LEFT
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment.RIGHT
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment.BOTTOM
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment.CENTER
import kotlin.math.max

/**
 * Displays a digital number with individual [DigitView]s.
 */
class NumberView(
	representation: DigitalSignalRepresentation,
	val bitWidth: BitWidth,
	drawDigitBorder: Boolean = true,
	drawBox: Boolean = true,
	private val propertyOwner: PropertyOwner<Any> = PropertyOwnerImpl()
) : AbstractRectangle(), Transparent, PropertyOwner<Any> by propertyOwner {

	companion object {
		private const val DIGIT_GROUP_GAP = 5
		private const val BYTE_LABEL_HOR_GAP = 1
		private const val SIGNAL_NOTATION_LABEL_GAP = 1

		/** The name of the [focusIndex] property as of [PropertyOwner].*/
		const val PROP_FOCUS_INDEX = "PROP_FOCUS_INDEX"
	}

	/** Contains the individual digit views, starting with the lowest priority bit at index 0. */
	private val digitViews = mutableListOf<DigitView>()

	/** Contains the [Label]s that designate the index of the displayed byte within the entire signal value. */
	private val byteIndexLabels = mutableListOf<Label>()

	private lateinit var signalNotationLabel: Label

	/** The index of the [digitViews] that has the focus, or `null` if none has the focus. */
	var focusIndex: Int? = null
		private set(value) {
			if (field != value) {
				val oldValue = field
				field = value
				propertyOwner.fire(PROP_FOCUS_INDEX, oldValue, value)
			}
		}

	init {
		propertyOwner.source = this
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

	/**
	 * @param textColor enforces the text color from the outside context in special situations,
	 * else uses the default coloring depending on the signal
	 */
	fun draw(context: DrawContext, isOn: Boolean, inactive: Boolean = false, textColor: Color? = null, focusColor: Color? = null) {
		context.translated(location) {
			for (digitView in digitViews) {
				digitView.draw(it, isOn, inactive, textColor, focusColor, enforceSingleBitColor = bitWidth.width == 1)
			}
			drawByteIndexLabels(it)

			if (digitViews.size > 1) {
				signalNotationLabel.draw(it)
			}
		}
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

	fun getDigitSignal(digitIndex: Int): DigitalSignal =
		digitViews[digitIndex].signalDigit

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
		var x: Double
		var y = 0.0
		var byteLabelColumnWidth = 0.0

		val maxDigitPerRow = 2 * representation.digitGroupSize
		val digitViewCount = representation.digitCount(bitWidth)

		createSignalNotationLabel(representation)

		// First row inset
		if (digitViewCount > maxDigitPerRow && digitViewCount % maxDigitPerRow != 0) {
			val insetDigitCount = maxDigitPerRow - (digitViewCount % maxDigitPerRow)
			var inset = insetDigitCount * DigitView.WIDTH
			if (insetDigitCount >= representation.digitGroupSize) {
				inset += DIGIT_GROUP_GAP
			}
			x = inset.toDouble()
		} else {
			x = 0.0
		}

		for (i in digitViewCount - 1 downTo 0) {
			val digitView = DigitView(representation, i, x, y, drawDigitBorder, drawBox)
			digitViews.add(0, digitView)
			x += digitView.width.toInt()

			if (digitViewCount > maxDigitPerRow && i % maxDigitPerRow == 0) {
				// Row break

				val label = when (CurrentDigitalSignalNotation.notation) {
					DigitalSignalNotation.PREFIX -> createByteIndexLabel(i, LEFT, Point2D(x + BYTE_LABEL_HOR_GAP, y + digitView.height))
					else -> createByteIndexLabel(i, RIGHT, Point2D(bounds.minX - BYTE_LABEL_HOR_GAP, y + digitView.height))
				}
				byteIndexLabels.add(label)
				byteLabelColumnWidth = max(byteLabelColumnWidth, label.boundingBox.width)

				x = 0.0
				y += digitView.height.toInt()
			} else if (i % representation.digitGroupSize == 0 && i > 0) {
				x += DIGIT_GROUP_GAP
			}

			bounds.add(digitView.bounds)
		}

		if (CurrentDigitalSignalNotation.notation != DigitalSignalNotation.PREFIX) {
			moveChildrenDeltaX(byteLabelColumnWidth)
		}
		bounds.expandLeftBy(byteLabelColumnWidth)

		if (digitViews.size > 1) {
			placeSignalNotationLabel(bounds)
		}

		setBounds(bounds)
	}

	private fun placeSignalNotationLabel(bounds: Rectangle2D) {
		val signalNotationLabelColumnWidth = signalNotationLabel.bounds.width + SIGNAL_NOTATION_LABEL_GAP - 2
		when (CurrentDigitalSignalNotation.notation) {
			DigitalSignalNotation.PREFIX -> {
				// place at the left side of the last DigitView
				val lastDigitView = digitViews.last()
				signalNotationLabel.location = Point2D(lastDigitView.x - SIGNAL_NOTATION_LABEL_GAP, lastDigitView.bounds.centerY)
				moveChildrenDeltaX(signalNotationLabelColumnWidth)
				bounds.expandLeftBy(signalNotationLabelColumnWidth)
			}
			else -> {
				// place at the right side of the first DigitView
				var firstDigitView = digitViews.first()
				signalNotationLabel.location = Point2D(firstDigitView.bounds.maxX + SIGNAL_NOTATION_LABEL_GAP, firstDigitView.bounds.centerY)
				bounds.expandLeftBy(signalNotationLabelColumnWidth)
			}
		}
	}

	private fun moveChildrenDeltaX(deltaX: Double) {
		digitViews.forEach { it.moveBy(deltaX, 0.0) }
		byteIndexLabels.forEach { it.location = it.location.add(deltaX, 0.0) }
		signalNotationLabel.location = signalNotationLabel.location.add(deltaX, 0.0)
	}

	private fun createByteIndexLabel(i: Int, horizontalAlignment: HorizontalAlignment, location: Point2D): Label {
		return Label(
			text = i.toString(),
			location = location,
			font = Look.EXT_PIN_FONT,
			color = Themes.get<AntaresTheme>().vertice.color.textColor,
			horizontalAlignment = horizontalAlignment,
			verticalAlignment = BOTTOM)
	}

	private fun createSignalNotationLabel(representation: DigitalSignalRepresentation) {
		signalNotationLabel = Label(
			text = CurrentDigitalSignalNotation.notation.notate(representation),
			font = DigitView.FONT,
			color = Themes.get<AntaresTheme>().vertice.color.textColor,
			horizontalAlignment = when (CurrentDigitalSignalNotation.notation) {
				DigitalSignalNotation.PREFIX -> RIGHT
				else -> LEFT
			},
			verticalAlignment = CENTER
		)
	}
}