package ch.scorpion.jabbah.base.geom

/**
 * Represents a dimension in 2D space.
 */
data class Dimension2D(val width: Double, val height: Double) {
    constructor(width: Int, height: Int): this(width.toDouble(), height.toDouble())

	val widthInt: Int = width.toInt()
	val heightInt: Int = height.toInt()
}