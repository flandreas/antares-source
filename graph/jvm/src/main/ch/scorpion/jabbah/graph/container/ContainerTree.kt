package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.dynamictree.DynamicInitializer
import ch.scorpion.jabbah.base.swing.dynamictree.DynamicReceiver
import ch.scorpion.jabbah.base.swing.dynamictree.InitializerTreeNode
import ch.scorpion.jabbah.draw.DrawableContainerEvent
import ch.scorpion.jabbah.draw.DrawableContainerListener
import ch.scorpion.jabbah.draw.container.DrawableContainerAdapter
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.graph.view.ControlViewSourceEvent
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.editor.GraphPortViewEvent
import ch.scorpion.jabbah.graph.view.editor.SubGraphVerticeViewEvent
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortFactory
import javax.swing.tree.TreeModel
import javax.swing.tree.TreeNode

enum class ContainerTreeItemType {
	Port,
	Control,
	Ports,
	Controls,
	SubGraphs,
	SubGraph
}

/**
 * Controller class that incrementally builds and fills the [TreeModel] to be displayed in the [ContainerPanel].
 * Balances the contents of the [TreeModel] and the [ContainerDrawing] by making sure that a particular
 * element is only contained in either one of them.
 */
class ContainerTree(
	portFactory: PortFactory = GraphViewModule.portFactory,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	private val mainGraphView: GraphView<*>,
	private val containerDrawing: ContainerDrawing,
	private val eventBus: EventBus = BaseModule.eventBus
) : DynamicInitializer {

	companion object {
		private val LOG by logger(ContainerTree::class)
	}

	val model = ContainerTreeModel(portFactory, styleProvider, this, mainGraphView, containerDrawing)

	private val balancer = Balancer()

	private val graphPortViewEventHandler: EventHandler<GraphPortViewEvent> = {
		when(it.type) {
			GraphPortViewEvent.Type.ADD -> model.addGraphPortView(it.graphPortView)
			GraphPortViewEvent.Type.REMOVE -> model.removeGraphPortView(it.graphPortView.model!!.name!!)
		}
	}

	private val controlViewSourceEventHandler: EventHandler<ControlViewSourceEvent> = {
		when(it.type) {
			ControlViewSourceEvent.Type.ADD -> model.addControlViewSource(it.source)
			ControlViewSourceEvent.Type.REMOVE -> model.removeControlViewSource(it.source.controlId!!)
		}
	}

	private val subGraphVerticeViewEventHandler: EventHandler<SubGraphVerticeViewEvent> = {
		when(it.type) {
			SubGraphVerticeViewEvent.Type.ADD -> model.addSubGraphVerticeView(it.subGraphVerticeView)
			SubGraphVerticeViewEvent.Type.REMOVE -> model.removeSubGraphVerticeView(it.subGraphVerticeView)
		}
	}

	init {
		eventBus.register(GraphPortViewEvent::class, graphPortViewEventHandler)
		eventBus.register(ControlViewSourceEvent::class, controlViewSourceEventHandler)
		eventBus.register(SubGraphVerticeViewEvent::class, subGraphVerticeViewEventHandler)
		containerDrawing.addDrawableContainerListener(balancer)
	}

	fun dispose() {
		eventBus.unregister(GraphPortViewEvent::class, graphPortViewEventHandler)
		eventBus.unregister(ControlViewSourceEvent::class, controlViewSourceEventHandler)
		eventBus.unregister(SubGraphVerticeViewEvent::class, subGraphVerticeViewEventHandler)
		containerDrawing.removeDrawableContainerListener(balancer)
	}

	/** ---- [DynamicInitializer] */

	override fun createInitializerTreeNode(parent: TreeNode): TreeNode {
		return InitializerTreeNode(parent, Translations.getString("graph.action.loading.desc"))
	}

	override fun initialize(value: Any, receiver: DynamicReceiver) {
		LOG.trace("ContainerTree/DynamicInitializer: initialize $receiver")
		if (value is AbstractContainerTreeItem) {
			InvocationHandler.invoke {
				when (value.type) {
					ContainerTreeItemType.SubGraphs -> model.addSubGraphVerticeNodes(value as SubgraphsFolderItem, receiver)
					ContainerTreeItemType.Controls -> model.addControlNodes(value as ControlsFolderTreeItem, receiver)
					else ->	receiver.addChildren(listOf())
				}
			}
		} else {
			receiver.addChildren(listOf())
		}
	}


	/** ---- [ContainerTree] */

	/**
	 * Balances the contents of the [ContainerTreeView] and the [ContainerDrawing] such that each object
	 * is always contained only in one of them, but not in both. Must be added by the using class as
	 * [DrawableContainerListener] of the [ContainerDrawing].
	 */
	private inner class Balancer : DrawableContainerAdapter<Component>() {

		/** Removes the object that has been added to the [ContainerDrawing] from the [ContainerTreeView].*/
		override fun drawableAdded(event: DrawableContainerEvent<Component>) {
			if (event.child is PortViewComponent<*>) {
				model.removeGraphPortView((event.child as PortViewComponent<*>).port.name!!)
			}
			if (event.child is ControlViewComponent) {
				model.removeControlViewSource((event.child as ControlViewComponent).controlView!!.controlId!!)
			}
		}

		/**
		 * Adds the object of the main [GraphView] to the [ContainerTreeView] when its corresponding object
		 * has been removed from the [ContainerDrawing].
		 */
		override fun drawableRemoved(event: DrawableContainerEvent<Component>) {
			if (event.child is PortViewComponent<*>) {
				val graphPortView = mainGraphView.getGraphPortView((event.child as PortViewComponent<*>).port.name!!)
				if (graphPortView != null) {
					model.addGraphPortView(graphPortView)
				}
			}
			if (event.child is ControlViewComponent) {
				val cvs = mainGraphView.getControlViewSource((event.child as ControlViewComponent).controlView!!.controlId!!)
				if (cvs != null) {
					model.addControlViewSource(cvs)
				}
			}
		}
	}
}
