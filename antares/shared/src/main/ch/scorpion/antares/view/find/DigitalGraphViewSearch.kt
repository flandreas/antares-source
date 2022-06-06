package ch.scorpion.antares.view.find

import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.antares.view.net.TunnelView
import ch.scorpion.jabbah.draw.view.find.SearchRequest
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.find.DrawingViewSearch

class DigitalGraphViewSearch : DrawingViewSearch() {

	override fun findImpl(drawing: Drawing<Component>, request: SearchRequest, result: MutableSet<Component>) {
		super.findImpl(drawing, request, result)

		findInOuts(drawing, request, result)
		findTunnels(drawing, request, result)
	}

	private fun findInOuts(drawing: Drawing<Component>, request: SearchRequest, result: MutableSet<Component>) {
		result.addAll(
			drawing.getDrawables { it is CircuitInOutView && compare(it.name, request) }
		)
	}

	private fun findTunnels(drawing: Drawing<Component>, request: SearchRequest, result: MutableSet<Component>) {
		result.addAll(
			drawing.getDrawables { it is TunnelView && compare(it.name, request) }
		)
	}
}