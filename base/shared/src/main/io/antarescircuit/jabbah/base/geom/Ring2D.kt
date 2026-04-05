package io.antarescircuit.jabbah.base.geom

/**
 * A ring is an ellipse with a border of a particular thickness and a transparent inner area.
 * The geometry of a [Ring2D] must respect the rectangular bounding area, thus implementing
 * it like an [Ellipse2D] with a [Stroke] of a particular thickness is not feasible.
 */
class Ring2D(
    x: Double,
    y: Double,
    width: Double,
    height: Double,
    val thickness: Double
) : AbstractRectangularShape(x, y, width, height)