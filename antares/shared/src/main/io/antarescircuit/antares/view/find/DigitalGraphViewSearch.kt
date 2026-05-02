package io.antarescircuit.antares.view.find

import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.input.DipSwitchView
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.antares.view.net.tunnel.TunnelView
import io.antarescircuit.jabbah.draw.view.find.SearchRequest
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.find.DrawingViewSearch
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.VerticeView

class DigitalGraphViewSearch : DrawingViewSearch() {

	override fun findImpl(drawing: Drawing<*>, request: SearchRequest, result: MutableSet<Component>) {
		super.findImpl(drawing, request, result)
		expandEdgeViews(drawing, result)
	}

	private fun expandEdgeViews(drawing: Drawing<*>, result: MutableSet<Component>) {
		val expansion = result
            .asSequence()
            .filter(::filterExpandable)
			.map { it as VerticeView<*> }
			.mapNotNull { (drawing as GraphView).getEdgeView(it.model.getPort<DigitalSignal>()) }
			.flatMap { it.netView!!.getElements() }
			.toSet()
		result.addAll(expansion)
	}

	private fun filterExpandable(component: Component): Boolean =
		component is TunnelView
		|| component is DigitalCircuitInOutView
		|| component is SwitchView
		|| component is DipSwitchView
}