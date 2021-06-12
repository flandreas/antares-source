package ch.scorpion.antares.view.app

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.gate.AbstractDigitalGate
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.DigitalGraphView
import ch.scorpion.antares.view.gate.AbstractDigitalGateView
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.antares.view.output.LightEmitter
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.CopyPasteService
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.app.GraphViewAppServiceImpl
import ch.scorpion.jabbah.graph.view.connect.GraphViewConnectService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortView

class DigitalGraphViewService(
	copyPasteService: CopyPasteService = EditModule.copyPasteService,
	commandManager: CommandManager = EditModule.commandManager,
	connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService,
	private val properties: Properties = BaseModule.properties
) : GraphViewAppServiceImpl(copyPasteService, commandManager, connectService) {

	companion object {
		private val LOG by logger(DigitalGraphViewService::class)
	}

	override fun customizeAddedComponent(component: Component) {
		if (component is LightEmitter) {
			component.lightColor = determineLightColor(component.parent as DigitalGraphView)
		}
	}

	fun replaceLightColor(graphView: DigitalGraphView) {
		graphView.defaultLightColor?.let { defaultLightColor ->
			LOG.info("Replace LightColor")
			graphView
				.getDrawables { it is LightEmitter }
				.map { it as LightEmitter }
				.forEach { it.lightColor = defaultLightColor }
		}
	}

	private fun determineLightColor(graphView: DigitalGraphView): LightColor =
		graphView.defaultLightColor ?: LightColor.getSystemDefault(properties)

	/**
	 * Changes the [InputCount] of an [AbstractDigitalGateView] (undoable), which might involve
	 * unconnecting [PortView]s that are being removed.
	 */
	fun changeInputCount(
		gateView: AbstractDigitalGateView<AbstractDigitalGate>,
		newInputCount: InputCount,
		drawingView: DrawingView<GraphView>
	) {
		checkArgument(newInputCount.count >= gateView.model.minInputCount.count, "InputCount must not be smaller than minimum ${gateView.model.minInputCount.count}")
		checkArgument(newInputCount.count <= gateView.model.maxInputCount.count, "InputCount must not be larger than maximum ${gateView.model.maxInputCount.count}")

		if (newInputCount.count > gateView.chosenInputCount.count) {
			increaseInputCount(gateView, newInputCount)
		} else if (newInputCount.count < gateView.chosenInputCount.count) {
			decreaseInputCount(gateView, newInputCount, drawingView)
		}
	}

	private fun increaseInputCount(gateView: AbstractDigitalGateView<AbstractDigitalGate>, newInputCount: InputCount) {
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

			gateView.updateLayout()
		}
	}

	private fun decreaseInputCount(gateView: AbstractDigitalGateView<AbstractDigitalGate>, newInputCount: InputCount, drawingView: DrawingView<GraphView>) {
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

			gateView.updateLayout()
		}
	}

	private fun unconnectDeletedPortView(portView: PortView<*>, drawingView: DrawingView<GraphView>) {
		drawingView.drawing.getEdgeViews()
			.filter { ev -> ev.origin?.portView === portView }
			.forEach { ev -> connectService.unconnectEdgeViewOrigin(ev) }
		drawingView.drawing.getEdgeViews()
			.filter { ev -> ev.destination?.portView === portView }
			.forEach { ev -> connectService.unconnectEdgeViewDestination(ev) }
	}
}