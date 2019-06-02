package ch.scorpion.antares.view.container

import ch.scorpion.antares.model.inout.CircuitInOutBitWidthChanged
import ch.scorpion.antares.model.inout.CircuitInOutSignalRepresentationChanged
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.jabbah.base.event.EventBus
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

    init {
        eventBus.register(CircuitInOutBitWidthChanged::class) {
	        val treeNode = containerTree?.model?.getPortsTreeNode(it.circuitInOut.name!!)
	        if (treeNode != null && treeNode.userObject is CircuitInOutView) {
		        (treeNode.userObject as CircuitInOutView).bitWidth = it.newValue
	        }
        }

	    eventBus.register(CircuitInOutSignalRepresentationChanged::class) {
		    val treeNode = containerTree?.model?.getPortsTreeNode(it.circuitInOut.name!!)
		    if (treeNode != null && treeNode.userObject is CircuitInOutView) {
			    (treeNode.userObject as CircuitInOutView).signalRepresentation = it.newValue
		    }
	    }
    }
}