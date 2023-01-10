package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewPointSequence

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
			offset = edgeView.animationOffset
		)
		while (sequence.hasNext()) {
			val p = sequence.getNext(DISTANCE)
			context.g.fillRect(p.x - HALF_SIZE, p.y - HALF_SIZE, SIZE, SIZE)
		}
	}
}