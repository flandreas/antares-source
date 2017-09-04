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
        private val SIGNAL_HEIGHT = 10
        private val BACKGROUND_COLOR = Color.BLACK
        private val AXIS_COLOR = Color(64, 64, 64)
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
        context.g.color = color!!.foregroundColor

        var lastPoint = Point2D()
        var lastEntry: SignalHistoryEntry<DigitalSignal>? = null
        for (entry in signalHistory!!.getReverseEntriesUntil(0)) {
            if (lastEntry == null) {
                lastPoint = Point2D(bounds.maxX, signalY(entry))
            } else {
                val nextX = bounds.maxX - timeline!!.getX(entry.time)
                val nextY = signalY(entry)

                if (nextX < bounds.minX) {
                    context.g.drawLine(lastPoint.x, lastPoint.y, bounds.minX, lastPoint.y)
                    break
                }

                //LOG.debug("DigitalSignalHistoryDrawer: time=${entry.time}, nextX=$nextX, nextY=$nextY")

                context.g.drawLine(lastPoint.x, lastPoint.y, nextX, lastPoint.y)
                context.g.drawLine(nextX, lastPoint.y, nextX, nextY)

                lastPoint = Point2D(nextX, nextY)
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