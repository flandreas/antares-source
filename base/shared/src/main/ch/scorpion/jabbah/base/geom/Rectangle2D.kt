package ch.scorpion.jabbah.base.geom

/**
 * The [Rectangle2D] class describes a rectangle defined by a location (x,y) and dimension (width x height).
 */
data class Rectangle2D(
    override var x: Double = 0.0,
    override var y: Double = 0.0,
    override var width: Double = 0.0,
    override var height: Double = 0.0
) : AbstractRectangularShape(x, y, width, height) {

    constructor(x: Int, y: Int, w: Int, h: Int): this(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())
    constructor(rect: RectangularShape) : this(rect.x, rect.y, rect.width, rect.height)
    constructor(location: Point2D, width: Double, height: Double) : this(location.x, location.y, width, height)

	companion object {

		/** Mutable object, cannot use singe instance.*/
		val ZERO: Rectangle2D get() =  Rectangle2D(0, 0, 0, 0)

		fun pointLike(point: Point2D): Rectangle2D = Rectangle2D(point, 0.0, 0.0)

		fun withCenter(center: Point2D, w: Double, h: Double): Rectangle2D = Rectangle2D(center.x - w / 2, center.y - h / 2, w, h)
	}
}