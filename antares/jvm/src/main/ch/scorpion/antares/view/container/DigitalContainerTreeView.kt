package ch.scorpion.antares.view.container

import ch.scorpion.antares.model.inout.DigitalCircuitInOutBitWidthChanged
import ch.scorpion.antares.model.inout.DigitalCircuitInOutSignalRepresentationChanged
import ch.scorpion.antares.model.inout.DigitalCircuitInOutStartValueChanged
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
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

	private val bitWidthHandler: EventHandler<DigitalCircuitInOutBitWidthChanged> = {
		val treeNode = containerTree?.model?.getPortsTreeNode(it.circuitInOut.name!!)
		if (treeNode != null && treeNode.userObject is DigitalCircuitInOutView) {
			(treeNode.userObject as DigitalCircuitInOutView).bitWidth = it.newValue
		}
	}

	private val signalRepresentationHandler: EventHandler<DigitalCircuitInOutSignalRepresentationChanged> = {
		val treeNode = containerTree?.model?.getPortsTreeNode(it.circuitInOut.name!!)
		if (treeNode != null && treeNode.userObject is DigitalCircuitInOutView) {
			(treeNode.userObject as DigitalCircuitInOutView).signalRepresentation = it.newValue
		}
	}

	private val startValueHandler: EventHandler<DigitalCircuitInOutStartValueChanged> = {
		val treeNode = containerTree?.model?.getPortsTreeNode(it.circuitInOut.name!!)
		if (treeNode != null && treeNode.userObject is DigitalCircuitInOutView) {
			(treeNode.userObject as DigitalCircuitInOutView).startValue = it.newValue?.getValue()?.toLong()
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
}