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
        val textColor: Color = Color.BLACK)