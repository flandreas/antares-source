package ch.scorpion.jabbah.graph.health

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.SystemMalfunctionEvent
import ch.scorpion.jabbah.app.health.SystemHealthCheck
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.view.port.PortView

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
			pv.coincidenceWarning = unconnectedPortViews.any { it !== pv && pv.coincidesWith(it) }
		}

		return null
	}
}