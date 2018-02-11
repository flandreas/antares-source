package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.ViewPainter
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.logger

/**
 * [InvalidatableViewPainter] keeps track of all invalidated areas and repaints only those.
 */
class InvalidatableViewPainter(val view: View<*>) : ViewPainter {

    companion object {
        private val LOG by logger(InvalidatableViewPainter::class)

        /** The number of repaints per second.*/
        private val REPAINT_FREQUENCY = 40
    }

    /** Keeps track of the current accumulated invalid region in model coordinate space.*/
    var dirtyRegion: Rectangle2D? = null
        private set

    private val timer: Timer = System.get().createTimer()

    init {
        timer.initialize(1000 / REPAINT_FREQUENCY, {
            timer.stop()
            System.SYSTEM!!.invokeLater {
                repaintDirtyRegion()
            }
        })
    }

    /** ---- [ViewPainter] interface */

    override fun repaintView() {
        startTimerIfNeeded()
    }

    override fun paintView(context: DrawContext) {
        view.draw(context)
    }

    override fun invalidateRegion(region: RectangularShape?, ghost: Boolean) {
        if (region == null) {
            dirtyRegion = null
            RepaintingObserver.invalidated(Rectangle2D(0, 0, view.width, view.height))
        } else {
            dirtyRegion = dirtyRegion?.add(region) as Rectangle2D? ?: Rectangle2D(region)
            RepaintingObserver.invalidated(region)
        }
    }

    /** ---- [InvalidatableViewPainter] */

    private fun repaintDirtyRegion() {
            val p1 = if (dirtyRegion != null) view.modelToView(Point2D(dirtyRegion!!.minX, dirtyRegion!!.minY)) else Point2D(0, 0)
            val p2 = if (dirtyRegion != null) view.modelToView(Point2D(dirtyRegion!!.maxX, dirtyRegion!!.maxY)) else Point2D(view.width, view.height)
            val x1 = Math.floor(p1.x).toInt()
            val y1 = Math.floor(p1.y).toInt()
            val x2 = Math.ceil(p2.x).toInt()
            val y2 = Math.ceil(p2.y).toInt()
            view.repaint(x1 - 1, y1 - 1, x2 - x1 + 2, y2 - y1 + 2)
            dirtyRegion = null
    }

    private fun startTimerIfNeeded() {
        if (!timer.isRunning()) {
            timer.start()
        }
    }
}