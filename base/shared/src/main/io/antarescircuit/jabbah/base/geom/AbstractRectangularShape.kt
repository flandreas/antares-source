package io.antarescircuit.jabbah.base.geom

/**
 * Base implementation of the [RectangularShape] interface.
 */
abstract class AbstractRectangularShape(
    override var x: Double,
    override var y: Double,
    override var width: Double,
    override var height: Double
) : MutableRectangularShape {

    @Suppress("unused") constructor() : this(0.0, 0.0, 0.0, 0.0)
    @Suppress("unused") constructor(x: Int, y: Int, w: Int, h: Int): this(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())
    @Suppress("unused") constructor(rect: RectangularShape) : this(rect.x, rect.y, rect.width, rect.height)
    @Suppress("unused") constructor(location: Point2D, width: Double, height: Double) : this(location.x, location.y, width, height)

    companion object {

        /** The bit-mask that indicates that a point lies to the left of this [RectangularShape].*/
        const val OUT_LEFT = 1

        /** The bit-mask that indicates that a point lies above this [RectangularShape].*/
        const val OUT_TOP = 2

        /** The bit-mask that indicates that a point lies to the right of this [RectangularShape].*/
        const val OUT_RIGHT = 4

        /** The bit-mask that indicates that a point lies below this [RectangularShape].*/
        const val OUT_BOTTOM = 8
    }

    /** ---- [Shape] interface */

    override val boundingBox: RectangularShape get() = this

    override fun setFrame(x: Double, y: Double, width: Double, height: Double) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }

    override fun intersects(x: Double, y: Double, w: Double, h: Double): Boolean {
        if (isEmpty || w <= 0 || h <= 0) {
            return false
        }
        return x + w > this.x && y + h > this.y && x < this.x + width && y < this.y + height
    }

    /** ---- [AbstractRectangularShape] */

    val bounds: RectangularShape get() = this

    /**
     * Determines whether the specified point lies with respect to this [Rectangle2D].
     * @returns a binary OR of the `OUT_xxx` bitmasks
     */
    fun outcode(x: Double, y: Double): Int {
        var out = 0
	    when {
		    this.width <= 0 -> {
			    out = out or (OUT_LEFT or OUT_RIGHT)
		    }
		    x < this.x -> {
			    out = out or OUT_LEFT
		    }
		    x > this.x + this.width -> {
			    out = out or OUT_RIGHT
		    }
	    }
	    when {
		    this.height <= 0 -> {
			    out = out or (OUT_TOP or OUT_BOTTOM)
		    }
		    y < this.y -> {
			    out = out or OUT_TOP
		    }
		    y > this.y + this.height -> {
			    out = out or OUT_BOTTOM
		    }
	    }
        return out
    }

    fun intersectsLine(xx1: Double, yy1: Double, x2: Double, y2: Double): Boolean {
        var x1 = xx1
        var y1 = yy1
        val out2 = outcode(x2, y2)
        if (out2 == 0) {
            return true
        }
        var out1= outcode(x1, y1)
        while (out1 != 0) {
            if (out1 and out2 != 0) {
                return false
            }
            if (out1 and (OUT_LEFT or OUT_RIGHT) != 0) {
                var x = x
                if (out1 and OUT_RIGHT != 0) {
                    x += width
                }
                y1 += (x - x1) * (y2 - y1) / (x2 - x1)
                x1 = x
            } else {
                var y = y
                if (out1 and OUT_BOTTOM != 0) {
                    y += height
                }
                x1 += (y - y1) * (x2 - x1) / (y2 - y1)
                y1 = y
            }
            out1= outcode(x1, y1)
        }
        return true
    }
}
