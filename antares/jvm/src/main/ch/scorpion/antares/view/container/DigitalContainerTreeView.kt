package ch.scorpion.antares.view.container

import ch.scorpion.antares.model.inout.CircuitInOutBitWidthChanged
import ch.scorpion.antares.model.inout.CircuitInOutSignalRepresentationChanged
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.container.ContainerTreeView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortFactory

class DigitalContainerTreeView(
    portFactory: PortFactory,
    styleProvider: StyleProvider,
    eventBus: EventBus
) : ContainerTreeView(portFactory, styleProvider, eventBus) {

    constructor(): this(GraphViewModule.portFactory, DrawStyleModule.styleProvider, BaseModule.eventBus)

    init {
        eventBus.register(CircuitInOutBitWidthChanged::class, {
            val treeNode = getPortsTreeNode(it.circuitInOut.name!!)
            if (treeNode != null && treeNode.userObject is CircuitInOutView) {
                (treeNode.userObject as CircuitInOutView).bitWidth = it.newValue
            }
        })

        eventBus.register(CircuitInOutSignalRepresentationChanged::class, {
            val treeNode = getPortsTreeNode(it.circuitInOut.name!!)
            if (treeNode != null && treeNode.userObject is CircuitInOutView) {
                (treeNode.userObject as CircuitInOutView).signalRepresentation = it.newValue
            }
        })
    }
}