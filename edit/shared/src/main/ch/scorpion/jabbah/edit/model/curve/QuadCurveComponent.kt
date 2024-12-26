package ch.scorpion.jabbah.edit.model.curve

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Component

/**
 * A [Component] consisting of 3 points defining a quadratic curve as of [Path.quadTo].
 * The first and the third point represent the endpoints of the curve, while the
 * second point represents the "control point", defining the curvature of the curve.
 */
class QuadCurveComponent(points: List<Point2D> = DEFAULT_POINTS) : AbstractCurveComponent(points) {

	companion object {

		private val type = Translations.getString("edit.component.quadraticCurve")
		private val DEFAULT_POINTS = listOf(
			Point2D(0, 0),
			Point2D(100, 100),
			Point2D(200, 0)
		)
	}

	override val type: String get() = QuadCurveComponent.type

	/** ---- [QuadCurveComponent] */

	override val pointsCount: Int get() = 3

	override fun updatePath() {
		check(points.size == pointsCount)
		path = System.createPath()
		path.moveTo(points[0].x, points[0].y)
		path.quadTo(points[1].x, points[1].y, points[2].x, points[2].y)
	}
}