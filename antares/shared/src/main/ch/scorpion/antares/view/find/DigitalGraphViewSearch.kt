package ch.scorpion.antares.view.find

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.antares.view.input.DipSwitchView
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.antares.view.net.TunnelView
import ch.scorpion.jabbah.draw.view.find.SearchRequest
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.find.DrawingViewSearch
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView

class DigitalGraphViewSearch : DrawingViewSearch() {

	override fun findImpl(drawing: Drawing<Component>, request: SearchRequest, result: MutableSet<Component>) {
		super.findImpl(drawing, request, result)
		expandEdgeViews(drawing, result)
	}

	private fun expandEdgeViews(drawing: Drawing<Component>, result: MutableSet<Component>) {
		val expansion = result
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