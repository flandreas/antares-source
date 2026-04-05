package io.antarescircuit.jabbah.draw.polyline

import io.antarescircuit.jabbah.base.geom.Point2D

actual object PolylineShapeFactory {

	actual fun create(points: List<Point2D>?): PolylineShape = PolylineShapeImpl(points)
}