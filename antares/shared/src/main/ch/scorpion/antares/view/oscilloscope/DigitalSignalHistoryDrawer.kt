package ch.scorpion.antares.view.oscilloscope

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistory
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistoryEntry
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryDrawer
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryTimeline

class DigitalSignalHistoryDrawer : AbstractRectangle(Rectangle2D()), SignalHistoryDrawer {

    companion object {
        private val LOG by logger(DigitalSignalHistoryDrawer::class)
        private val SIGNAL_HEIGHT = 10
    }

    private var signalHistory: SignalHistory<DigitalSignal>? = null
    private var timeline: SignalHistoryTimeline? = null

    /** ---- [RectangularDrawable] interface*/

    override val lineWidth: Double get() = 0.0

    override fun draw(context: DrawContext) {

        if (signalHistory == null || timeline == null) {
            return
        }

        var lastPoint = Point2D()
        var lastEntry: SignalHistoryEntry<DigitalSignal>? = null
        for (entry in signalHistory!!.getReverseEntriesUntil(0)) {
            if (lastEntry == null) {
                lastPoint = Point2D(bounds.maxX - timeline!!.getX(entry.time), signalY(entry))
            } else {
                val nextX = bounds.maxX - timeline!!.getX(entry.time)
                val nextY = signalY(entry)

                LOG.debug("DigitalSignalHistoryDrawer: time=${entry.time}, nextX=$nextX, nextY=$nextY")

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

    override fun bind(signalHistory: SignalHistory<Any>?, timeline: SignalHistoryTimeline?) {
        this.signalHistory = signalHistory as SignalHistory<DigitalSignal>?
        this.timeline = timeline
    }
}