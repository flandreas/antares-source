package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.base.SIGMA
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewPointSequence
import kotlin.math.abs

/** Visualizes the flow of electrical current along a [AnalogEdgeView].*/
object CurrentFlowVisualization {

	private const val HALF_SIZE = 1.5
	private const val SIZE = 2 * HALF_SIZE
	const val DISTANCE = 10.0

	fun draw(edgeView: AnalogEdgeView, context: DrawContext) {
		context.g.color = edgeView.model.signal!!.color.foregroundColor

		val sequence = EdgeViewPointSequence(
			edgeView,
			isReverse = edgeView.current < 0,
			offset = calculateOffset(edgeView)
		)
		while (sequence.hasNext()) {
			val p = sequence.getNext(DISTANCE)
			context.g.fillRect(p.x - HALF_SIZE, p.y - HALF_SIZE, SIZE, SIZE)
		}
	}

	private fun calculateOffset(edgeView: AnalogEdgeView): Double {
		val factor = if (abs(edgeView.current) <= SIGMA) 0 else 1
		return edgeView.currentAnimationOffset * factor
	}
}