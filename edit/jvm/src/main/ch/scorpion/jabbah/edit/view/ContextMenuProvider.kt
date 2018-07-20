package ch.scorpion.jabbah.edit.view

import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.ContextMenuProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.app.*
import javax.swing.JMenu
import javax.swing.JPopupMenu


open class EditContextMenuProvider : ContextMenuProvider {

	companion object {
		private val cutAction = CutAction()
		private val copyAction = CopyAction()
		private val deleteAction = DeleteAction()
		private val rotateAction = RotateAction()
		private val toFrontAction = ToFrontAction()
		private val toBackAction = ToBackAction()
		private val oneDownAction = OneDownAction()
		private val oneUpAction = OneUpAction()
	}

	override fun fillContextMenu(view: View<*>, x: Double, y: Double, menu: JPopupMenu) {
		fillContextMenu((view as DrawingView<*>).selectionManager.selection, menu)
	}

	private fun fillContextMenu(selection: Collection<Component>, popupMenu: JPopupMenu) {
		popupMenu.removeAll()
		if (selection.size == 1) {
			addActions(popupMenu)
		}
	}

	protected open fun addClipboardActions(popupMenu: JPopupMenu) {
		popupMenu.add(ActionWrapperSwing(cutAction))
		popupMenu.add(ActionWrapperSwing(copyAction))
	}

	protected open fun addActions(popupMenu: JPopupMenu) {
		addClipboardActions(popupMenu)
		popupMenu.addSeparator()
		popupMenu.add(ActionWrapperSwing(deleteAction))
		popupMenu.add(ActionWrapperSwing(rotateAction))
		val arrangeMenu = JMenu(Translations.getString("edit.action.stackingOrder.name"))
		arrangeMenu.add(ActionWrapperSwing(toFrontAction))
		arrangeMenu.add(ActionWrapperSwing(oneUpAction))
		arrangeMenu.add(ActionWrapperSwing(oneDownAction))
		arrangeMenu.add(ActionWrapperSwing(toBackAction))
		popupMenu.add(arrangeMenu)
	}
}