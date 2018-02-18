package ch.scorpion.jabbah.base.geom


/**
 * The [Shape] interface provides definitions for objects that represent some form of geometric shape.
 */
interface Shape {

    /** Returns a copy of the rectangular region that entirely encloses this [Shape].*/
    val boundingBox: RectangularShape

    /** Tests if the specified coordinates are inside the boundary of this [Shape].*/
    fun contains(x: Double, y: Double): Boolean

    /** Tests if the specified [Point2D] is inside the boundary of this [Shape].*/
    fun contains(p: Point2D): Boolean
        = contains(p.x, p.y)

    /** Tests if the interior of this [Shape] entirely contains the specified rectangular area.*/
    fun contains(x: Double, y: Double, width: Double, height: Double): Boolean

    /** Tests if the interior of this [Shape] entirely contains the specified [Rectangle2D].*/
    fun contains(rect: Rectangle2D): Boolean = contains(rect.x, rect.y, rect.width, rect.height)

    /** Tests if the interior of this [Shape] intersects the interior of the specified rectangular area.*/
    fun intersects(x: Double, y: Double, w: Double, h: Double): Boolean

}