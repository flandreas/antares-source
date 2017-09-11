package ch.scorpion.antares.view.oscilloscope

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistory
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistoryEntry
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryDrawer
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryTimeline

class DigitalSignalHistoryDrawer : AbstractRectangle(Rectangle2D()), SignalHistoryDrawer {

    companion object {
        private val LOG by logger(DigitalSignalHistoryDrawer::class)
        private val SIGNAL_HEIGHT = 15.0
        private val BACKGROUND_COLOR = Color.BLACK
        private val AXIS_COLOR = Color(64, 64, 64)
        private val START_SIZE = 4.0
        private val FILL_SIGNAL = false
    }

    private var signalHistory: SignalHistory<DigitalSignal>? = null
    private var timeline: SignalHistoryTimeline? = null
    private var color: CompositeColor? = null

    /** ---- [RectangularDrawable] interface*/

    override val lineWidth: Double get() = 0.0

    override fun draw(context: DrawContext) {

        // Draw background
        context.g.color = BACKGROUND_COLOR
        context.g.fill(bounds)
        context.g.draw(bounds)

        // Draw axis
        context.g.color = AXIS_COLOR
        context.g.drawLine(bounds.maxX, bounds.height / 2, bounds.minX, bounds.height / 2)

        if (signalHistory == null || timeline == null || color == null) {
            return
        }

        // Draw curve
        var lastPoint = Point2D()
        var lastEntry: SignalHistoryEntry<DigitalSignal>? = null
        for (entry in signalHistory!!.getReverseEntriesUntil(0)) {
            val x = bounds.maxX - timeline!!.getX(entry.time)
            val y = signalY(entry)
            if (lastEntry == null) {
                // Right border
                lastPoint = Point2D(x, y)
                val effNextX = Math.max(x, bounds.minX)
                if (FILL_SIGNAL) {
                    context.g.color = color!!.backgroundColor
                    context.g.fillRect(effNextX, y, bounds.maxX - effNextX, bounds.height / 2 - y)
                }
                context.g.color = color!!.foregroundColor
                context.g.drawLine(bounds.maxX, y, effNextX, y)
                context.g.fillOval(bounds.maxX - START_SIZE, y - START_SIZE, 2 * START_SIZE, 2 * START_SIZE)

                if (x <= bounds.minX) {
                    break
                }
            } else {
                val nextX = x
                val nextY = y
                val effNextX = Math.max(nextX, bounds.minX)

                if (FILL_SIGNAL) {
                    context.g.color = color!!.backgroundColor
                    context.g.fillRect(effNextX, nextY, lastPoint.x - effNextX, bounds.height / 2 - nextY)
                }
                context.g.color = color!!.foregroundColor
                context.g.drawLine(lastPoint.x, lastPoint.y, lastPoint.x, nextY)
                context.g.drawLine(lastPoint.x, nextY, effNextX, nextY)

                if (nextX <= bounds.minX) {
                    break
                }

                lastPoint = Point2D(effNextX, nextY)
            }

            lastEntry = entry
        }
    }

    fun signalY(entry: SignalHistoryEntry<DigitalSignal>): Double {
        return if (entry.signal.bitAt(0).isSet) {
            bounds.height / 2 - SIGNAL_HEIGHT
        } else {
            bounds.height / 2
        }
    }

    /** ---- [SignalHistoryDrawer] interface */

    override fun bind(signalHistory: SignalHistory<Any>?, timeline: SignalHistoryTimeline?, color: CompositeColor) {
        this.signalHistory = signalHistory as SignalHistory<DigitalSignal>?
        this.timeline = timeline
        this.color = color
    }
}