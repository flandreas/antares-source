package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.view.EditContextMenuProvider
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.container.EditSubGraphVerticeViewAction
import ch.scorpion.jabbah.graph.container.ResetSubGraphVerticeViewAction
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import javax.swing.JPopupMenu

open class GraphContextMenuProvider(
	private val scheduler: Scheduler = ExecutionModule.scheduler
) : EditContextMenuProvider() {

	companion object {
		private val cutAction = ActionWrapperSwing(CutAction())
		private val copyAction = ActionWrapperSwing(CopyAction())
		private val openGraphAction by lazy { OpenGraphNavigationPanelAction() }
		private val editSubGraphAction = ActionWrapperSwing(EditSubGraphVerticeViewAction())
		private val resetSubGraphAction = ActionWrapperSwing(ResetSubGraphVerticeViewAction())
	}

	override fun fillContextMenu(view: View<*>, x: Double, y: Double, menu: JPopupMenu) {
		if (scheduler.isActive) {
			addExecutionActions(view, x, y, menu)
		} else {
			super.fillContextMenu(view, x, y, menu)
		}
	}

	override fun addClipboardActions(popupMenu: JPopupMenu) {
		popupMenu.add(cutAction)
		popupMenu.add(copyAction)
	}

	override fun addActions(popupMenu: JPopupMenu) {
		super.addActions(popupMenu)
		popupMenu.addSeparator()
		popupMenu.add(ActionWrapperSwing(openGraphAction))
		popupMenu.add(editSubGraphAction)
		popupMenu.add(resetSubGraphAction)
	}

	private fun addExecutionActions(view: View<*>, x: Double, y: Double, menu: JPopupMenu) {
		menu.removeAll()
		val drawable = (view as DrawingView<*>).drawing.getDrawableAt(x, y)
		if (drawable is ActorView) {
			addExecutionActions(drawable, menu)
		}
	}

	private fun addExecutionActions(actorView: ActorView, menu: JPopupMenu) {
		if (actorView is SubGraphVerticeView<*>) {
			openGraphAction.subGraphVerticeView = actorView
			menu.add(ActionWrapperSwing(openGraphAction))
		}
	}
}