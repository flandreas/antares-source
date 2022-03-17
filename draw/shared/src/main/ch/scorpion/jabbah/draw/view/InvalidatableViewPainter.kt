package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.ViewPainter
import kotlin.math.ceil
import kotlin.math.floor

/**
 * [InvalidatableViewPainter] keeps track of all invalidated areas and repaints only those.
 */
class InvalidatableViewPainter(val view: View<*>) : ViewPainter {

	companion object {
		/** The number of repaints per second.*/
		private const val REPAINT_FREQUENCY = 40
	}

	/** Keeps track of the current accumulated invalid region in model coordinate space.*/
	var dirtyRegion: Rectangle2D? = null
		private set

	/** If set, indicates that the entire [View] is dirty, in which case [dirtyRegion] is overwritten.*/
	private var dirtyView: Boolean = false

	private val timer: Timer = System.createTimer()

	init {
		timer.initialize(1000 / REPAINT_FREQUENCY, repeats = false) {
			timer.stop()
			System.invokeLater {
				repaintDirtyRegion()
			}
		}
	}

	/** ---- [ViewPainter] interface */

	override fun repaintView() {
		startTimerIfNeeded()
	}

	override fun paintView(context: DrawContext) {
		view.draw(context)
	}

	override fun invalidateRegion(region: RectangularShape?) {
		if (region == null) {
			dirtyView = true
			RepaintingObserver.invalidated(Rectangle2D(0, 0, view.width, view.height))
		} else {
			if (!dirtyView) {
				dirtyRegion = dirtyRegion?.add(region) as Rectangle2D? ?: Rectangle2D(region)
			}
			RepaintingObserver.invalidated(region)
		}
	}

	/** ---- [InvalidatableViewPainter] */

	private fun repaintDirtyRegion() {
		if (dirtyView) {
			view.repaint(0, 0, view.width, view.height)
		} else {
			val p1 = if (dirtyRegion != null) view.modelToView(Point2D(dirtyRegion!!.minX, dirtyRegion!!.minY)) else Point2D(0, 0)
			val p2 = if (dirtyRegion != null) view.modelToView(Point2D(dirtyRegion!!.maxX, dirtyRegion!!.maxY)) else Point2D(view.width, view.height)
			val x1 = floor(p1.x).toInt()
			val y1 = floor(p1.y).toInt()
			val x2 = ceil(p2.x).toInt()
			val y2 = ceil(p2.y).toInt()
			view.repaint(x1 - 1, y1 - 1, x2 - x1 + 2, y2 - y1 + 2)
		}
		dirtyRegion = null
		dirtyView = false
	}

	private fun startTimerIfNeeded() {
		if (!timer.isRunning()) {
			timer.start()
		}
	}
}