package ch.scorpion.jabbah.graph.health

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.SystemMalfunctionEvent
import ch.scorpion.jabbah.app.health.SystemHealthCheck
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.EdgeView

/**
 * Checks if there are any broken [Port] references as of [EdgeView.hasBrokenPortRef].
 */
object GraphViewConsistencyCheck : SystemHealthCheck {

	private val LOG by logger(GraphViewConsistencyCheck::class)

	/** The name of the property in [RemoteControlService] to enable this check.*/
	const val REMOTE_PROP_CONSISTENCY_CHECK = "graph.consistencyCheck"

	override fun execute(data: ApplicationData): SystemMalfunctionEvent? {
		if (data.content !is MetaGraph) {
			return null
		}
		val metaGraph = data.content as MetaGraph
		return execute(metaGraph.graph.graphView)
	}

	fun execute(graphView: GraphView): SystemMalfunctionEvent? {
		val brokenEdgeViews = graphView.getEdgeViews().filter { it.hasBrokenPortRef }
		if (brokenEdgeViews.isNotEmpty()) {
			LOG.error("Found broken port ref in net during consistency check: EdgeView.id = ${brokenEdgeViews.first().id}")
			brokenEdgeViews.forEach { it.model.activateBrokenRefError() }
			return SystemMalfunctionEvent(Net.BROKEN_REF_DESIGN_ERROR.description)
		}

		return null
	}
}