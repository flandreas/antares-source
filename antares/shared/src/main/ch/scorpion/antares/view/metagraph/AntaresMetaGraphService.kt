package ch.scorpion.antares.view.metagraph

import ch.scorpion.antares.model.inout.DigitalCircuitInOutImpl
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.getDrawableInstances
import ch.scorpion.jabbah.edit.model.CopyPasteService
import ch.scorpion.jabbah.edit.model.DrawingService
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.Connection
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.connect.GraphViewConnectService
import ch.scorpion.jabbah.graph.view.metagraph.MetaGraphService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

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

		val edgeView = metaGraph.graph.graphView.getEdgeView(verticeView.model.getPort<DigitalSignal>()) as EdgeView<DigitalSignal>?
		if (edgeView != null) {
			val endpointType = edgeView.getConnectionEndpointType(verticeView)!!
			connectService.unconnectEdgeView(edgeView, endpointType)
			connectService.connect(edgeView, endpointType, Connection(inOutView, inOutView.model.getPort()))
		}

		metaGraph.graph.graphView.remove(verticeView)
	}
}