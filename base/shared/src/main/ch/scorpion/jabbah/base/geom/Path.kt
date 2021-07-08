package ch.scorpion.jabbah.base.geom

/**
 * The [Path] interface provides a simple, yet flexible shape which represents an arbitrary geometric path.
 */
interface Path : Shape {

    /** Adds a point to the path by moving to the specified location.*/
    fun moveTo(x: Double, y: Double): Path

    fun moveTo(x: Int, y: Int): Path = moveTo(x.toDouble(), y.toDouble())

    fun moveTo(x: Float, y: Float): Path = moveTo(x.toDouble(), y.toDouble())

    /** Adds a point to the path by drawing a straight line from the current location to the specified location.*/
    fun lineTo(x: Double, y: Double): Path

    fun lineTo(x: Int, y: Int): Path = lineTo(x.toDouble(), y.toDouble())

    fun lineTo(x: Float, y: Float): Path = lineTo(x.toDouble(), y.toDouble())

    /**
     * Adds a curved segment, defined by two new points, to the path by drawing a Quadratic curve that intersects
     * both the current coordinates and the specified coordinates (x2,y2), using the specified point (x1,y1)
     * as a quadratic parametric control point.
     */
    fun quadTo(x1: Double, y1: Double, x2: Double, y2: Double): Path

    fun quadTo(x1: Int, y1: Int, x2: Int, y2: Int): Path = quadTo(x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble())

    /**
     * Adds a curved segment, defined by three new points, to the path by drawing a Bézier curve that intersects
     * both the current coordinates and the specified coordinates (x3,y3), using the specified points
     * (x1,y1) and (x2,y2) as Bézier control points.
     */
    fun curveTo(x1: Double, y1: Double, x2: Double, y2: Double, x3: Double, y3: Double): Path

    fun curveTo(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int, y3: Int): Path
        = curveTo(x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble(), x3.toDouble(), y3.toDouble())

    /*
    * Closes the current subpath by drawing a straight line back to the coordinates of the last [moveTo].
    * If the path is already closed then this method has no effect.
    */
    fun close(): Path

    /** Transforms the geometry of this [Path] using the specified [AffineTransform].*/
    fun transform(transform: AffineTransform)

}