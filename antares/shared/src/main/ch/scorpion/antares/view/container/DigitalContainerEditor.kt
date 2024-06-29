package ch.scorpion.antares.view.container

import ch.scorpion.antares.model.inout.DigitalCircuitInOutBitWidthChanged
import ch.scorpion.antares.model.inout.DigitalCircuitInOutSignalRepresentationChanged
import ch.scorpion.antares.model.inout.DigitalCircuitInOutStartValueChanged
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.container.ContainerEditor

class DigitalContainerEditor(
    view: DrawingView<Drawing<Component>>,
    eventBus: EventBus = BaseModule.eventBus
) : ContainerEditor(view, eventBus) {

	private val circuitInOutBitWidthHandler: EventHandler<DigitalCircuitInOutBitWidthChanged> = {
		val portViewComponent = getContainerDrawing().getPortViewComponent(it.circuitInOut.name!!)
		if (portViewComponent != null) {
			(portViewComponent.port as DigitalPort).bitWidth = it.newValue
		}
	}

	private val circuitInOutSignalRepresentationHandler: EventHandler<DigitalCircuitInOutSignalRepresentationChanged> = {
		val portViewComponent = getContainerDrawing().getPortViewComponent(it.circuitInOut.name!!)
		if (portViewComponent != null) {
			(portViewComponent.port as DigitalPort).signalRepresentation = it.newValue
		}
	}

	private val circuitInOutStartValueHandler: EventHandler<DigitalCircuitInOutStartValueChanged> = {
		val portViewComponent = getContainerDrawing().getPortViewComponent(it.circuitInOut.name!!)
		if (portViewComponent != null) {
			if (portViewComponent.port.portType.isInput) {
				(portViewComponent.port as DigitalPort).unconnectedStartValue = it.newValue
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