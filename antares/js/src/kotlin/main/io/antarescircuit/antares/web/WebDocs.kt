package io.antarescircuit.antares.web

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.graph.ui.documentation.OpenDocumentationAction
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Markdown documentation of circuits: the documentation of the open circuit (desktop: `DocumentationPanelSwing`,
 * edited through the shared `DocumentationPanelController`) and the documentation of a selected sub-circuit,
 * shown in the `documentation` dialog (desktop: `OpenDocumentationAction`, `DocumentationDesktopViewItemSwing`).
 */
@JsExport
class WebDocs internal constructor(private val ctx: WebEditorContext) {

	private val panel get() = ctx.frame().documentationPanelController

	/** ---- Documentation of the open circuit */

	/** Describes the documentation of the open circuit as JSON: text and editable, or `null` without circuit. */
	fun circuit(): String = ctx.guarded("null") {
		val controller = panel
		if (controller.content == null) return@guarded "null"
		val text = controller.text ?: ""
		val editable = controller.editable && !ctx.frame().applicationModeHolder.currentMode.isExecute()
		toJson(obj {
			this.text = text
			this.editable = editable
		})
	}

	/** Sets the documentation of the open circuit to [text] as one undoable change, like the desktop's text area. */
	fun setCircuitText(text: String) = ctx.guarded(Unit) {
		val controller = panel
		if (controller.content == null || !controller.editable || text == (controller.text ?: "")) return@guarded
		val view = controller.view as WebDocumentationPanelView
		view.text = text
		controller.documentChangeBegin()
		controller.documentChangeEnd()
		ctx.notifyChanged()
	}

	/** ---- Documentation of a sub-circuit */

	/** Whether the selection is a single sub-circuit, whose documentation [showSelected] can show. */
	fun canShowSelected(): Boolean = ctx.guarded(false) {
		val selection = ctx.editor.view.selectionManager.selection
		selection.size == 1 && selection.first() is SubGraphVerticeView<*>
	}

	/** Shows the documentation of the selected sub-circuit in the `documentation` dialog, or a message if it has none. */
	fun showSelected() = ctx.guarded(Unit) {
		val action = OpenDocumentationAction()
		try {
			if (action.enabled) action.execute(ActionEvent(null, this, 0, "openDocumentation", System.currentTimeMillis()))
		} finally {
			action.dispose()
		}
		ctx.notifyChanged()
	}

	/** Describes the documentation shown in the dialog as JSON: title (the sub-circuit's name) and text. */
	fun dialog(): String = ctx.guarded("null") {
		val doc = ctx.dialog()?.takeIf { it.kind == WebItemKinds.DOCUMENTATION }?.payload as? WebDocumentation
			?: return@guarded "null"
		val title = RichText.stripToPlainText(doc.title)
		val text = doc.document.text
		toJson(obj {
			this.title = title
			this.text = text
		})
	}

	/** Closes the documentation dialog, removing the sub-circuit's highlight like closing the desktop's view. */
	fun closeDialog() = ctx.guarded(Unit) {
		val item = ctx.dialog()?.takeIf { it.kind == WebItemKinds.DOCUMENTATION } ?: return@guarded
		ctx.frame().graphPanelViewController.desktopController.closeItem(item)
		ctx.notifyChanged()
	}
}
