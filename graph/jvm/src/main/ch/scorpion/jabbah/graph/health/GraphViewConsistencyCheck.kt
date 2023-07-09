package ch.scorpion.jabbah.graph.health

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.SystemMalfunctionEvent
import ch.scorpion.jabbah.app.health.SystemHealthCheck
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.Net

class GraphViewConsistencyCheck : SystemHealthCheck {

	companion object {
		private val LOG by logger(GraphViewConsistencyCheck::class)
	}

	override fun execute(data: ApplicationData): SystemMalfunctionEvent? {
		if (data.content !is MetaGraph) {
			return null
		}
		val metaGraph = data.content as MetaGraph

		val brokenEdgeViews = metaGraph.graph.graphView.getEdgeViews().filter { it.hasBrokenPortRef }
		if (brokenEdgeViews.isNotEmpty()) {
			LOG.error("Found broken port ref in net during consistency check: EdgeView.id = ${brokenEdgeViews.first().id}")
			brokenEdgeViews.forEach { it.model.activateBrokenRefError() }
			return SystemMalfunctionEvent(Net.BROKEN_REF_DESIGN_ERROR.description)
		}

		return null
	}
}