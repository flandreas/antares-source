package io.antarescircuit.jabbah.edit.model.curve

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.geom.Path
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.Component

/**
 * A [Component] consisting of 4 points defining a cubic curve as of [Path.curveTo].
 * The first and the fourth point represent the endpoints of the curve, while the
 * second and third point represent the "control points", defining the curvature of the curve.
 */
class CubicCurveComponent(points: List<Point2D> = DEFAULT_POINTS) : AbstractCurveComponent(points){

    companion object {
        private val type = Translations.getString("edit.component.cubicCurve")
        private val DEFAULT_POINTS = listOf(
            Point2D(0, 0),
            Point2D(100, 100),
            Point2D(200, 100),
            Point2D(200, 0)
        )
    }

    override val type: String get() = CubicCurveComponent.type

    override val pointsCount: Int get() = 4

    override fun updatePath() {
        check(points.size == pointsCount)
        path = System.createPath()
        path.moveTo(points[0].x, points[0].y)
        path.curveTo(points[1].x, points[1].y, points[2].x, points[2].y, points[3].x, points[3].y)
    }
}