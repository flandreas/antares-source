package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.Component

/**
 * Implemented by [Component]s that feature an internal label.
 */
interface InternallyLabeled : RectangularDrawable {
    val internalLabel: Label?
    val internalLabelFont: Font
    val rotation: Rotation
}