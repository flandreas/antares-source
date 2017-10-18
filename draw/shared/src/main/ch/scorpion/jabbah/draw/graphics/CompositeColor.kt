package ch.scorpion.jabbah.draw.graphics

/**
 * A [CompositeColor] is a defined set of harmonic colors to be used for drawing graphical objects.
 *
 * @param foregroundColor the [Color] for drawing the border of a graphical object
 * @param backgroundColor the [Color] for drawing the background of a graphical object
 * @param textColor the [Color] for drawing text above the interior of a graphical object
 */
data class CompositeColor(
        val foregroundColor: Color = Color.BLACK,
        val backgroundColor: Color = Color.WHITE,
        val textColor: Color = foregroundColor
) {

    /**
     * Creates a new [CompositeColor] by exchanging [foregroundColor] and [backgroundColor] of this [CompositeColor],
     * and setting the [textColor] from the new [foregroundColor].
     */
    fun exchange(): CompositeColor = CompositeColor(backgroundColor, foregroundColor, backgroundColor)
}