package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.view.EditContextMenuProvider
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.container.ResetSubGraphVerticeViewAction
import ch.scorpion.jabbah.graph.container.editsubgraph.EditSubGraphVerticeViewAction
import ch.scorpion.jabbah.graph.ui.graphviewer.OpenSubGraphViewerAction
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import javax.swing.JPopupMenu

open class GraphContextMenuProvider(
	application: Application
) : EditContextMenuProvider() {

	companion object {
		private val openGraphAction by lazy { OpenGraphNavigationAction() }
		private val openGraphActionWrapper by lazy { ActionWrapperSwing(openGraphAction) }
		private val resetSubGraphAction by lazy { ActionWrapperSwing(ResetSubGraphVerticeViewAction()) }
	}

	private val extractMetaGraphAction = ActionWrapperSwing(ExtractMetaGraphAction(application.controller))

	private fun getGraphApplicationContextHolder(view: View<*>): GraphApplicationContextHolder? =
		view.applicationContextHolder as GraphApplicationContextHolder?

	override fun fillContextMenu(view: View<*>, x: Double, y: Double, menu: JPopupMenu) {
		openGraphAction.subGraphVerticeView = null
		if (getGraphApplicationContextHolder(view)?.scheduler?.isActive == true) {
			addExecutionActions(view, x, y, menu)
		} else {
			super.fillContextMenu(view, x, y, menu)
		}
	}

	override fun addActions(view: View<*>, popupMenu: JPopupMenu) {
		super.addActions(view, popupMenu)
		popupMenu.addSeparator()
		popupMenu.add(openGraphActionWrapper)
		getGraphApplicationContextHolder(view)?.let {
			popupMenu.add(ActionWrapperSwing(OpenSubGraphViewerAction(applicationName, it)))
			popupMenu.add(ActionWrapperSwing(EditSubGraphVerticeViewAction(it)))
		}
		popupMenu.add(resetSubGraphAction)
		popupMenu.add(extractMetaGraphAction)
		popupMenu.addSeparator()
		popupMenu.add(helpAction)
	}

	private fun addExecutionActions(view: View<*>, x: Double, y: Double, menu: JPopupMenu) {
		menu.removeAll()
		val drawable = (view as DrawingView<*>).drawing.getDrawableAt(x, y)
		if (drawable is ActorView) {
			addExecutionActions(view, drawable, menu)
		}
	}

	private fun addExecutionActions(view: View<*>, actorView: ActorView, menu: JPopupMenu) {
		if (actorView is SubGraphVerticeView<*>) {
			openGraphAction.subGraphVerticeView = actorView
			openGraphAction.enabled = true
			menu.add(openGraphActionWrapper)
			getGraphApplicationContextHolder(view)?.let {
				val action = OpenSubGraphViewerAction(applicationName, it, actorView)
				action.enabled = true
				menu.add(ActionWrapperSwing(action))
			}
		}
	}
}