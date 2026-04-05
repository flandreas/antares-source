package io.antarescircuit.jabbah.graph.health

import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.app.SystemMalfunctionEvent
import io.antarescircuit.jabbah.app.health.SystemHealthCheck
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.model.Net
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.EdgeView

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