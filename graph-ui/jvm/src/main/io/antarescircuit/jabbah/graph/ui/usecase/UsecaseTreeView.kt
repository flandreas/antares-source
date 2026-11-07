package io.antarescircuit.jabbah.graph.ui.usecase

import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.swing.JTreeUtil
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.base.ui.UIBasics.PROP_TREE_SHOW_ROOT_HANDLES
import io.antarescircuit.jabbah.draw.graphics.Font
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJvm
import io.antarescircuit.jabbah.draw.richtext.RichTextLabel
import io.antarescircuit.jabbah.edit.model.text.NamableTreeNode
import io.antarescircuit.jabbah.edit.model.text.description.NameChangedEvent
import io.antarescircuit.jabbah.execution.scheduler.SchedulerActivationStateEvent
import io.antarescircuit.jabbah.graph.ui.MetaGraphIconProvider
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.Usecase
import io.antarescircuit.jabbah.graph.view.UsecaseAddedEvent
import io.antarescircuit.jabbah.graph.view.UsecaseRemovedEvent
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPopupMenu
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreeSelectionModel

/** Displays the tree of [Usecase]s of a [GraphView].*/
class UsecaseTreeView(
	private val controller: UsecaseViewController,
	private val eventBus: EventBus = BaseModule.eventBus
) : JTree() {

	/** The [JPopupMenu] to be displayed for the [GraphView] node.*/
	private val graphViewPopupMenu = JPopupMenu()

	/** The [JPopupMenu] to be displayed for a [Usecase] node.*/
	private val usecasePopupMenu = JPopupMenu()

	private val rightMouseListener = RightMouseListener()

	private val usecaseAddedHandler: EventHandler<UsecaseAddedEvent> = {
		if (it.graphView === this.graphView) {
			val usecaseNode = usecaseTreeModel.addUsecase(it.usecase)
			selectionPath = JTreeUtil.getPath(usecaseNode)
		}
	}

	private val usecaseRemovedHandler: EventHandler<UsecaseRemovedEvent> = {
		if (it.graphView === this.graphView) {
			usecaseTreeModel.removeUsecase(it.usecase)
		}
	}

	private val schedulerActivationStateHandler: EventHandler<SchedulerActivationStateEvent> = {
		if (it.scheduler === controller.applicationContextHolder.scheduler) {
			if (it.scheduler.isActive) {
				selectionModel.clearSelection()
			}
			isEnabled = !it.scheduler.isActive
		}
	}

	private val nameChangedHandler: EventHandler<NameChangedEvent> = {
		if (this.graphView != null && it.owner === this.graphView!!.graph) {
			usecaseTreeModel.updateGraphName()
		} else if (it.owner is Usecase) {
			usecaseTreeModel.updateUsecaseName(it.owner as Usecase)
		}
	}

	init {
		setShowsRootHandles(BaseModule.properties.getBoolean(PROP_TREE_SHOW_ROOT_HANDLES))
		selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
		addMouseListener(rightMouseListener)

		setCellRenderer(UsecaseTreeRenderer())
		setRowHeight(24)

		eventBus.register(UsecaseAddedEvent::class, usecaseAddedHandler)
		eventBus.register(UsecaseRemovedEvent::class, usecaseRemovedHandler)
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
		eventBus.register(NameChangedEvent::class, nameChangedHandler)

		graphViewPopupMenu.add(ActionWrapperSwing(AddUsecaseAction(controller)))
		graphViewPopupMenu.addSeparator()
		graphViewPopupMenu.add(ActionWrapperSwing(RunAllTestsAction(controller)))

		usecasePopupMenu.add(ActionWrapperSwing(DeleteUsecaseAction(controller)))
		usecasePopupMenu.add(ActionWrapperSwing(DuplicateUsecaseAction(controller)))
		usecasePopupMenu.add(ActionWrapperSwing(RecordUsecaseAction(controller)))
		usecasePopupMenu.addSeparator()
		usecasePopupMenu.add(ActionWrapperSwing(RunUsecaseAction(controller)))
		usecasePopupMenu.add(ActionWrapperSwing(RunSingleUsecaseTestAction(controller)))
	}

	fun dispose() {
		eventBus.unregister(usecaseAddedHandler)
		eventBus.unregister(usecaseRemovedHandler)
		eventBus.unregister(schedulerActivationStateHandler)
		eventBus.unregister(nameChangedHandler)
	}

	/** Holds the [GraphView] whose [Usecase]s are displayed by this [UsecaseTreeView].*/
	var graphView: GraphView? = null
		set(value) {
			if (field != value) {
				field = value
				// TODO How about null?
				model = UsecaseTreeModel(field!!, Graphics2DJvm.fromAwtFont(font))
			}
		}

	val selectedUsecase: Usecase?
		get() {
			val path = selectionPath ?: return null
			val selectedObj = (path.lastPathComponent as DefaultMutableTreeNode).userObject
			if (selectedObj is Usecase) {
				return selectedObj
			}
			return null
		}

	private val usecaseTreeModel: UsecaseTreeModel get() = model!! as UsecaseTreeModel

	private inner class RightMouseListener : MouseAdapter() {
		override fun mousePressed(e: MouseEvent?) {
			if (e?.button == MouseEvent.BUTTON3) {
				getPathForLocation(e.x, e.y)?.let { path ->
					requestFocusInWindow()
					selectionPath = path

					val menu = when ((path.lastPathComponent as DefaultMutableTreeNode).userObject) {
						is GraphView -> graphViewPopupMenu
						is Usecase -> usecasePopupMenu
						else -> null
					}

					menu?.let {
						it.show(this@UsecaseTreeView, e.x, e.y)
					}
				}
			}
		}
	}

	private class UsecaseTreeModel(
		graphView: GraphView,
		font: Font
	) : DefaultTreeModel(NamableTreeNode(graphView, font)) {

		private val graphViewNode: DefaultMutableTreeNode get() = root as DefaultMutableTreeNode

		init {
			graphView.usecases.getUsecases().forEach { addUsecase(it) }
			nodeStructureChanged(root)
		}

		fun updateGraphName() {
			if (graphViewNode is NamableTreeNode) {
				(graphViewNode as NamableTreeNode).richTextName.reset()
			}
			nodeChanged(graphViewNode)
		}

		fun updateUsecaseName(usecase: Usecase) {
			findUsecaseNode(usecase)?.let { nodeChanged(it) }
		}

		fun addUsecase(usecase: Usecase): TreeNode {
			val usecaseNode = DefaultMutableTreeNode(usecase)
			graphViewNode.add(usecaseNode)
			nodesWereInserted(graphViewNode, intArrayOf(graphViewNode.childCount - 1))
			return usecaseNode
		}

		fun removeUsecase(usecase: Usecase) {
			val usecaseNode = findUsecaseNode(usecase)
			val index = getUsecaseIndex(usecase)
			if (index >= 0) {
				graphViewNode.remove(index)
				nodesWereRemoved(graphViewNode, intArrayOf(index), arrayOf(usecaseNode))
			}
		}

		private fun findUsecaseNode(usecase: Usecase): TreeNode? {
			for (e in graphViewNode.depthFirstEnumeration()) {
				if ((e as DefaultMutableTreeNode).userObject == usecase) {
					return e
				}
			}
			return null
		}

		private fun getUsecaseIndex(usecase: Usecase): Int {
			for (index in 0 until graphViewNode.childCount) {
				val item = (graphViewNode.getChildAt(index) as DefaultMutableTreeNode).userObject as Usecase
				if (item == usecase) {
					return index
				}
			}
			return -1
		}
	}

	private class UsecaseTreeRenderer : RichTextLabel() {

		companion object {
			private val usecaseIcon = UiUtil.themedIcon("/img/usecase-16.png")
		}

		override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
			val component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus) as RichTextLabel
			component.richText = null

			when ((value as DefaultMutableTreeNode).userObject) {
				is Usecase -> {
					component.icon = usecaseIcon
					component.disabledIcon = usecaseIcon
				}
				is GraphView -> {
					val icon = (value.userObject as GraphView).graph?.let {
						MetaGraphIconProvider.provideIcon(it.type, false, StringUtils.isNotBlank(it.script))
					}
					component.richText = (value as NamableTreeNode).richTextName.value
					component.icon = icon
					component.disabledIcon = icon
				}
			}

			return component
		}
	}
}