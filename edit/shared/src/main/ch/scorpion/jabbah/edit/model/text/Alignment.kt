package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.geom.Rectangle2D

enum class HorizontalAlignment(val customName: String) {
    LEFT("left") {
        override fun opposite(): HorizontalAlignment = RIGHT
        override fun getX(baselineRect: Rectangle2D): Double = baselineRect.x
    },

    CENTER("center") {
        override fun opposite() = CENTER
        override fun getX(baselineRect: Rectangle2D): Double = baselineRect.x - baselineRect.width / 2
    },

    RIGHT("right") {
        override fun opposite(): HorizontalAlignment = LEFT
        override fun getX(baselineRect: Rectangle2D): Double = baselineRect.x - baselineRect.width
    };

    companion object {
        fun withName(name: String): HorizontalAlignment {
            for (alignment in HorizontalAlignment.values()) {
                if (alignment.customName == name) {
                    return alignment
                }
            }
            throw IllegalArgumentException("Cannot determine HorizontalAlignment for '$name'")
        }
    }

    abstract fun opposite(): HorizontalAlignment

    /**
     * Returns the x-coordinate of the text shape relative to the [Label]'s location.
     * @param baselineRect the text's shape relative to the baseline.
     * @return the x-coordinate of the text shape.
     */
    abstract fun getX(baselineRect: Rectangle2D): Double

    override fun toString(): String = when (this) {
        HorizontalAlignment.LEFT -> Translations.getString("edit.property.horizontalAlignment.left.name")
        HorizontalAlignment.CENTER -> Translations.getString("edit.property.horizontalAlignment.center.name")
        HorizontalAlignment.RIGHT -> Translations.getString("edit.property.horizontalAlignment.right.name")
    }
}

enum class VerticalAlignment(val customName: String) {
    BOTTOM("bottom") {
        override fun opposite(): VerticalAlignment = TOP
        override fun getY(baselineRect: Rectangle2D): Double = baselineRect.height
    },

    CENTER("center") {
        override fun opposite(): VerticalAlignment = CENTER
        override fun getY(baselineRect: Rectangle2D): Double = baselineRect.height / 2
    },

    TOP("top") {
        override fun opposite(): VerticalAlignment = BOTTOM
        override fun getY(baselineRect: Rectangle2D): Double = 0.0
    };

    companion object {
        fun withName(name: String): VerticalAlignment {
            for (alignment in VerticalAlignment.values()) {
                if (alignment.customName == name) {
                    return alignment
                }
            }
            throw IllegalArgumentException("Cannot determine HorizontalAlignment for '$name'")
        }
    }

    internal abstract fun opposite(): VerticalAlignment

    /**
     * Returns the y-coordinate of the text shape relative to the [Label]'s location.
     * @param baselineRect the text's shape relative to the baseline.
     * @return the y-coordinate of the text shape.
     */
    internal abstract fun getY(baselineRect: Rectangle2D): Double

    override fun toString(): String = when (this) {
        VerticalAlignment.TOP -> Translations.getString("edit.property.verticalAlignment.top.name")
        VerticalAlignment.CENTER -> Translations.getString("edit.property.verticalAlignment.center.name")
        VerticalAlignment.BOTTOM -> Translations.getString("edit.property.verticalAlignment.bottom.name")
    }
}

/** Used to update horizontal and vertical alignment at once. */
data class Alignment(val horizontal: HorizontalAlignment, val vertical: VerticalAlignment)