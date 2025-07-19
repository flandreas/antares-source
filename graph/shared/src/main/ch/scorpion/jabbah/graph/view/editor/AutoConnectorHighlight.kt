package ch.scorpion.jabbah.graph.view.editor

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointHighlight.Companion.SIZE_HALF
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointHighlightCircle

/**
 * Highlights the connection points of a [VerticeView] that will automatically connect to open
 * [EdgeView]s when dragging the [VerticeView].
 *
 * Uses [ConnectionPointHighlightCircle] for rendering.
 */
class AutoConnectorHighlight : AbstractDrawable() {

    /** Holds the currently highlighted points.*/
    private val points = mutableListOf<Point2D>()

    /** Holds the bounding box in model coordinate space.*/
    private val bboxModel = Rectangle2D()

    /** ---- [AbstractDrawable] */

    override val boundingBox: RectangularShape get() = bboxModel

    override fun draw(context: DrawContext) {
    	for (p in points) {
    		ConnectionPointHighlightCircle.drawNormalViewAt(p, context)
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

    private fun getPointBoundingBox(p: Point2D): Rectangle2D =
	    Rectangle2D(p.x - SIZE_HALF, p.y - SIZE_HALF, 2.0 * SIZE_HALF, 2.0 * SIZE_HALF)
}