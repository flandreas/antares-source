package io.antarescircuit.antares.view.metagraph

import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.draw.getDrawableInstances
import io.antarescircuit.jabbah.edit.model.CopyPasteService
import io.antarescircuit.jabbah.edit.model.DrawingService
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.view.Connection
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.connect.GraphViewConnectService
import io.antarescircuit.jabbah.graph.view.metagraph.MetaGraphService
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule

class AntaresMetaGraphService(
	drawingService: DrawingService = EditModule.drawingService,
	copyPasteService: CopyPasteService = EditModule.copyPasteService,
	private val connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService
) : MetaGraphService(drawingService, copyPasteService) {

	override fun tailorMetaGraph(metaGraph: MetaGraph) {
		replaceSwitches(metaGraph)
		replaceLEDViews(metaGraph)
	}

	private fun replaceLEDViews(metaGraph: MetaGraph) {
		metaGraph.graph.graphView
			.getDrawableInstances<LEDView>()
			.forEach {
				replaceVerticeView(metaGraph, it, it.name, it.orientation, PortType.OUTPUT)
			}
	}

	private fun replaceSwitches(metaGraph: MetaGraph) {
		metaGraph.graph.graphView
			.getDrawableInstances<SwitchView>()
			.forEach {
				replaceVerticeView(metaGraph, it, it.name, it.orientation, PortType.INPUT)
			}
	}

	private fun replaceVerticeView(
		metaGraph: MetaGraph,
		verticeView: VerticeView<*>,
		name: String?,
		orientation: Direction,
		portType: PortType
	) {
		val inOutView = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = name, portType = portType), orientation = orientation)
		inOutView.location = verticeView.location
		metaGraph.graph.graphView.add(inOutView)

		@Suppress("UNCHECKED_CAST")
		val edgeView = metaGraph.graph.graphView.getEdgeView(verticeView.model.getPort<DigitalSignal>()) as EdgeView<DigitalSignal>?

		if (edgeView != null) {
			val endpointType = edgeView.getConnectionEndpointType(verticeView)!!
			connectService.unconnectEdgeView(edgeView, endpointType)
			connectService.connect(edgeView, endpointType, Connection(inOutView, inOutView.model.getPort()))
		}

		metaGraph.graph.graphView.remove(verticeView)
	}
}