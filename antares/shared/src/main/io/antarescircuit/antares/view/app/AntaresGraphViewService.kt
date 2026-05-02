package io.antarescircuit.antares.view.app

import io.antarescircuit.antares.model.PortCount
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalRepresentation
import io.antarescircuit.antares.model.signal.DigitalSignalRepresenter
import io.antarescircuit.antares.view.DigitalGraphView
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.gate.TriStateBufferGateView
import io.antarescircuit.antares.view.net.WireTapView
import io.antarescircuit.antares.view.output.LightColor
import io.antarescircuit.antares.view.output.LightEmitter
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.model.CopyPasteService
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.param.GraphParamDefinition
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.app.GraphViewAppServiceImpl
import io.antarescircuit.jabbah.graph.view.connect.GraphViewConnectService
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.port.PortView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView

class AntaresGraphViewService(
	copyPasteService: CopyPasteService = EditModule.copyPasteService,
	commandManager: CommandManager = EditModule.commandManager,
	connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService,
	private val properties: Properties = BaseModule.properties
) : GraphViewAppServiceImpl(copyPasteService, commandManager, connectService) {

	companion object {
		private val LOG by logger(AntaresGraphViewService::class)
	}

	override fun customizeAddedComponent(component: Component, drawing: Drawing<*>) {
		super.customizeAddedComponent(component, drawing)
		if (drawing is DigitalGraphView) {
			val lightColor = determineLightColor(drawing)
			val signalRepresentation = determineSignalRepresentation(drawing)
			if (component is LightEmitter) {
				component.lightColor = lightColor
			}
			if (component is DigitalSignalRepresenter) {
				component.signalRepresentation = signalRepresentation
			} else if (component is GraphElementView<*> && component.model is DigitalSignalRepresenter) {
				(component.model as DigitalSignalRepresenter).signalRepresentation = signalRepresentation
			}
			if (component is SubGraphVerticeView<*>) {
				@Suppress("UNCHECKED_CAST")
				applyDefaultLightColor(component as SubGraphVerticeView<SubGraphVerticeRef>, lightColor)
			}
			if (component is LogicGateView) {
				component.size = drawing.defaultLogicGateSize
			} else if (component is TriStateBufferGateView) {
				component.size = drawing.defaultLogicGateSize
			}
		}
	}

	fun replaceLightColor(graphView: DigitalGraphView) {
		graphView.defaultLightColor?.let { defaultLightColor ->
			LOG.info("Replace LightColor")
			graphView
				.getDrawables { it is LightEmitter || it is SubGraphVerticeView<*> }
				.forEach {
					if (it is LightEmitter) {
						it.lightColor = defaultLightColor
					} else if (it is SubGraphVerticeView<*>) {
						@Suppress("UNCHECKED_CAST")
						applyDefaultLightColor(it as SubGraphVerticeView<SubGraphVerticeRef>, defaultLightColor)
					}
				}
		}
	}

	private fun applyDefaultLightColor(subGraphVV: SubGraphVerticeView<SubGraphVerticeRef>, defaultLightColor: LightColor) {
		val paramDefs = subGraphVV.model.graphUUID?.let {
			LibraryModule.libraryHolder.getMetaGraph(it).graph.model?.parameterDefinitions
		}
		paramDefs?.definitions
			?.filter { it.type.valueClass == LightColor::class }
			?.map {
				@Suppress("UNCHECKED_CAST")
				it as GraphParamDefinition<LightColor>
			}
			?.forEach { paramDef ->
				subGraphVV.model.setParamValue(paramDef.createValue(defaultLightColor))
			}
	}

	private fun determineLightColor(graphView: DigitalGraphView): LightColor =
		graphView.defaultLightColor ?: LightColor.getSystemDefault(properties)

	private fun determineSignalRepresentation(graphView: DigitalGraphView): DigitalSignalRepresentation =
		graphView.defaultSignalRepresentation ?: DigitalSignalRepresentation.getSystemDefault(properties)

	/**
	 * Changes the [PortCount] of an [LogicGateView] (undoable), which might involve
	 * unconnecting [PortView]s that are being removed.
	 */
	fun changeInputCount(
		gateView: LogicGateView,
		newInputCount: PortCount,
		drawingView: DrawingView<GraphElementView<*>, GraphView>
	) {
		require(newInputCount.count >= gateView.model.minInputCount.count) { "InputCount must not be smaller than minimum ${gateView.model.minInputCount.count}" }
		require(newInputCount.count <= gateView.model.maxInputCount.count) { "InputCount must not be larger than maximum ${gateView.model.maxInputCount.count}" }

		if (newInputCount.count > gateView.chosenInputCount.count) {
			increaseInputCount(gateView, newInputCount)
		} else if (newInputCount.count < gateView.chosenInputCount.count) {
			decreaseInputCount(gateView, newInputCount, drawingView)
		}
	}

	private fun increaseInputCount(gateView: LogicGateView, newInputCount: PortCount) {
		gateView.model.apply {
			// Temporarily "hide" Port.id of OutputPort
			getOutput<DigitalSignal>().portId = -1

			// Add additional Ports
			for (i in chosenInputCount.count + 1 .. newInputCount.count) {
				val port = createInputPort()
				addPort(port)
				gateView.addPortView(gateView.createInputPortView(port))
			}

			// Re-establish Port.id of OutputPort
			getOutput<DigitalSignal>().portId = inputCount + 1

			gateView.updateInputBitWidthAnnotations()
			gateView.model.notifyStateChanged()
			gateView.updateLayout()
		}
	}

	private fun decreaseInputCount(gateView: LogicGateView, newInputCount: PortCount, drawingView: DrawingView<GraphElementView<*>, GraphView>) {
		gateView.model.apply {
			val ports = getInputs().sortedBy { it.portId }.toMutableList()
			for (i in chosenInputCount.count - 1 downTo newInputCount.count) {
				val port = ports.removeLast()
				val portView = gateView.getPortView(port)!!
				unconnectDeletedPortView(portView, drawingView)
				gateView.removePortView(portView)
				removePort(port)
			}

			// Re-establish Port.id of OutputPort
			getOutput<DigitalSignal>().portId = inputCount + 1

			gateView.updateInputBitWidthAnnotations()
			gateView.model.notifyStateChanged()
			gateView.updateLayout()
		}
	}

	fun changeOutputCount(
		wireTapView: WireTapView,
		newOutputCount: PortCount,
		drawingView: DrawingView<GraphElementView<*>, GraphView>
	) {
		if (newOutputCount.count > wireTapView.tapCount.count) {
			increaseOutputCount(wireTapView, newOutputCount)
		} else if (newOutputCount.count < wireTapView.tapCount.count) {
			decreaseOutputCount(wireTapView, newOutputCount, drawingView)
		}
	}

	private fun increaseOutputCount(wireTapView: WireTapView, newOutputCount: PortCount) {
		wireTapView.model.apply {
			addNarrowPorts(newOutputCount.count - tapCount.count)
			.forEach {
				wireTapView.addPortView(wireTapView.createOutputPortView(it))
			}
			notifyStateChanged()
		}
		wireTapView.updateGeometry()
	}

	private fun decreaseOutputCount(wireTapView: WireTapView, newOutputCount: PortCount, drawingView: DrawingView<GraphElementView<*>, GraphView>) {
		wireTapView.model.apply {
			val ports = getPorts().filter { it.portId > 1 }.sortedBy { it.portId }.toMutableList()
			for (i in ports.size - 1 downTo newOutputCount.count) {
				val port = ports.removeLast()
				val portView = wireTapView.getPortView(port)!!
				unconnectDeletedPortView(portView, drawingView)
				wireTapView.removePortView(portView)
				removePort(port)
			}

			wireTapView.model.notifyStateChanged()
			wireTapView.updateGeometry()
		}
	}

	private fun unconnectDeletedPortView(portView: PortView<*>, drawingView: DrawingView<GraphElementView<*>, GraphView>) {
		drawingView.drawing.getEdgeViews()
			.filter { ev -> ev.origin?.portView === portView }
			.forEach { ev -> connectService.unconnectEdgeViewOrigin(ev) }
		drawingView.drawing.getEdgeViews()
			.filter { ev -> ev.destination?.portView === portView }
			.forEach { ev -> connectService.unconnectEdgeViewDestination(ev) }
	}
}