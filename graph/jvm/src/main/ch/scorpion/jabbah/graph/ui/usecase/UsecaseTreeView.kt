package ch.scorpion.jabbah.graph.ui.usecase

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.richtext.RichTextLabel
import ch.scorpion.jabbah.edit.model.text.NamableTreeNode
import ch.scorpion.jabbah.edit.model.text.description.NameChangedEvent
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.ui.MetaGraphIconProvider
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Usecase
import ch.scorpion.jabbah.graph.view.UsecaseAddedEvent
import ch.scorpion.jabbah.graph.view.UsecaseRemovedEvent
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
	application: Application,
	applicationModeHolder: ApplicationModeHolder,
	applicationContextHolder: GraphApplicationContextHolder,
	eventBus: EventBus = BaseModule.eventBus
) : JTree() {

	/** The [JPopupMenu] to be displayed for the [GraphView] node.*/
	private val graphViewPopupMenu = JPopupMenu()

	/** The [JPopupMenu] to be displayed for a [Usecase] node.*/
	private val usecasePopupMenu = JPopupMenu()

	private val rightMouseListener = RightMouseListener()

	init {
		selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
		addMouseListener(rightMouseListener)

		setCellRenderer(UsecaseTreeRenderer())

		eventBus.register(UsecaseAddedEvent::class) {
			if (it.graphView === this.graphView) {
				val usecaseNode = usecaseTreeModel.addUsecase(it.usecase)
				selectionPath = JTreeUtil.getPath(usecaseNode)
			}
		}

		eventBus.register(UsecaseRemovedEvent::class) {
			if (it.graphView === this.graphView) {
				usecaseTreeModel.removeUsecase(it.usecase)
			}
		}

		eventBus.register(SchedulerActivationStateEvent::class) {
			if (it.scheduler === applicationContextHolder.scheduler) {
				if (it.scheduler.isActive) {
					selectionModel.clearSelection()
				}
				isEnabled = !it.scheduler.isActive
			}
		}

		eventBus.register(NameChangedEvent::class) {
			if (this.graphView != null && it.owner === this.graphView!!.graph) {
				usecaseTreeModel.updateGraphName()
			}
		}

		graphViewPopupMenu.add(ActionWrapperSwing(AddUsecaseAction(application, applicationModeHolder)))
		graphViewPopupMenu.addSeparator()
		graphViewPopupMenu.add(ActionWrapperSwing(RunAllTestsAction(application, applicationContextHolder.scheduler, applicationModeHolder = applicationModeHolder)))

		usecasePopupMenu.add(ActionWrapperSwing(DeleteUsecaseAction(application, applicationModeHolder)))
		usecasePopupMenu.add(ActionWrapperSwing(DuplicateUsecaseAction(application, applicationModeHolder)))
		usecasePopupMenu.addSeparator()
		usecasePopupMenu.add(ActionWrapperSwing(RunUsecaseAction(application, applicationContextHolder.scheduler, applicationModeHolder = applicationModeHolder)))
		usecasePopupMenu.add(ActionWrapperSwing(RunSingleUsecaseTestAction(application, applicationContextHolder.scheduler, applicationModeHolder = applicationModeHolder)))
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
			nodeChanged(graphViewNode)
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