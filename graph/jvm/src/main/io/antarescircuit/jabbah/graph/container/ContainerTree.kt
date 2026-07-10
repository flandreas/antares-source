package io.antarescircuit.jabbah.graph.container

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.swing.dynamictree.DynamicInitializer
import io.antarescircuit.jabbah.base.swing.dynamictree.DynamicReceiver
import io.antarescircuit.jabbah.base.swing.dynamictree.InitializerTreeNode
import io.antarescircuit.jabbah.draw.DrawableContainerEvent
import io.antarescircuit.jabbah.draw.DrawableContainerListener
import io.antarescircuit.jabbah.draw.container.DrawableContainerAdapter
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.model.text.description.NameChangedEvent
import io.antarescircuit.jabbah.graph.model.GraphPortNameChanged
import io.antarescircuit.jabbah.graph.model.GraphPortTypeChanged
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.view.ControlViewSourceEvent
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.editor.GraphPortViewEvent
import io.antarescircuit.jabbah.graph.view.editor.SubGraphVerticeViewEvent
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.model.port.PortFactory
import io.antarescircuit.jabbah.graph.view.port.PortViewFactory
import javax.swing.tree.TreeModel
import javax.swing.tree.TreeNode

enum class ContainerTreeItemType {
	Port,
	Control,
	Ports,
	Controls,
	SubGraphs,
	SubGraph,
	Images,
	Image
}

/**
 * Controller class that incrementally builds and fills the [TreeModel] to be displayed in the [ContainerPanelSwing].
 * Balances the contents of the [TreeModel] and the [ContainerDrawing] by making sure that a particular
 * element is only contained in either one of them.
 */
class  ContainerTree(
	portFactory: PortFactory = GraphModelModule.portFactory,
	portViewFactory: PortViewFactory = GraphViewModule.portViewFactory,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	private val mainGraphView: GraphView,
	private val containerDrawing: ContainerDrawing,
	private val eventBus: EventBus = BaseModule.eventBus
) : DynamicInitializer {

	companion object {
		private val LOG by logger(ContainerTree::class)
	}

	var isManualContainer: Boolean = false

	val model = ContainerTreeModel(portFactory, portViewFactory, styleProvider, this, mainGraphView, containerDrawing)

	private val balancer = Balancer()

	private val graphPortViewEventHandler: EventHandler<GraphPortViewEvent> = {
		if (!requiresAutoLayout) {
			// Update tree model
			when (it.type) {
				GraphPortViewEvent.Type.ADD -> model.addGraphPortView(it.graphPortView)
				GraphPortViewEvent.Type.REMOVE -> model.removeGraphPortView(it.graphPortView.model.name!!)
			}
		} else {
			generateContainerDrawing()
		}
	}

	private val controlViewSourceEventHandler: EventHandler<ControlViewSourceEvent> = {
		when (it.type) {
			ControlViewSourceEvent.Type.ADD -> model.addControlViewSource(it.source)
			ControlViewSourceEvent.Type.REMOVE -> model.removeControlViewSource(it.source.controlId)
			ControlViewSourceEvent.Type.CHANGE -> model.updateControlViewSource(it.source)
		}
	}

	private val subGraphVerticeViewEventHandler: EventHandler<SubGraphVerticeViewEvent> = {
		when (it.type) {
			SubGraphVerticeViewEvent.Type.ADD -> model.addSubGraphVerticeView(it.subGraphVerticeView)
			SubGraphVerticeViewEvent.Type.REMOVE -> model.removeSubGraphVerticeView(it.subGraphVerticeView)
		}
	}

	private val graphPortNameHandler: EventHandler<GraphPortNameChanged<*>> = {
		if (it.newName != null) {
			model.handleGraphPortViewRenamed(it.newName)
		}
		if (requiresAutoLayout) {
			// Also check for old name to be independent of event dispatching order
			if (it.newName != null && containerDrawing.getPortViewComponent(it.newName) != null
				|| it.oldName != null && containerDrawing.getPortViewComponent(it.oldName) != null
			) {
				generateContainerDrawing()
			}
		}
	}

	private val graphNameHandler: EventHandler<NameChangedEvent> = {
		if (it.owner === mainGraphView.graph && !isManualContainer) {
			generateContainerDrawing()
		}
	}

	private val portTypeHandler: EventHandler<GraphPortTypeChanged<*>> = {
		if (it.graphPort.name != null) {
			model.handleGraphPortTypeChanged(it.graphPort.name!!)
		}
		if (requiresAutoLayout) {
			if (mainGraphView.graph!!.graphPorts.contains(it.graphPort)) {
				generateContainerDrawing()
			}
		}
	}

	init {
		eventBus.register(GraphPortViewEvent::class, graphPortViewEventHandler)
		eventBus.register(ControlViewSourceEvent::class, controlViewSourceEventHandler)
		eventBus.register(SubGraphVerticeViewEvent::class, subGraphVerticeViewEventHandler)
		eventBus.register(GraphPortNameChanged::class, graphPortNameHandler)
		eventBus.register(NameChangedEvent::class, graphNameHandler)
		eventBus.register(GraphPortTypeChanged::class, portTypeHandler)
		containerDrawing.addDrawableContainerListener(balancer)
	}

	fun dispose() {
		eventBus.unregister(GraphPortViewEvent::class, graphPortViewEventHandler)
		eventBus.unregister(ControlViewSourceEvent::class, controlViewSourceEventHandler)
		eventBus.unregister(SubGraphVerticeViewEvent::class, subGraphVerticeViewEventHandler)
		eventBus.unregister(GraphPortNameChanged::class, graphPortNameHandler)
		eventBus.unregister(NameChangedEvent::class, graphNameHandler)
		eventBus.unregister(GraphPortTypeChanged::class, portTypeHandler)
		containerDrawing.removeDrawableContainerListener(balancer)
	}

	/** ---- [DynamicInitializer] */

	override fun createInitializerTreeNode(parent: TreeNode): TreeNode =
		InitializerTreeNode(parent, Translations.getString("graph.action.loading.desc"))

	override fun initialize(value: Any, receiver: DynamicReceiver) {
		LOG.trace("ContainerTree/DynamicInitializer: initialize $receiver")
		if (value is AbstractContainerTreeItem) {
			InvocationHandler.invoke {
				when (value.type) {
					ContainerTreeItemType.SubGraphs -> model.addSubGraphVerticeNodes(value as SubGraphsFolderItem, receiver)
					ContainerTreeItemType.Controls -> model.addControlNodes(value as ControlsFolderTreeItem, receiver)
					ContainerTreeItemType.Images -> model.addImages(receiver)
					else ->	receiver.addChildren(listOf())
				}
			}
		} else {
			receiver.addChildren(listOf())
		}
	}

	/** ---- [ContainerTree] */

	private val requiresAutoLayout: Boolean get() = !isManualContainer &&
		CurrentContainerDrawingLayouter.value.doesLayout

	fun generateContainerDrawing() {
		// Keep the balancer active while layouting, so that PortViews are removed from the tree when the layouter
		// adds them to the containerDrawing
		CurrentContainerDrawingLayouter.value.layout(mainGraphView, containerDrawing, addLabel = true)
	}

	/**
	 * Balances the contents of the [ContainerTreeView] and the [ContainerDrawing] such that each object
	 * is always contained only in one of them, but not in both. Must be added by the using class as
	 * [DrawableContainerListener] of the [ContainerDrawing].
	 */
	private inner class Balancer : DrawableContainerAdapter<Component>() {

		/** Removes the object that has been added to the [ContainerDrawing] from the [ContainerTreeView].*/
		override fun drawableAdded(event: DrawableContainerEvent<Component>) {
			if (event.child is PortViewComponent) {
				model.removeGraphPortView((event.child as PortViewComponent).port.name!!)
			}
			if (event.child is ControlViewComponent) {
				val link = (event.child as ControlViewComponent).controlModelLink
				if (link.size == 1) {
					model.removeControlViewSource((event.child as ControlViewComponent).controlView.controlId!!)
				} else {
					model.removeControlViewSource((event.child as ControlViewComponent).controlModelLink)
				}
			}
		}

		/**
		 * Adds the object of the main [GraphView] to the [ContainerTreeView] when its corresponding object
		 * has been removed from the [ContainerDrawing].
		 */
		override fun drawableRemoved(event: DrawableContainerEvent<Component>) {
			if (event.child is PortViewComponent) {
				val graphPortView = mainGraphView.getGraphPortView((event.child as PortViewComponent).port.name!!)
				if (graphPortView != null) {
					model.addGraphPortView(graphPortView)
				}
			}
			if (event.child is ControlViewComponent) {
				val comp = event.child as ControlViewComponent
				if (comp.controlModelLink.size == 1) {
					val cvs = mainGraphView.getControlViewSource((event.child as ControlViewComponent).controlView.controlId!!)
					if (cvs != null) {
						model.addControlViewSource(cvs)
					}
				} else {
					model.addControlViewSourceFor(event.child as ControlViewComponent)
				}
			}
		}
	}
}
