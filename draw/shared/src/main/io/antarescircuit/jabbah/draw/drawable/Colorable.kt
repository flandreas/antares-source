package io.antarescircuit.jabbah.draw.drawable

import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
/**
 * A [Colorable] is a [Drawable] whose [CompositeColor] can be controlled from outside.
 */
interface Colorable {
    var color: CompositeColor
}