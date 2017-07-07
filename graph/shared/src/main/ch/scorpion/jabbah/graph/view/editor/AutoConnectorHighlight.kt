package ch.scorpion.jabbah.graph.view.editor

import ch.scorpion.jabbah.draw.DrawProperties
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.ZoomPan
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.module.DrawModule

/**
 * Highlights the connection points of a [VerticeView] that will automatically connect to open
 * [EdgeView]s when dragging the [VerticeView].
 */
class AutoConnectorHighlight : AbstractDrawable(), Unzoomable {

    companion object {
        /** The name of the [Color] property in [DrawProperties]. */
        val PROP_COLOR = "graph.view.port.highlight.color"

        private val SIZE_HALF = 6

        private val STROKE = Stroke(width = 1f)
    }

    /** Holds the currently highlighted points.*/
    private val points = mutableListOf<Point2D>()

    /** Holds the bounding box in model coordinate space.*/
    private val bboxModel = Rectangle2D()

    /** ---- [Unzoomable] */

    override var zoomPan: ZoomPan? = ZoomPan()

    /** ---- [AbstractDrawable] */

    override val boundingBox: RectangularShape get() = bboxModel

    override fun draw(context: DrawContext) {
        context.g.color = DrawModule.properties.getColor(PROP_COLOR)
        context.g.stroke = STROKE
        for (point in points) {
            val p = zoomPan!!.transform.modelToView(point)
            context.g.drawOval(p.x.toInt() - SIZE_HALF, p.y.toInt() - SIZE_HALF, 2 * SIZE_HALF, 2 * SIZE_HALF)
            context.g.fillOval(p.x.toInt() - SIZE_HALF, p.y.toInt() - SIZE_HALF, 2 * SIZE_HALF, 2 * SIZE_HALF)
        }
    }

    override fun contains(x: Double, y: Double): Boolean = false

    /** ---- [AutoConnectorHighlight] */

    fun setPoints(points: Collection<Point2D>) {
        invalidate()
        this.points.clear()
        this.points.addAll(points)
        updateBoundingBox()
        invalidate()
        update()
    }

    private fun updateBoundingBox() {
        if (points.isEmpty()) {
            bboxModel.setFrame(0.0, 0.0, 0.0, 0.0)
            return
        }
        val iter = points.iterator()
        bboxModel.setFrame(getPointBoundingBox(iter.next()))
        while (iter.hasNext()) {
            bboxModel.add(getPointBoundingBox(iter.next()))
        }
    }

    private fun getPointBoundingBox(p: Point2D): Rectangle2D {
        return Rectangle2D(p.x - SIZE_HALF, p.y - SIZE_HALF, 2.0 * SIZE_HALF, 2.0 * SIZE_HALF)
    }
}