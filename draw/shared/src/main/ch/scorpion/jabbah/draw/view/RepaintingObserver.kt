package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke

/**
 * A debugging utility class that observes invalidation and repainting action in the
 * [ViewManager]'s current [View].
 */
object RepaintingObserver {

    private val LOG by logger(RepaintingObserver::class)

    var isEnabled: Boolean = false
        set(value) {
	        check(!isRunning)
            if (field != value) {
                field = value
                LOG.trace("RepaintingObserver isEnabled=$field")
                if (!field && display != null) {
                    display?.let {
                        view!!.overlayContainer.remove(it)
                        view!!.overlayContainer.validate()
                    }
                }
                logEntries.clear()
                BaseModule.eventBus.post(RepaintingObserverEnabledEvent())
            }
        }

    var isRunning: Boolean = false
        set(value) {
	        check(isEnabled)
            if (field != value) {
                field = value
                LOG.trace("RepaintingObserver isRunning=$field")
                if (field) {
                    logEntries.clear()
                    display?.let {
                        view!!.overlayContainer.remove(it)
                        view!!.overlayContainer.validate()
                    }
                    display = null
                } else {
                    logIndex = 0
                    repaintIndex = 0
                    display = RepaintingObserverDisplay(view!!)
                    view!!.overlayContainer.add(display!!)
                    view!!.overlayContainer.validate()
                }
                BaseModule.eventBus.post(RepaintingObserverRunningEvent())
            }
        }

    var logIndex: Int = 0
        private set

    val logSize: Int get() = logEntries.size

    val currentLog: RepaintingLogEntry get() = logEntries[logIndex]

    private val view: View<*>? get() = DrawViewModule.viewManager.activeView?.view

    private var repaintIndex = 0

    private val logEntries = mutableListOf<RepaintingLogEntry>()

    private var display: RepaintingObserverDisplay? = null

    /**
     * Notifies this [RepaintingObserver] that a region in the current [View] has been invalidated.
     * @param rect the invalid region in model coordinates
     */
    fun invalidated(rect: RectangularShape) {
        if (view == null || !isRunning) {
            return
        }
        LOG.trace("RepaintingObserver.invalidated rect=$rect")
        logEntries.add(RepaintingLogEntry(modelToViewRect(rect), repaintIndex++))
    }

    /**
     * Notifies this [RepaintingObserver] that the current [View] has been repainted.
     * @param rect the repainted region in view coordinates
     */
    fun repainted(@Suppress("UNUSED_PARAMETER") rect: RectangularShape) {
        // TODO
    }

    fun previousLogEntry() {
        LOG.trace("RepaintingObserver.previousLogEntry")
	    check(logIndex > 0)
        logIndex -= 1
        BaseModule.eventBus.post(RepaintingObserverLogEvent())
    }

    fun nextLogEntry() {
        LOG.trace("RepaintingObserver.nextLogEntry")
	    check(logIndex < logSize - 1)
        logIndex += 1
        BaseModule.eventBus.post(RepaintingObserverLogEvent())
    }

    fun getLog(index: Int): RepaintingLogEntry {
        return logEntries[index]
    }

    private fun modelToViewRect(rect: RectangularShape): RectangularShape {
        val p1 = view!!.modelToView(Point2D(rect.minX, rect.minY))
        val p2 = view!!.modelToView(Point2D(rect.maxX, rect.maxY))
        return Rectangle2D(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y)
    }
}

class RepaintingObserverEnabledEvent
class RepaintingObserverRunningEvent
class RepaintingObserverLogEvent

data class RepaintingLogEntry(val invalidRect: RectangularShape, val repaintNumber: Int)

private class RepaintingObserverDisplay(view: View<*>) : AbstractRectangle(0, 0, view.width, view.height) {

    companion object {
        private val INVALIDATE_STROKE = Stroke(0.8f)
        private val INVALIDATE_COLOR = Color.RED
    }

    init {
        BaseModule.eventBus.register(RepaintingObserverLogEvent::class) {
	        invalidate()
	        validate()
        }
    }

    override fun draw(context: DrawContext) {
        context.g.color = INVALIDATE_COLOR
        context.g.stroke = INVALIDATE_STROKE
        context.g.draw(RepaintingObserver.currentLog.invalidRect)
    }

    override val lineWidth: Double get() = 1.0
}