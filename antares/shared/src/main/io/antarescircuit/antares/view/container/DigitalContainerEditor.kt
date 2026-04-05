package io.antarescircuit.antares.view.container

import io.antarescircuit.antares.model.inout.DigitalCircuitInOut
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutBitWidthChanged
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutSignalRepresentationChanged
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutStartValueChanged
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.graph.container.ContainerEditor
import io.antarescircuit.jabbah.graph.view.GraphView

class DigitalContainerEditor(
	view: DrawingView<Drawing<Component>>,
	mainDrawingView: DrawingView<Drawing<Component>>,
	eventBus: EventBus = BaseModule.eventBus
) : ContainerEditor(view, mainDrawingView, eventBus) {

	private val circuitInOutBitWidthHandler: EventHandler<DigitalCircuitInOutBitWidthChanged> = { event ->
		getDigitalPort(event.circuitInOut)?.let {
			it.bitWidth = event.newValue
		}
	}

	private val circuitInOutSignalRepresentationHandler: EventHandler<DigitalCircuitInOutSignalRepresentationChanged> = { event ->
		getDigitalPort(event.circuitInOut)?.let {
			it.signalRepresentation = event.newValue
		}
	}

	private val circuitInOutStartValueHandler: EventHandler<DigitalCircuitInOutStartValueChanged> = { event ->
		getDigitalPort(event.circuitInOut)?.let {
			if (it.portType.isInput) {
				it.unconnectedStartValue = event.newValue
			}
		}
	}

    init {
        eventBus.register(DigitalCircuitInOutBitWidthChanged::class, circuitInOutBitWidthHandler)
	    eventBus.register(DigitalCircuitInOutSignalRepresentationChanged::class, circuitInOutSignalRepresentationHandler)
		eventBus.register(DigitalCircuitInOutStartValueChanged::class, circuitInOutStartValueHandler)
    }

	override fun dispose() {
		super.dispose()
		eventBus.unregister(circuitInOutBitWidthHandler)
		eventBus.unregister(circuitInOutSignalRepresentationHandler)
		eventBus.unregister(circuitInOutStartValueHandler)
	}

	private fun getDigitalPort(circuitInOut: DigitalCircuitInOut): DigitalPort? =
		if ((super.mainDrawingView.drawing as GraphView).graph?.contains(circuitInOut) == true) {
			val portViewComponent = getContainerDrawing().getPortViewComponent(circuitInOut.name!!)
			if (portViewComponent != null && portViewComponent.port is DigitalPort) {
				(portViewComponent.port as DigitalPort)
			} else {
				null
			}
		} else {
			null
		}
}