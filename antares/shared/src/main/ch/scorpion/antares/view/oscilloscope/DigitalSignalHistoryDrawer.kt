package ch.scorpion.antares.view.oscilloscope

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistory
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryDrawer
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryTimeline

class DigitalSignalHistoryDrawer : AbstractRectangle(Rectangle2D()), SignalHistoryDrawer {

    /** ---- [RectangularDrawable] interface*/

    override val lineWidth: Double get() = 0.0

    override fun draw(context: DrawContext) {
        // TODO
        context.g.color = Color.YELLOW
        context.g.fill(bounds)
        context.g.color = Color.BLACK
        context.g.draw(bounds)
    }

    /** ---- [SignalHistoryDrawer] interface */

    override fun bind(signalHistory: SignalHistory<Any>, timeline: SignalHistoryTimeline) {
        throw UnsupportedOperationException("not implemented")
    }
}