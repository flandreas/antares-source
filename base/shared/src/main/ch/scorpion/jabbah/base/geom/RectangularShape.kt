package ch.scorpion.jabbah.base.geom

/**
 * A [RectangularShape] is a [Shape] whose geometry is defined by a rectangular frame.
 */
interface RectangularShape : Shape {

    /** ---- [Shape] */

    override fun contains(x: Double, y: Double): Boolean =
        x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height

    override fun contains(x: Double, y: Double, width: Double, height: Double): Boolean {
        if (isEmpty) {
            return false
        }
        return x >= this.x
            && x + width <= maxX
            && y >= this.y
            && y + height <= maxY
    }

    /** ---- [RectangularShape] */

    val x: Double
    val y: Double
    val width: Double
    val height: Double

	val widthInt: Int get() = width.toInt()
	val heightInt: Int get() = height.toInt()

    /** Determines whether this [RectangularShape]'s geometry property are all zero.*/
    val isInitial: Boolean get() = isEmpty && x == 0.0 && y == 0.0

    /** Determines whether this [RectangularShape] encloses no area.*/
    val isEmpty: Boolean get() = width <= 0 && height <= 0

    /** Contains the x coordinate of this [RectangularShape]'s center.  */
    val centerX: Double get() = x + width / 2.0

    /** Contains the y coordinate of this [RectangularShape]'s center.*/
    val centerY: Double get() = y + height / 2.0

    /** Contains the minimum x coordinate of this [RectangularShape].*/
    val minX: Double get() = x

    /** Contains the minimum y coordinate of this [RectangularShape].*/
    val minY: Double get() = y

    /** Contains the maximum x coordinate of this [RectangularShape].*/
    val maxX: Double get() = x + width

    /** Contains the maximum y coordinate of this [RectangularShape].*/
    val maxY: Double get() = y + height

	val center: Point2D get() = Point2D(centerX, centerY)

	/** Contains the top-left edge of this [RectangularShape].*/
	val topLeft: Point2D get() = Point2D(minX, minY)

    val topRight: Point2D get() = Point2D(maxX, minY)

	val topCenter: Point2D get() = Point2D(centerX, minY)

	val bottomLeft: Point2D get() = Point2D(minX, maxY)

	/** Contains the bottom-right edge of this [RectangularShape].*/
	val bottomRight: Point2D get() = Point2D(maxX, maxY)

	val bottomCenter: Point2D get() = Point2D(centerX, maxY)

	val centerLeft: Point2D get() = Point2D(minX, centerY)

	val centerRight: Point2D get() = Point2D(maxX, centerY)

    val dimension: Dimension2D get() = Dimension2D(width, height)
}