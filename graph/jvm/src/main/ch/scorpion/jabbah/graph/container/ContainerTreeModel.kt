package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.base.swing.dynamictree.DynamicInitializer
import ch.scorpion.jabbah.base.swing.dynamictree.DynamicReceiver
import ch.scorpion.jabbah.base.swing.dynamictree.DynamicTreeModel
import ch.scorpion.jabbah.base.swing.dynamictree.DynamicTreeNode
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.port.PortFactory
import ch.scorpion.jabbah.graph.model.vertice.DeepVerticeLink
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.GraphPortView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortViewFactory
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeModel
import javax.swing.tree.TreeNode

class ContainerTreeModel(
	private val portFactory: PortFactory = GraphModelModule.portFactory,
	private val portViewFactory: PortViewFactory = GraphViewModule.portViewFactory,
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	private val initializer: DynamicInitializer,
	graphView: GraphView,
	private val containerDrawing: ContainerDrawing
) {

	val treeModel = DynamicTreeModel("Container", initializer, false)

	/** The top-level node that contains the [PortViewComponent]s.*/
	private val portsNode = DefaultMutableTreeNode(ContainerTreeFolderItem.PORTS)

	/** The top-level node that contains the [ControlViewSource]s. */
	private val controlsNode = DefaultMutableTreeNode(ContainerTreeFolderItem.CONTROLS)

	/** The node that contains the top-level [SubGraphVerticeViewFolderItem].*/
	private var subGraphsNode: DynamicTreeNode

	init {
		fillGraphPortViews(graphView, containerDrawing)
		fillControlViewSources(graphView, containerDrawing)
		(treeModel.root as DefaultMutableTreeNode).add(portsNode)
		(treeModel.root as DefaultMutableTreeNode).add(controlsNode)
		subGraphsNode = DynamicTreeNode(SubGraphsFolderItem(graphView, DeepVerticeLink()), initializer, treeModel)
		(treeModel.root as DefaultMutableTreeNode).add(subGraphsNode)
	}

	/** ---- Ports management */

	/**
	 * Creates a tree node for the specified [GraphPortView] and adds it the corresponding parent node [portsNode]
	 * of the managed [TreeModel].
	 */
	fun addGraphPortView(graphPortView: GraphPortView<*>) {
		val item = ContainerTreePortItem(graphPortView) {
			portViewFactory.createPortViewComponent(portViewFactory.createPortView(portFactory.createSubGraphPort(graphPortView.model)))
		}
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

	/**
	 * Adds all toplevel [GraphPortView]s that are not contained in the [ContainerDrawing] to the [TreeModel].
	 */
	private fun fillGraphPortViews(graphView: GraphView, containerDrawing: ContainerDrawing) {
		graphView.getGraphPortViews()
			.filter { containerDrawing.getPortViewComponent(it.model.name!!) == null }
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

	/** ---- Controls management */

	/**
	 * Creates a [TreeNode] containing a [ContainerTreeControlItem] for all controls in the given [ControlsFolderTreeItem]
	 * and adds it to the specified [DynamicReceiver].
	 * TODO Refactor: Merge with [fillControlViewSources]
	 */
	fun addControlNodes(item: ControlsFolderTreeItem, receiver: DynamicReceiver) {
		receiver.addChildren(
			item.graphView.getControlViewSources()
				.filter { containerDrawing.getControlViewComponent(item.link.append(it.model.id)) == null }
				.map { createControlViewNode(it, item.link) })
	}

	/** Creates a tree node for the specified top-level [ControlViewSource] and adds it to the [TreeModel] .*/
	fun addControlViewSource(source: ControlViewSource<Vertice>) {
		controlsNode.add(createControlViewNode(source))
		treeModel.nodesWereInserted(controlsNode, intArrayOf(controlsNode.childCount - 1))
	}

	fun addControlViewSourceFor(comp: ControlViewComponent) {
		val treeNode = findSubGraphVerticeViewFolder(comp.controlModelLink)
		if (treeNode != null && (treeNode.getChildAt(0) as DynamicTreeNode).isInitialized) {
			val subGraphVerticeView = (treeNode as DefaultMutableTreeNode).userObject as SubGraphVerticeViewFolderItem
			val innerFolder = treeNode.getChildAt(0) as DynamicTreeNode
			val source = subGraphVerticeView.graphView.getControlViewSource(comp.controlView.controlId!!)
			innerFolder.add(createControlViewNode(source!!, comp.controlModelLink.withoutLast()))
			treeModel.nodesWereInserted(innerFolder, intArrayOf(innerFolder.childCount - 1))
		}
	}

	fun updateControlViewSource(source: ControlViewSource<Vertice>) {
		val index = findControlViewSourceIndex(source.controlId!!)
		if (index != null) {
			treeModel.nodesChanged(controlsNode, intArrayOf(index))
		}
	}

	/** Removes the top-level [ControlViewSource] with the specified ID from the [TreeModel]. */
	fun removeControlViewSource(controlId: String) {
		val index = findControlViewSourceIndex(controlId)
		if (index != null) {
			val child = controlsNode.getChildAt(index)
			controlsNode.remove(index)
			treeModel.nodesWereRemoved(controlsNode, intArrayOf(index), arrayOf(child))
		}
	}

	/** Removes the [ControlViewSource] that matches the specified [DeepVerticeLink] from the [TreeModel]. */
	fun removeControlViewSource(link: DeepVerticeLink) {
		val treeNode = findSubGraphVerticeViewFolder(link)
		if (treeNode != null && (treeNode.getChildAt(0) as DynamicTreeNode).isInitialized) {
			val innerFolder = treeNode.getChildAt(0) as DefaultMutableTreeNode
			val index = findControlViewSourceIndexForModelId(innerFolder, link.last)
			if (index != null) {
				val child = innerFolder.getChildAt(index)
				innerFolder.remove(index)
				treeModel.nodesWereRemoved(innerFolder, intArrayOf(index), arrayOf(child))
			}
		}
	}

	private fun findSubGraphVerticeViewFolder(link: DeepVerticeLink): TreeNode? {
		val subGraphVerticeViewLink = link.withoutLast()
		return JTreeUtil.findTreeNode(treeModel.root as TreeNode) {
			it is DefaultMutableTreeNode
				&& it.childCount > 0
				&& it.userObject is SubGraphVerticeViewFolderItem
				&& (it.userObject as SubGraphVerticeViewFolderItem).link == subGraphVerticeViewLink
		}
	}

	/**
	 * Adds all toplevel [ControlViewSource]s that are not contained in the [ContainerDrawing] to the [TreeModel].
	 */
	private fun fillControlViewSources(graphView: GraphView, containerDrawing: ContainerDrawing) {
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
			source,
			{ ControlViewComponent(styleProvider, source, baseLink) },
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

	private fun findControlViewSourceIndexForModelId(folder: TreeNode, modelId: Int): Int? {
		for (index in 0 until folder.childCount) {
			val item = (folder.getChildAt(index) as DefaultMutableTreeNode).userObject as ContainerTreeControlItem
			if (item.controlModelId == modelId) {
				return index
			}
		}
		return null
	}

	/** ---- SubGraphVerticeViews management */

	fun addSubGraphVerticeNodes(item: SubGraphsFolderItem, receiver: DynamicReceiver) {
		receiver.addChildren(
			item.graphView.getSubGraphVerticeViews()
				.map { createSubGraphVerticeViewTreeNode(it, item.link.append(it.model.id)) })
	}

	/** Adds the specified [SubGraphVerticeView] to the [TreeNode] with the top-level [SubGraphsFolderItem]. */
	fun addSubGraphVerticeView(vv: SubGraphVerticeView<*>) {
		if (subGraphsNode.isInitialized) {
			subGraphsNode.add(createSubGraphVerticeViewTreeNode(vv, DeepVerticeLink(vv.model.id)))
		}
	}

	fun removeSubGraphVerticeView(vv: SubGraphVerticeView<*>) {
		if (subGraphsNode.isInitialized) {
			val index = findSubGraphVerticeViewIndex(vv.id)
			if (index != null) {
				val child = subGraphsNode.getChildAt(index)
				subGraphsNode.remove(index)
				treeModel.nodesWereRemoved(subGraphsNode, intArrayOf(index), arrayOf(child))
			}
		}
	}

	private fun createSubGraphVerticeViewTreeNode(vv: SubGraphVerticeView<*>, link: DeepVerticeLink): MutableTreeNode {
		val subGraphView = vv.createSubGraphView()
		val treeNode = DefaultMutableTreeNode(SubGraphVerticeViewFolderItem(subGraphView, vv, link))
		treeNode.add(DynamicTreeNode(ControlsFolderTreeItem(subGraphView, link), initializer, treeModel, true))
		treeNode.add(DynamicTreeNode(SubGraphsFolderItem(subGraphView, link), initializer, treeModel, true))
		return treeNode
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
}

abstract class AbstractContainerTreeItem(
	val type: ContainerTreeItemType
) {
	abstract fun getDescription(): String
}

abstract class DraggableTreeItem(
	type: ContainerTreeItemType,
	val factory: () -> Component,
	val iconPath: String
) : AbstractContainerTreeItem(type)

class ContainerTreePortItem(
	val graphPortView: GraphPortView<*>,
	factory: () -> Component
) : DraggableTreeItem(ContainerTreeItemType.Port, factory, graphPortView.iconPath) {

	val portName: String get() = StringUtils.replaceNegation(graphPortView.model.name!!)

	override fun getDescription(): String {
		return "${graphPortView.model.portType} $portName"
	}
}

private class ContainerTreeControlItem(
	private val source: ControlViewSource<Vertice>,
	factory: () -> Component,
	iconPath: String
) : DraggableTreeItem(ContainerTreeItemType.Control, factory, iconPath) {

	val controlViewId: String get() = source.controlId!!
	val controlModelId: Int get() = source.model.id
	val controlName: String get() = StringUtils.replaceNegation(source.controlName)

	override fun getDescription(): String = controlName
}

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

	override fun getDescription(): String = translatedDesc

	override fun toString(): String = translatedDesc
}

/**
 * The user object of a tree node that contains all [SubGraphVerticeView]s of a particular [GraphView].
 * @property graphView the [GraphView] whose [SubGraphVerticeView] are contained in the tree node
 */
class SubGraphsFolderItem(
	val graphView: GraphView,
	val link: DeepVerticeLink
) : AbstractContainerTreeItem(ContainerTreeItemType.SubGraphs) {

	override fun getDescription(): String = ContainerTreeFolderItem.SUBGRAPHS_NAME
}

/**
 * The user object of a tree node that represents a single [SubGraphVerticeView].
 * @property subGraphVerticeView the [SubGraphVerticeView] whose referenced [GraphView] can be opened by the tree node
 */
class SubGraphVerticeViewFolderItem(
	val graphView: GraphView,
	val subGraphVerticeView: SubGraphVerticeView<*>,
	val link: DeepVerticeLink
) : AbstractContainerTreeItem(ContainerTreeItemType.SubGraph) {

	override fun getDescription(): String = subGraphVerticeView.subGraphVertice?.name ?: "n.a."
}

class ControlsFolderTreeItem(
	val graphView: GraphView,
	val link: DeepVerticeLink
) : AbstractContainerTreeItem(ContainerTreeItemType.Controls) {

	override fun getDescription(): String = ContainerTreeFolderItem.CONTROLS_NAME
}
