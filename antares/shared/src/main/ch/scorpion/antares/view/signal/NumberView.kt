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
import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.draw.style.Themes

/**
 * Displays a digital number with individual [DigitView]s.
 */
class NumberView(
    representation: DigitalSignalRepresentation,
    bitWidth: BitWidth
) : AbstractRectangle() {

    companion object {
        val NIBBLE_GAP = 5
        val BYTE_LABEL_HOR_GAP = 0
    }

    /** Contains the individual digit views, starting with the lowest priority bit a index 0. */
    private val digitViews = mutableListOf<DigitView>()

    /** Contains the [Label]s that designate the index of the displayed byte within the entire signal value. */
    private val byteIndexLabels = mutableListOf<Label>()

    /** The index of the [digitView] that has the focus, or `null` if none has the focus. */
    var focusIndex: Int? = null
        private set

    init {
        buildUI(representation, bitWidth)
    }

    /** ---- [AbstractRectangle] */

    override val lineWidth: Double get() = 0.0

    override fun draw(context: DrawContext) {
        draw(context, true)
    }

    fun draw(context: DrawContext, isOn: Boolean) {
        context.g.translate(x, y)
        for (digitView in digitViews) {
            digitView.draw(context, isOn)
        }
        if (context.useContextColors) {
            context.g.color = context.color!!.textColor
        }
        for (label in byteIndexLabels) {
            label.draw(context)
        }
        context.g.translate(-x, -y)
    }

    /** ---- [NumberView] */

    /** Returns the number of {@link DigitView} that this {@link NumberView} displays.*/
    val digitCount: Int get() = digitViews.size

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
        var i = 0
        for (digitView in digitViews) {
            if (digitView.contains(x, y)) {
                return i
            }
            i++
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
        updateFocusIndex(newFocusIndex)
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

    private fun buildUI(representation: DigitalSignalRepresentation, bitWidth: BitWidth) {
        val bounds = Rectangle2D(0, 0, 0, 0)
        var x = 0.0
        var y = 0.0
        val max = Math.max(1, bitWidth.width / representation.bits()) - 1
        for (i in max downTo 0) {
            val digitView = DigitView(representation, i, x.toDouble(), y.toDouble())
            digitViews.add(0, digitView)
            x += digitView.width.toInt()

            if (max > 8 && i % 8 == 0) {
                byteIndexLabels.add(Label(
                        text = i.toString(),
                        location = Point2D(x + BYTE_LABEL_HOR_GAP, y + digitView.height),
                        font = Look.EXT_PIN_FONT,
                        color = Themes.get<AntaresTheme>().vertice.color.textColor,
                        horizontalAlignment = Label.HorizontalAlignment.LEFT,
                        verticalAlignment = Label.VerticalAlignment.BOTTOM))
                x = 0.0
                y += digitView.height.toInt()
            } else if (i % 4 == 0 && i > 0) {
                x += NIBBLE_GAP
            }

            bounds.add(digitView.bounds)
        }
        setBounds(bounds)
    }
}