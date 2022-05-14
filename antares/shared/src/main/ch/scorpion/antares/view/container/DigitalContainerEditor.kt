package ch.scorpion.antares.view.container

import ch.scorpion.antares.model.inout.CircuitInOutBitWidthChanged
import ch.scorpion.antares.model.inout.CircuitInOutSignalRepresentationChanged
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

	private val circuitInOutBitWidthHandler: EventHandler<CircuitInOutBitWidthChanged> = {
		val portViewComponent = getContainerDrawing().getPortViewComponent(it.circuitInOut.name!!)
		if (portViewComponent != null) {
			(portViewComponent.port as DigitalPort).bitWidth = it.newValue
		}
	}

	private val circuitInOutSignalRepresentationHandler: EventHandler<CircuitInOutSignalRepresentationChanged> = {
		val portViewComponent = getContainerDrawing().getPortViewComponent(it.circuitInOut.name!!)
		if (portViewComponent != null) {
			(portViewComponent.port as DigitalPort).signalRepresentation = it.newValue
		}
	}

    init {
        eventBus.register(CircuitInOutBitWidthChanged::class, circuitInOutBitWidthHandler)
	    eventBus.register(CircuitInOutSignalRepresentationChanged::class, circuitInOutSignalRepresentationHandler)
    }

	override fun dispose() {
		eventBus.unregister(circuitInOutBitWidthHandler)
		eventBus.unregister(circuitInOutSignalRepresentationHandler)
	}
}