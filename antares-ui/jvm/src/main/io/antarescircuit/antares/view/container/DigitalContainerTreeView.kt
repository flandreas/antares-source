package io.antarescircuit.antares.view.container

import io.antarescircuit.antares.model.inout.DigitalCircuitInOut
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutBitWidthChanged
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutSignalRepresentationChanged
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutStartValueChanged
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.graph.container.ContainerTreePortItem
import io.antarescircuit.jabbah.graph.container.ContainerTreeView
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.port.PortFactory
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.port.PortViewFactory

class DigitalContainerTreeView(
	mainDrawingView: DrawingView<GraphElementView<*>, GraphView>,
	portFactory: PortFactory = GraphModelModule.portFactory,
	portViewFactory: PortViewFactory = GraphViewModule.portViewFactory,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	eventBus: EventBus = BaseModule.eventBus
) : ContainerTreeView(mainDrawingView, portFactory, portViewFactory, styleProvider, eventBus) {

	private val bitWidthHandler: EventHandler<DigitalCircuitInOutBitWidthChanged> = { event ->
		getDigitalCircuitInOutView(event.circuitInOut)?.let {
			it.bitWidth = event.newValue
		}
	}

	private val signalRepresentationHandler: EventHandler<DigitalCircuitInOutSignalRepresentationChanged> = { event ->
		getDigitalCircuitInOutView(event.circuitInOut)?.let {
			it.signalRepresentation = event.newValue
		}
	}

	private val startValueHandler: EventHandler<DigitalCircuitInOutStartValueChanged> = { event ->
		getDigitalCircuitInOutView(event.circuitInOut)?.let {
			it.startValue = event.newValue?.getValue()?.toLong()
		}
	}

    init {
	    eventBus.register(DigitalCircuitInOutBitWidthChanged::class, bitWidthHandler)
	    eventBus.register(DigitalCircuitInOutSignalRepresentationChanged::class, signalRepresentationHandler)
		eventBus.register(DigitalCircuitInOutStartValueChanged::class, startValueHandler)
    }

	override fun dispose() {
		super.dispose()
		eventBus.unregister(bitWidthHandler)
		eventBus.unregister(signalRepresentationHandler)
		eventBus.unregister(startValueHandler)
	}

	private fun getDigitalCircuitInOutView(circuitInOut: DigitalCircuitInOut): DigitalCircuitInOutView? =
		if (super.mainDrawingView.drawing.graph?.contains(circuitInOut) == true) {
			val treeNode = containerTree?.model?.getPortTreeNode(circuitInOut.name!!)
			if (treeNode != null
				&& treeNode.userObject is ContainerTreePortItem
				&& (treeNode.userObject as ContainerTreePortItem).graphPortView is DigitalCircuitInOutView
			) {
				(treeNode.userObject as ContainerTreePortItem).graphPortView as DigitalCircuitInOutView
			} else {
				null
			}
		} else {
			null
		}
}