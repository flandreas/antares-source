package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistory
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryDrawer
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryTimeline

class AnalogSignalHistoryDrawer : AbstractRectangle(Rectangle2D()), SignalHistoryDrawer {

	/** ---- [RectangularDrawable] interface*/

	override val lineWidth: Double get() = 0.0

	override fun draw(context: DrawContext) {
		// TODO
	}

	/** ---- [SignalHistoryDrawer] */

	override fun bind(
		signalHistory: SignalHistory<Any>?,
		gridSignalHistory: SignalHistory<Any>?,
		timeline: SignalHistoryTimeline?,
		color: CompositeColor
	) {
		// TODO
	}

}