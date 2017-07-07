package ch.scorpion.jabbah.base.geom

/**
 * The [Rectangle2D] class describes a rectangle defined by a location (x,y) and dimension (width x height).
 */
data class Rectangle2D(
    override var x: Double,
    override var y: Double,
    override var width: Double,
    override var height: Double
) : AbstractRectangularShape(x, y, width, height) {

    constructor() : this(0.0, 0.0, 0.0, 0.0)
    constructor(x: Int, y: Int, w: Int, h: Int): this(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())
    constructor(rect: RectangularShape) : this(rect.x, rect.y, rect.width, rect.height)
    constructor(location: Point2D, width: Double, height: Double) : this(location.x, location.y, width, height)
}