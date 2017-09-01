package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.ViewPainter
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.Math

/**
 * [InvalidatableViewPainter] keeps track of all invalidated areas and repaints only those.
 */
class InvalidatableViewPainter(val view: View<*>) : ViewPainter {

    /** Keeps track of the current accumulated invalid region in model coordinate space.*/
    var dirtyRegion: Rectangle2D? = null
        private set

    /** ---- [ViewPainter] interface */

    override fun repaintView() {
        repaintDirtyRegion()
    }

    override fun paintView(context: DrawContext) {
        view.draw(context)
    }

    override fun invalidateRegion(region: RectangularShape?, ghost: Boolean) {
        if (region == null) {
            dirtyRegion = Rectangle2D(0.0, 0.0, view.width.toDouble(), view.height.toDouble())
        } else {
            dirtyRegion = dirtyRegion?.add(region) as Rectangle2D? ?: Rectangle2D(region)
        }
    }

    /** ---- [InvalidatableViewPainter] */

    private fun repaintDirtyRegion() {
        if (dirtyRegion != null) {
            dirtyRegion?.let { value ->
                val p1 = view.modelToView(Point2D(value.minX, value.minY))
                val p2 = view.modelToView(Point2D(value.maxX, value.maxY))
                val x1 = Math.floor(p1.x).toInt()
                val y1 = Math.floor(p1.y).toInt()
                val x2 = Math.ceil(p2.x).toInt()
                val y2 = Math.ceil(p2.y).toInt()
                view.repaint(x1 - 1, y1 - 1, x2 - x1 + 2, y2 - y1 + 2)
                dirtyRegion = null
            }
        }
    }
}