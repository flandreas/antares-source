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
		private val cutAction by lazy { CutAction() }
		private val copyAction by lazy { CopyAction() }
		private val deleteAction by lazy { DeleteAction() }
		private val rotateAction by lazy { RotateAction() }
		private val toFrontAction by lazy { ToFrontAction() }
		private val toBackAction by lazy { ToBackAction() }
		private val oneDownAction by lazy { OneDownAction() }
		private val oneUpAction by lazy { OneUpAction() }
	}

	override fun fillContextMenu(view: View<*>, x: Double, y: Double, menu: JPopupMenu) {
		fillContextMenu(view, (view as DrawingView<*>).selectionManager.selection, menu)
	}

	private fun fillContextMenu(view: View<*>, selection: Collection<Component>, popupMenu: JPopupMenu) {
		popupMenu.removeAll()
		if (selection.size == 1) {
			addActions(view, popupMenu)
		}
	}

	protected open fun addClipboardActions(popupMenu: JPopupMenu) {
		popupMenu.add(ActionWrapperSwing(cutAction))
		popupMenu.add(ActionWrapperSwing(copyAction))
	}

	protected open fun addActions(view: View<*>, popupMenu: JPopupMenu) {
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