package ch.scorpion.jabbah.edit.snap

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.ZoomPan
import ch.scorpion.jabbah.draw.style.Style
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType

/**
 * Paints a simple dot for each grid dot.
 */
class DottedGridPainter(private val styleProvider: StyleProvider) : GridPainter {

    val LOG by logger(DottedGridPainter::class)

    /** ---- [GridPainter] interface */

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
            var y = low.y
            while (y <= high.y) {
                context.g.drawDot(x.toInt(), y.toInt())
                y += dy
            }
            x += dx
        }

        context.g.color = oldColor
    }
}