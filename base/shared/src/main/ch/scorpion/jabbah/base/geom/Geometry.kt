package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.PI_2
import ch.scorpion.jabbah.base.SIGMA
import ch.scorpion.jabbah.base.TWO_PI
import kotlin.math.*

/**
 * A utility object providing geometry methods.
 */
object Geometry {

	/** Considers two values as being equal if their difference is not larger than [SIGMA]. */
	fun equal(a: Double, b: Double): Boolean = abs(a - b) <= SIGMA

	/** Enhances [kotlin.math.sign] by considering a value as zero if its absolute value is not larger that [SIGMA].*/
	fun sign(value: Double): Double {
		if (abs(value) <= SIGMA) {
			return 0.0
		}
		return kotlin.math.sign(value)
	}

	/** Wraps an angle in radians to the range 0 .. 2*PI.*/
	fun wrapAngle(angle: Double): Double {
		if (angle < 0) {
			return TWO_PI - abs(angle % TWO_PI)
		}
		return angle % TWO_PI
	}

	/** Determines whether the shortest rotation from one angle to another angle is a clockwise rotation.*/
	fun isClockwiseAngleChange(angle1: Double, angle2: Double): Boolean {
		return angle1 != angle2 && wrapAngle(angle1 - angle2) <= PI
	}

	/**
	 * Calculates the angle (in radians, counter-clockwise) between the horizontal x-axis
	 * and the line defined by the two specified points.
	 */
    fun angle(p1: Point2D, p2: Point2D): Double {
        return angle(p1.x, p1.y, p2.x, p2.y)
    }

	/**
	 * Calculates the angle (in radians, counter-clockwise) between the horizontal x-axis
	 * and the line defined by the two specified points.
	 */
    fun angle(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        val fx = if (x2 > x1) 1 else -1
        val fy = if (y2 > y1) -1 else 1
        var angle: Double

        if (x1 == x2 && y1 == y2) {
            return 0.0
        }

        if (equal(x1, x2)) {
	        angle = if (fy == 1)
		        PI_2
	        else
		        3 * PI_2
        } else {
            angle = atan(abs(y2 - y1) / abs(x2 - x1))
            if (fx == 1) {
                if (fy == -1)
                    angle = 2 * PI - angle
            } else {
	            angle = if (fy == 1)
		            PI - angle
	            else
		            PI + angle
            }
        }
        return angle
    }

    fun normal(x1: Double, y1: Double, x2: Double, y2: Double): Point2D {
        return Point2D(-(y2 - y1), (x2 - x1))
    }

    fun middle(p1: Point2D, p2: Point2D): Point2D {
	    return Point2D(p1.x + (p2.x - p1.y) / 2, p1.y + (p2.y - p1.y / 2))
    }

	fun rotate(x: Double, y: Double, angle: Double): Point2D {
		return Point2D(
			x * cos(angle) - y * sin(angle),
			y * cos(angle) + x * sin(angle))
	}
}