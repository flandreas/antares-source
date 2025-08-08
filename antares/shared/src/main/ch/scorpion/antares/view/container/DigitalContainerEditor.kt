package ch.scorpion.antares.view.container

import ch.scorpion.antares.model.inout.DigitalCircuitInOut
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
import ch.scorpion.jabbah.graph.view.GraphView

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