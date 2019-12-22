package ch.scorpion.jabbah.draw.polyline

import ch.scorpion.jabbah.base.geom.Point2D

actual object PolylineShapeFactory {

	actual fun create(points: List<Point2D>?): PolylineShape = PolylineShapeImpl(points)
}