package ch.scorpion.antares.view.container

import ch.scorpion.antares.model.inout.DigitalCircuitInOutBitWidthChanged
import ch.scorpion.antares.model.inout.DigitalCircuitInOutSignalRepresentationChanged
import ch.scorpion.antares.model.inout.DigitalCircuitInOutStartValueChanged
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.container.ContainerEditor
import ch.scorpion.jabbah.graph.view.GraphView

class DigitalContainerEditor(
	view: DrawingView<Drawing<Component>>,
	mainDrawingView: DrawingView<Drawing<Component>>,
	eventBus: EventBus = BaseModule.eventBus
) : ContainerEditor(view, mainDrawingView, eventBus) {

	companion object {
		private val LOG by logger(DigitalContainerEditor::class)
	}

	private val circuitInOutBitWidthHandler: EventHandler<DigitalCircuitInOutBitWidthChanged> = { event ->
		if ((super.mainDrawingView.drawing as GraphView).graph?.contains(event.circuitInOut) == true) {
			val portViewComponent = getContainerDrawing().getPortViewComponent(event.circuitInOut.name!!)
			if (portViewComponent != null) {
				(portViewComponent.port as DigitalPort).bitWidth = event.newValue
			}
		}
	}

	private val circuitInOutSignalRepresentationHandler: EventHandler<DigitalCircuitInOutSignalRepresentationChanged> = { event ->
		if ((super.mainDrawingView.drawing as GraphView).graph?.contains(event.circuitInOut) == true) {
			val portViewComponent = getContainerDrawing().getPortViewComponent(event.circuitInOut.name!!)
			if (portViewComponent != null) {
				(portViewComponent.port as DigitalPort).signalRepresentation = event.newValue
			}
		}
	}

	private val circuitInOutStartValueHandler: EventHandler<DigitalCircuitInOutStartValueChanged> = { event ->
		if ((super.mainDrawingView.drawing as GraphView).graph?.contains(event.circuitInOut) == true) {
			val portViewComponent = getContainerDrawing().getPortViewComponent(event.circuitInOut.name!!)
			if (portViewComponent != null) {
				if (portViewComponent.port.portType.isInput) {
					(portViewComponent.port as DigitalPort).unconnectedStartValue = event.newValue
				}
			}
		}
	}

    init {
        eventBus.register(DigitalCircuitInOutBitWidthChanged::class, circuitInOutBitWidthHandler)
	    eventBus.register(DigitalCircuitInOutSignalRepresentationChanged::class, circuitInOutSignalRepresentationHandler)
		eventBus.register(DigitalCircuitInOutStartValueChanged::class, circuitInOutStartValueHandler)
    }

	override fun dispose() {
		eventBus.unregister(circuitInOutBitWidthHandler)
		eventBus.unregister(circuitInOutSignalRepresentationHandler)
		eventBus.unregister(circuitInOutStartValueHandler)
	}
}