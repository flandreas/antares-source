package io.antarescircuit.jabbah.graph.view

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rotation
import io.antarescircuit.jabbah.draw.drawable.RectangularDrawable
import io.antarescircuit.jabbah.draw.graphics.Font
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.edit.Component

/**
 * Implemented by [Component]s that feature an internal label.
 */
interface InternallyLabeled : RectangularDrawable {
    val internalLabel: Label?
    val internalLabelFont: Font
    val rotation: Rotation

    fun setInternalLabelLocation(location: Point2D) {
        internalLabel?.location = location
    }
}