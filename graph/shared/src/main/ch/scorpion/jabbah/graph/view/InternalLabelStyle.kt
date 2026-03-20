package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment

/** Represents the supported styles for the [Label] of an [LabeledRectangularVerticeView].*/
enum class InternalLabelStyle {

    /**
     * Positions the [Label] within the box by centering it horizontally and placing it
     * at one-third of the height.
     */
    LARGE_CENTERED {
        override fun updateLabel(box: InternallyLabeled) {
            box.internalLabel?.let {
                it.font = box.internalLabelFont
                it.ownerRotation = box.rotation
                it.horizontalAlignment = HorizontalAlignment.CENTER
                it.verticalAlignment = VerticalAlignment.CENTER
                it.location = Point2D(box.x + box.width / 2, box.y + box.height / 3)
            }
        }
    },

    SMALL_UPPER_LEFT {
        override fun updateLabel(box: InternallyLabeled) {
            box.internalLabel?.let {
                it.font = deriveFont(box)
                it.ownerRotation = box.rotation
                it.horizontalAlignment = HorizontalAlignment.RIGHT
                it.verticalAlignment = VerticalAlignment.TOP
                it.location = Point2D(box.bounds.maxX - SMALL_LABEL_INSET, box.bounds.minY + SMALL_LABEL_INSET)
            }
        }
    };

    companion object {
        const val SMALL_LABEL_INSET = 3
        const val FONT_SIZE_FACTOR = 0.6
    }

    abstract fun updateLabel(box: InternallyLabeled)

    fun deriveFont(box: InternallyLabeled): Font =
        box.internalLabelFont.deriveFont((box.internalLabelFont.size * FONT_SIZE_FACTOR).toInt())
}