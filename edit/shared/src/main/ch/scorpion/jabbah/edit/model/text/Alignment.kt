package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.geom.Direction
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
            for (alignment in values()) {
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
        LEFT -> Translations.getString("edit.property.horizontalAlignment.left.name")
        CENTER -> Translations.getString("edit.property.horizontalAlignment.center.name")
        RIGHT -> Translations.getString("edit.property.horizontalAlignment.right.name")
    }
}

enum class VerticalAlignment(val customName: String) {
    TOP("top") {
        override fun opposite(): VerticalAlignment = BOTTOM
        override fun getY(baselineRect: Rectangle2D): Double = 0.0
    },

	CENTER("center") {
		override fun opposite(): VerticalAlignment = CENTER
		override fun getY(baselineRect: Rectangle2D): Double = baselineRect.height / 2
	},

	BOTTOM("bottom") {
        override fun opposite(): VerticalAlignment = TOP
        override fun getY(baselineRect: Rectangle2D): Double = baselineRect.height
    };

    companion object {
        fun withName(name: String): VerticalAlignment {
            for (alignment in values()) {
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
        TOP -> Translations.getString("edit.property.verticalAlignment.top.name")
        CENTER -> Translations.getString("edit.property.verticalAlignment.center.name")
        BOTTOM -> Translations.getString("edit.property.verticalAlignment.bottom.name")
    }
}

/** Used to update horizontal and vertical alignment at once. */
data class Alignment(val horizontal: HorizontalAlignment, val vertical: VerticalAlignment) {

	companion object {

		fun forOrientation(orientation: Direction): Alignment {
			return when (orientation) {
				Direction.EAST -> {
					Alignment(HorizontalAlignment.RIGHT, VerticalAlignment.CENTER)
				}
				Direction.NORTH -> {
					Alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP)
				}
				Direction.WEST -> {
					Alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
				}
				Direction.SOUTH -> {
					Alignment(HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM)
				}
			}
		}
	}
}