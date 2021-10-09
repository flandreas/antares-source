package ch.scorpion.jabbah.base.geom

/**
 * A rectangle with round corners.
 */
class RoundRectangle2D(
    x: Double,
    y: Double,
    width: Double,
    height: Double,
    val arcW: Double,
    val arcH: Double
) : AbstractRectangularShape(x, y, width, height) {

	constructor(location: Point2D, dimension: Dimension2D, arc: Double):
		this(location.x, location.y, dimension.width, dimension.height, arc, arc)
}