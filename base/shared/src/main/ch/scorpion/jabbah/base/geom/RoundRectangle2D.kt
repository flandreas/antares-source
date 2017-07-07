package ch.scorpion.jabbah.base.geom

/**
 * A [Rectangle2D] with round corners.
 */
class RoundRectangle2D(
    x: Double,
    y: Double,
    width: Double,
    height: Double,
    val arcW: Double,
    val arcH: Double
) : AbstractRectangularShape(x, y, width, height) {
}