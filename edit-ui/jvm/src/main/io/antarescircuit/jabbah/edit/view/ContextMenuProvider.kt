package io.antarescircuit.jabbah.edit.view

import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.view.ContextMenuProvider
import io.antarescircuit.jabbah.edit.app.*
import javax.swing.JMenu
import javax.swing.JPopupMenu


open class EditContextMenuProvider : ContextMenuProvider {

	companion object {
		private val cutAction by lazy { ActionWrapperSwing(CutAction()) }
		private val copyAction by lazy { ActionWrapperSwing(CopyAction()) }
		private val duplicateAction by lazy { ActionWrapperSwing(DuplicateAction()) }
		private val deleteAction by lazy { ActionWrapperSwing(DeleteAction()) }
		private val rotateAction by lazy { ActionWrapperSwing(RotateAction()) }
		private val rotateClockwiseAction by lazy { ActionWrapperSwing(RotateAction(clockwise = true)) }
		private val toFrontAction by lazy { ActionWrapperSwing(ToFrontAction()) }
		private val toBackAction by lazy { ActionWrapperSwing(ToBackAction()) }
		private val oneDownAction by lazy { ActionWrapperSwing(OneDownAction()) }
		private val oneUpAction by lazy { ActionWrapperSwing(OneUpAction()) }
		val helpAction by lazy { ActionWrapperSwing(HelpComponentAction()) }
	}

	override var applicationName: String = ""

	override fun fillContextMenu(view: View<*>, x: Double, y: Double, menu: JPopupMenu) {
		fillContextMenu(view, menu)
	}

	private fun fillContextMenu(view: View<*>, popupMenu: JPopupMenu) {
		popupMenu.removeAll()
		addActions(view, popupMenu)
	}

	protected open fun addClipboardActions(popupMenu: JPopupMenu) {
		popupMenu.add(cutAction)
		popupMenu.add(copyAction)
		popupMenu.add(duplicateAction)
	}

	protected open fun addActions(view: View<*>, popupMenu: JPopupMenu) {
		addClipboardActions(popupMenu)
		popupMenu.addSeparator()
		popupMenu.add(deleteAction)
		popupMenu.add(rotateAction)
		popupMenu.add(rotateClockwiseAction)
		val arrangeMenu = JMenu(Translations.getString("edit.action.stackingOrder.name"))
		arrangeMenu.add(toFrontAction)
		arrangeMenu.add(oneUpAction)
		arrangeMenu.add(oneDownAction)
		arrangeMenu.add(toBackAction)
		popupMenu.add(arrangeMenu)
	}
}