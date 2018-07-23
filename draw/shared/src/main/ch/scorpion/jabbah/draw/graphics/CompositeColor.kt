package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.Math
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

    val disabledTextColor: Color get() = Color(
            Math.ceil((backgroundColor.red + textColor.red) / 2.0).toInt(),
            Math.ceil((backgroundColor.green + textColor.green) / 2.0).toInt(),
            Math.ceil((backgroundColor.blue + textColor.blue) / 2.0).toInt()
    )


    /**
     * Creates a new [CompositeColor] by exchanging [foregroundColor] and [backgroundColor] of this [CompositeColor],
     * and setting the [textColor] from the new [foregroundColor].
     */
    fun exchange(): CompositeColor = CompositeColor(backgroundColor, foregroundColor, backgroundColor)

	/** Creates a new [CompositeColor] having the same color than this one, except the specified foreground color.*/
	fun withForeground(color: Color): CompositeColor = CompositeColor(color, backgroundColor, textColor)

	/** Creates a new [CompositeColor] having the same color than this one, except the specified background color.*/
    fun withBackground(color: Color): CompositeColor = CompositeColor(foregroundColor, color, textColor)

	/** Creates a new [CompositeColor] having the same color than this one, except the specified text color.*/
	fun withTextColor(color: Color): CompositeColor = CompositeColor(foregroundColor, backgroundColor, color)

	fun withAlpha(alpha: Int): CompositeColor = CompositeColor(foregroundColor.withAlpha(alpha), backgroundColor.withAlpha(alpha), textColor.withAlpha(alpha))

}