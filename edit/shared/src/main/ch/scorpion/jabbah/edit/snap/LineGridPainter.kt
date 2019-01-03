package ch.scorpion.jabbah.edit.snap

import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.ZoomPan
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.GridPainter

/**
 * Paints a horizontal and a vertical line through each grid point.
 */
class LineGridPainter(private val styleProvider: StyleProvider) : GridPainter {

	companion object {
		const val NAME = "line"
	}

	/** ---- [GridPainter] interface */

	override val name: String get() = NAME

	override var distanceX: Double = 10.0

	override var distanceY: Double = 10.0

	override var zoomPan: ZoomPan? = null

	override fun paint(context: DrawContext, rect: Rectangle2D) {
		val oldColor = context.g.color
		context.g.color = styleProvider.getStyle(StyleType.BACKGROUND).color.foregroundColor

		val dx = distanceX * zoomPan!!.zoomFactor
		val dy = distanceY * zoomPan!!.zoomFactor

		val low: Point2D = zoomPan!!.transform.modelToView(Point2D(
			distanceX * Math.floor(zoomPan!!.transform.viewToModelX(rect.x) / distanceX),
			distanceY * Math.floor(zoomPan!!.transform.viewToModelY(rect.y) / distanceY)))

		val high: Point2D = zoomPan!!.transform.modelToView(Point2D(
			distanceX * Math.ceil(zoomPan!!.transform.viewToModelX(rect.maxX) / distanceX),
			distanceY * Math.ceil(zoomPan!!.transform.viewToModelY(rect.maxY) / distanceY)))

		var x = low.x
		while (x <= high.x) {
			context.g.drawLine(x, low.y, x, high.y)
			x += dx
		}

		var y = low.y
		while ( y <= high.y) {
			context.g.drawLine(low.x, y, high.x, y)
			y += dy
		}

		context.g.color = oldColor
	}
}