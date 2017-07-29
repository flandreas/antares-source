package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.graphics.CompositeColor
/**
 * A [Colorable] is a [Drawable] whose [CompositeColor] can be controlled from outside.
 */
interface Colorable {
    var color: CompositeColor
}