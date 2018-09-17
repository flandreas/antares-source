package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.swing.dynamictree.*
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.graph.container.ContainerTreeFolderItem.Companion.CONTROLS_NAME
import ch.scorpion.jabbah.graph.container.ContainerTreeFolderItem.Companion.SUBGRAPHS_NAME
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.DeepVerticeLink
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.GraphPortView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortFactory
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode
import javax.swing.tree.TreeModel

enum class ContainerTreeItemType {
	Port,
	Control,
	Ports,
	Controls,
	SubGraphs,
	SubGraph
}

abstract class AbstractContainerTreeItem(
	val type: ContainerTreeItemType
) {
	abstract val description: String
}

abstract class DraggableTreeItem(
	type: ContainerTreeItemType,
	override val description: String,
	val factory: () -> Component,
	val iconPath: String
) : AbstractContainerTreeItem(type)

private class ContainerTreePortItem(
	val portName: String,
	description: String,
	factory: () -> Component,
	iconPath: String
) : DraggableTreeItem(ContainerTreeItemType.Port, description, factory, iconPath)

private class ContainerTreeControlItem(
	val controlViewId: String,
	description: String,
	factory: () -> Component,
	iconPath: String
)  : DraggableTreeItem(ContainerTreeItemType.Control, description, factory, iconPath)

private class ContainerTreeFolderItem(
	type: ContainerTreeItemType,
	private val translatedDesc: String
) : AbstractContainerTreeItem(type) {

	companion object {
		val CONTROLS_NAME = Translations.getString("graph.component.controls")
		val SUBGRAPHS_NAME = Translations.getString("graph.component.subgraphs")
		val PORTS_NAME = Translations.getString("graph.component.ports")
		val PORTS = ContainerTreeFolderItem(ContainerTreeItemType.Ports, PORTS_NAME)
		val CONTROLS = ContainerTreeFolderItem(ContainerTreeItemType.Controls, CONTROLS_NAME)
	}

	override val description: String
		get() = translatedDesc

	override fun toString(): String {
		return translatedDesc
	}
}

/**
 * The user object of a tree node that contains all [SubGraphVerticeView]s of a particular [GraphView].
 * @property graphView the [GraphView] whose [SubGraphVerticeView] are contained in the tree node
 */
private class SubgraphsFolderItem(
	val graphView: GraphView<*>,
	val link: DeepVerticeLink
) : AbstractContainerTreeItem(ContainerTreeItemType.SubGraphs) {

	override val description: String get() = SUBGRAPHS_NAME
}

/**
 * The user object of a tree node that represents a single [SubGraphVerticeView].
 * @property subGraphVerticeView the [SubGraphVerticeView], whose referenced [GraphView] can be opened by the tree node
 */
private class SubGraphVerticeViewFolderItem(
	val subGraphVerticeView: SubGraphVerticeView<SubGraphVertice>
) : AbstractContainerTreeItem(ContainerTreeItemType.SubGraph) {

	override val description: String get() = subGraphVerticeView.subGraphVertice?.name ?: "n.a."
}

private class ControlsFolderTreeItem(
	 val graphView: GraphView<*>,
	 val link: DeepVerticeLink
) : AbstractContainerTreeItem(ContainerTreeItemType.Controls) {

	override val description: String get() = CONTROLS_NAME
}

/** Incrementally builds and fills the tree used in the container panel.*/
class ContainerTree(
	private val portFactory: PortFactory = GraphViewModule.portFactory,
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	graphView: GraphView<*>,
	private val containerDrawing: ContainerDrawing
) : DynamicInitializer {

	companion object {
		private val LOG by logger(ContainerTree::class)
	}

	/** The top-level node that contains the [PortViewComponent]s.*/
	private val portsNode = DefaultMutableTreeNode(ContainerTreeFolderItem.PORTS)

	/** The top-level node that contains the [ControlViewSource]s. */
	private val controlsNode = DefaultMutableTreeNode(ContainerTreeFolderItem.CONTROLS)

	/** The node that contains the top-level [SubGraphVerticeViewFolderItem].*/
	private lateinit var subGraphsNode: DynamicTreeNode

	lateinit var treeModel: DynamicTreeModel
		private set

	init {
		createTreeModel(graphView, containerDrawing)
	}

	private fun createTreeModel(graphView: GraphView<*>, containerDrawing: ContainerDrawing) {
		treeModel = DynamicTreeModel("Container", this, false)
		fillGraphPortViews(graphView, containerDrawing)
		fillControlViewSources(graphView, containerDrawing)
		(treeModel.root as DefaultMutableTreeNode).add(portsNode)
		(treeModel.root as DefaultMutableTreeNode).add(controlsNode)
		subGraphsNode = DynamicTreeNode(SubgraphsFolderItem(graphView, DeepVerticeLink()), this, treeModel)
		(treeModel.root as DefaultMutableTreeNode).add(subGraphsNode)
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
					ContainerTreeItemType.SubGraphs -> addSubGraphVerticeNodes(value as SubgraphsFolderItem, receiver)
					ContainerTreeItemType.Controls -> addControlNodes(value as ControlsFolderTreeItem, receiver)
					else ->	receiver.addChildren(listOf())
				}
			}
		} else {
			receiver.addChildren(listOf())
		}
	}

	private fun createSubGraphVerticeViewTreeNode(vv: SubGraphVerticeView<SubGraphVertice>, link: DeepVerticeLink): MutableTreeNode {
		val treeNode = DefaultMutableTreeNode(SubGraphVerticeViewFolderItem(vv))
		val subGraphView = vv.createSubGraphView()
		treeNode.add(DynamicTreeNode(ControlsFolderTreeItem(subGraphView, link), this, treeModel, true))
		treeNode.add(DynamicTreeNode(SubgraphsFolderItem(subGraphView, link), this, treeModel, true))
		return treeNode
	}

	private fun addSubGraphVerticeNodes(item: SubgraphsFolderItem, receiver: DynamicReceiver) {
		receiver.addChildren(
			item.graphView.getSubGraphVerticeViews()
				.map { createSubGraphVerticeViewTreeNode(it, item.link.append(it.model!!.id)) })
	}

	/**
	 * Creates a [TreeNode] containing a [ContainerTreeControlItem] for all controls in the given [ControlsFolderTreeItem]
	 * and adds it to the specified [DynamicReceiver].
	 * TODO Refactor: Merge with [fillControlViewSources]
	 */
	private fun addControlNodes(item: ControlsFolderTreeItem, receiver: DynamicReceiver) {
		receiver.addChildren(
			item.graphView.getControlViewSources()
				.filter { containerDrawing.getControlViewComponent(item.link.append(it.model!!.id)) == null }
				.map { createControlViewNode(it, item.link) })
	}

	/** ---- [ContainerTree] */

	/**
	 * Creates a tree node for the specified [GraphPortView] and adds it the corresponding parent node [portsNode]
	 * of the managed [TreeModel].
	 */
	fun addGraphPortView(graphPortView: GraphPortView<*>) {
		val item = ContainerTreePortItem(
			graphPortView.model!!.name!!,
			"${graphPortView.model!!.portType} ${graphPortView.model!!.name!!}",
			{ portFactory.createPortViewComponent(portFactory.createPortView(portFactory.createSubGraphPort(graphPortView.model!!)))},
			graphPortView.iconPath
		)
		portsNode.add(DefaultMutableTreeNode(item))
		treeModel.nodesWereInserted(portsNode, intArrayOf(portsNode.childCount - 1))
	}

	/** Removes the [PortViewComponent] for the [Port] with the specified name from the [TreeModel]. */
	fun removeGraphPortView(portName: String) {
		val index = findGraphPortViewIndex(portName)
		if (index != null) {
			val child = portsNode.getChildAt(index)
			portsNode.remove(index)
			treeModel.nodesWereRemoved(portsNode, intArrayOf(index), arrayOf(child))
		}
	}

	/** Returns the [TreeNode] with the [PortViewComponent] for the [Port] with the specified name. */
	fun getPortsTreeNode(portName: String): DefaultMutableTreeNode? {
		val index = findGraphPortViewIndex(portName)
		if (index != null) {
			return portsNode.getChildAt(index) as DefaultMutableTreeNode
		}
		return null
	}

	/** Creates a tree node for the specified top-level [ControlViewSource] and adds it to the [TreeModel] .*/
	fun addControlViewSource(source: ControlViewSource<Vertice>) {
		controlsNode.add(createControlViewNode(source))
		treeModel.nodesWereInserted(controlsNode, intArrayOf(controlsNode.childCount - 1))
	}

	/** Removes the [ControlViewSource] with the specified ID from the [TreeModel]. */
	fun removeControlViewSource(controlId: String) {
		val index = findControlViewSourceIndex(controlId)
		if (index != null) {
			val child = controlsNode.getChildAt(index)
			controlsNode.remove(index)
			treeModel.nodesWereRemoved(controlsNode, intArrayOf(index), arrayOf(child))
		}
	}

	/** Adds the specified [SubGraphVerticeView] to the [TreeNode] with the top-level [SubgraphsFolderItem]. */
	fun addSubGraphVerticeView(vv: SubGraphVerticeView<SubGraphVertice>) {
		if (subGraphsNode.isInitialized) {
			subGraphsNode.add(createSubGraphVerticeViewTreeNode(vv, DeepVerticeLink(vv.model!!.id)))
		}
	}

	fun removeSubGraphVerticeView(vv: SubGraphVerticeView<SubGraphVertice>) {
		if (subGraphsNode.isInitialized) {
			val index = findSubGraphVerticeViewIndex(vv.id)
			if (index != null) {
				val child = subGraphsNode.getChildAt(index)
				subGraphsNode.remove(index)
				treeModel.nodesWereRemoved(subGraphsNode, intArrayOf(index), arrayOf(child))
			}
		}
	}

	/**
	 * Finds the index of the [ContainerTreePortItem] with the given name in the toplevel [portsNode].
	 * @return `null` if not found
	 */
	private fun findSubGraphVerticeViewIndex(id: Int): Int? {
		if (subGraphsNode.isInitialized) {
			for (index in 0 until subGraphsNode.childCount) {
				val item = (subGraphsNode.getChildAt(index) as DefaultMutableTreeNode).userObject as SubGraphVerticeViewFolderItem
				if (item.subGraphVerticeView.id == id) {
					return index
				}
			}
		}
		return null
	}

	/**
	 * Adds all toplevel [GraphPortView]s that are not contained in the [ContainerDrawing] to the [TreeModel].
	 */
	private fun fillGraphPortViews(graphView: GraphView<*>, containerDrawing: ContainerDrawing) {
		graphView.getGraphPortViews()
			.filter { containerDrawing.getPortViewComponent(it.model!!.name!!) == null }
			.forEach { addGraphPortView(it) }
	}

	/**
	 * Finds the index of the [ContainerTreePortItem] with the given name in the toplevel [portsNode].
	 * @return `null` if not found
	 */
	private fun findGraphPortViewIndex(portName: String): Int? {
		for (index in 0 until portsNode.childCount) {
			val item = (portsNode.getChildAt(index) as DefaultMutableTreeNode).userObject as ContainerTreePortItem
			if (item.portName == portName) {
				return index
			}
		}
		return null
	}


	/**
	 * Adds all toplevel [ControlViewSource]s that are not contained in the [ContainerDrawing] to the [TreeModel].
	 */
	private fun fillControlViewSources(graphView: GraphView<*>, containerDrawing: ContainerDrawing) {
		graphView.getControlViewSources()
			.filter { containerDrawing.getControlViewComponent(it.controlId!!) == null }
			.forEach { addControlViewSource(it) }
	}

	/**
	 * Creates a [MutableTreeNode] containing a [ContainerTreeControlItem] that can create a [ControlViewComponent]
	 * from the given [ControlViewSource]. The specified [DeepVerticeLink] reaches to the model of the [SubGraphVerticeView]
	 * that contains ´source´, but does NOT include the ID of the [ControlView]'s model.
	 */
	private fun createControlViewNode(source: ControlViewSource<Vertice>, baseLink: DeepVerticeLink = DeepVerticeLink.EMPTY): MutableTreeNode {
		return DefaultMutableTreeNode(ContainerTreeControlItem(
			source.controlId!!,
			source.controlName,
			{ ControlViewComponent(styleProvider, source.createControlView(), baseLink) },
			source.iconPath))
	}

	/**
	 * Finds the index of the [ContainerTreeControlItem] with the given control ID in the toplevel [controlsNode]
	 * @return `null`if not found
	 */
	private fun findControlViewSourceIndex(controlViewId: String): Int? {
		for (index in 0 until controlsNode.childCount) {
			val item = (controlsNode.getChildAt(index) as DefaultMutableTreeNode).userObject as ContainerTreeControlItem
			if (item.controlViewId == controlViewId) {
				return index
			}
		}
		return null
	}
}