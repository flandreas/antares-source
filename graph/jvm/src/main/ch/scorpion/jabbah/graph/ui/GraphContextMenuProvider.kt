package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.view.ContextMenuProvider
import ch.scorpion.jabbah.edit.view.EditContextMenuProvider
import ch.scorpion.jabbah.graph.container.EditSubGraphVerticeViewAction
import javax.swing.JPopupMenu

open class GraphContextMenuProvider : EditContextMenuProvider() {

	companion object {
		private val cutAction = ActionWrapperSwing(CutAction())
		private val copyAction = ActionWrapperSwing(CopyAction())
		private val openGraphAction = ActionWrapperSwing(OpenGraphNavigationPanelAction())
		private val editSubgraphAction = ActionWrapperSwing(EditSubGraphVerticeViewAction())
	}

	override fun addClipboardActions(popupMenu: JPopupMenu) {
		popupMenu.add(cutAction)
		popupMenu.add(copyAction)
	}

	override fun addActions(popupMenu: JPopupMenu) {
		super.addActions(popupMenu)
		popupMenu.addSeparator()
		popupMenu.add(openGraphAction)
		popupMenu.add(editSubgraphAction)
	}

}