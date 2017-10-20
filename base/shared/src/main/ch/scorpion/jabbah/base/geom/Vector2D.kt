package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.Math

/** A simple implementation of a geometrical vector.*/
class Vector2D(val x: Double, val y: Double) {

    constructor(x: Int, y: Int): this(x.toDouble(), y.toDouble())

    constructor(p: Point2D): this(p.x, p.y)

    val magnitude: Double get() = Math.sqrt(x * x + y * y)

    val normalize: Vector2D
        get() {
            val mag = magnitude
            return Vector2D(x / mag, y / mag)
        }

    fun multiply(s: Double): Vector2D = Vector2D(x * s, y * s)
}