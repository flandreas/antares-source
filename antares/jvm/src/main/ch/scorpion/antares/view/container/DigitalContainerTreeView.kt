package ch.scorpion.antares.view.container

import ch.scorpion.antares.model.inout.CircuitInOutBitWidthChanged
import ch.scorpion.antares.model.inout.CircuitInOutSignalRepresentationChanged
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.container.ContainerTreeView
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.model.port.PortFactory
import ch.scorpion.jabbah.graph.view.port.PortViewFactory

class DigitalContainerTreeView(
	portFactory: PortFactory = GraphModelModule.portFactory,
	portViewFactory: PortViewFactory = GraphViewModule.portViewFactory,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	eventBus: EventBus = BaseModule.eventBus
) : ContainerTreeView(portFactory, portViewFactory, styleProvider, eventBus) {

	private val bitWidthHandler: EventHandler<CircuitInOutBitWidthChanged> = {
		val treeNode = containerTree?.model?.getPortsTreeNode(it.circuitInOut.name!!)
		if (treeNode != null && treeNode.userObject is CircuitInOutView) {
			(treeNode.userObject as CircuitInOutView).bitWidth = it.newValue
		}
	}

	private val signalRepresentationHandler: EventHandler<CircuitInOutSignalRepresentationChanged> = {
		val treeNode = containerTree?.model?.getPortsTreeNode(it.circuitInOut.name!!)
		if (treeNode != null && treeNode.userObject is CircuitInOutView) {
			(treeNode.userObject as CircuitInOutView).signalRepresentation = it.newValue
		}
	}

    init {
	    eventBus.register(CircuitInOutBitWidthChanged::class, bitWidthHandler)
	    eventBus.register(CircuitInOutSignalRepresentationChanged::class, signalRepresentationHandler)
    }

	override fun dispose() {
		super.dispose()
		eventBus.unregister(bitWidthHandler)
		eventBus.unregister(signalRepresentationHandler)
	}
}