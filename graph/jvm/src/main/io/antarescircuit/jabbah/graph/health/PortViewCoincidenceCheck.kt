package io.antarescircuit.jabbah.graph.health

import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.app.SystemMalfunctionEvent
import io.antarescircuit.jabbah.app.health.SystemHealthCheck
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.view.port.PortView

object PortViewCoincidenceCheck : SystemHealthCheck {

	override fun execute(data: ApplicationData): SystemMalfunctionEvent? {
		if (data.content !is MetaGraph) {
			return null
		}
		val metaGraph = data.content as MetaGraph
		val graphView = metaGraph.graph.graphView

		val unconnectedPortViews = mutableSetOf<PortView<*>>()

		// Reset all PortViews and connect unconnected ones
		graphView.getVerticeViews().forEach { vv ->
			vv.getPortViews().forEach { pv ->
				pv.coincidenceWarning = false
				if (!pv.port.isConnected) {
					unconnectedPortViews.add(pv)
				}
			}
		}

		unconnectedPortViews.forEach { pv ->
			pv.coincidenceWarning = unconnectedPortViews.any { it !== pv && it.owner !== pv.owner && pv.coincidesWith(it) }
		}

		return null
	}
}