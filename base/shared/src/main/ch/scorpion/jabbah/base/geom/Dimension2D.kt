package ch.scorpion.jabbah.base.geom

/**
 * Represents a dimensionSupplier in 2D space.
 */
data class Dimension2D(val width: Double, val height: Double) {
    constructor(width: Int, height: Int): this(width.toDouble(), height.toDouble())
}