package ch.scorpion.jabbah.base.geom

/**
 * Represents an ellipse.
 */
class Ellipse2D(
    override var x: Double = 0.0,
    override var y: Double = 0.0,
    override var width: Double = 0.0,
    override var height: Double = 0.0
) : AbstractRectangularShape(x, y, width, height) {

    constructor(x: Int, y: Int, w: Int, h: Int): this(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())
    constructor(rect: RectangularShape) : this(rect.x, rect.y, rect.width, rect.height)
    constructor(location: Point2D, width: Double, height: Double) : this(location.x, location.y, width, height)
    constructor(center: Point2D, radius: Double): this(center.x - radius, center.x - radius, 2 * radius, 2 * radius)

    /** ---- [RectangularShape] */

    override fun contains(x: Double, y: Double): Boolean {
        if (width <= 0.0) {
            return false
        }
        val xNorm = (x - this.x) / width - 0.5
        if (height <= 0.0) {
            return false
        }
        val yNorm = (y - this.y) / height - 0.5
        return xNorm * xNorm + yNorm * yNorm < 0.25
    }

    override fun contains(x: Double, y: Double, width: Double, height: Double): Boolean {
        return contains(x, y) &&
            contains(x + width, y) &&
            contains(x, y + height) &&
            contains(x + width, y + height)
    }
}