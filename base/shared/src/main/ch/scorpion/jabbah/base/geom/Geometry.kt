package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.base.MathClass

/**
 * A utility class providing geometry methods.
 */
object Geometry {

    /** Calculate the angle between two lines in radians.*/
    fun angle(p1: Point2D, p2: Point2D): Double {
        return angle(p1.x, p1.y, p2.x, p2.y)
    }

    /** Calculate the angle between two lines in radians.*/
    fun angle(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        val fx = if (x2 > x1) 1 else -1
        val fy = if (y2 > y1) -1 else 1
        var angle: Double

        if (x1 == x2 && y1 == y2) {
            return 0.0
        }

        if (Math.abs(x2 - x1) < 0.0001) {
            if (fy == 1)
                angle = MathClass.PI / 2
            else
                angle = 3 * MathClass.PI / 2
        } else {
            angle = Math.atan(Math.abs(y2 - y1) / Math.abs(x2 - x1))
            if (fx == 1) {
                if (fy == -1)
                    angle = 2 * MathClass.PI - angle
            } else {
                if (fy == 1)
                    angle = MathClass.PI - angle
                else
                    angle = MathClass.PI + angle
            }
        }
        return angle
    }

    fun normal(x1: Double, y1: Double, x2: Double, y2: Double): Point2D {
        return Point2D(-(y2 - y1), (x2 - x1))
    }
}