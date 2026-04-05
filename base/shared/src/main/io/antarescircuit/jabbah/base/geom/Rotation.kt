package io.antarescircuit.jabbah.base.geom

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min

/**
 * Represents the main four orthogonal rotations in a plane.
 */
enum class Rotation(val customName: String, val angle: Double) {
    R0("0", 0.0),
    R90("90", 3 * PI / 2),
    R180("180", PI),
    R270("270", PI / 2);

    /** Holds the equivalent [AffineTransform] of this [Rotation].*/
    private val transform: AffineTransform

    init {
        transform = AffineTransformImpl()
        transform.rotate(angle)
    }

    companion object {
        fun withName(name: String): Rotation {
            for (value in values()) {
                if (value.customName == name) {
                    return value
                }
            }
            throw IllegalArgumentException("unknown Rotation $name")
        }
    }

    /** Returns the next clockwise [Rotation].*/
    fun next(): Rotation = values()[(this.ordinal + 1) % 4]

	fun previous(): Rotation = values()[(this.ordinal + 3) % 4]

    /** Returns the opposite of this [Rotation].*/
    fun opposite(): Rotation = values()[(this.ordinal + 2) % 4]

    fun inverse(): Rotation =
    	when (this) {
	        R0 -> R0
	        R90 -> R270
	        R180 -> R180
	        R270 -> R90
	    }

	fun add(other: Rotation): Rotation = values()[(this.ordinal + other.ordinal) % 4]

    /** Rotates a [Point2D] with coordinates expressed as relative (0, 0).*/
    @Suppress("unused")
    fun rotatePoint(p: Point2D): Point2D = rotatePoint(p.x, p.y)

    /** Rotates a (x,y) point with coordinates expressed as relative (0, 0).*/
    fun rotatePoint(x: Double, y: Double): Point2D =
    	when (this) {
	        R0 -> Point2D(x, y)
	        R90 -> Point2D(y, -x)
	        R180 -> Point2D(-x, -y)
	        R270 -> Point2D(-y, x)
	    }

    /** Rotates a (x,y) point around the specified rotation center (pivot).*/
    fun rotatePointAround(pivot: Point2D, x: Double, y: Double): Point2D {
        val p = rotatePoint(x - pivot.x, y - pivot.y)
        return Point2D(pivot.x + p.x, pivot.y + p.y)
    }

	fun rotatePointAround(pivot: Point2D, point: Point2D): Point2D =
		rotatePointAround(pivot, point.x, point.y)

    /**
     * Rotates a [RectangularShape] around the specified pivot point (rotation center),
     * both being defined in the same global coordinate system.
     */
    fun rotateRectangleAround(pivot: Point2D, rect: RectangularShape): Rectangle2D {
        val p1 = rotatePoint(rect.x - pivot.x, rect.y - pivot.y)
        val p2 = rotatePoint(rect.x + rect.width - pivot.x, rect.y + rect.height - pivot.y)

        val newRect = Rectangle2D(
                min(p1.x, p2.x),
                min(p1.y, p2.y),
                abs(p1.x - p2.x),
                abs(p1.y - p2.y))

        newRect.setFrame(
                newRect.x + pivot.x, newRect.y + pivot.y,
                newRect.width, newRect.height)

        return newRect
    }

    /** Rotates the specified [Direction] by as many degrees as this [Rotation] represents.*/
    fun rotateDirection(dir: Direction): Direction {
        var i = 0
        var result = dir
        while (i < ordinal) {
            result = result.next()
            i++
        }
        return result
    }
}