package io.antarescircuit.antares.view.analog

import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewPointSequence

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
			returnSequenceEndPoint = false,
			offset = edgeView.animationOffset
		)

		sequence.forEach(DISTANCE) { x, y ->
			context.g.fillRect(x - HALF_SIZE, y - HALF_SIZE, SIZE, SIZE)
		}
	}
}