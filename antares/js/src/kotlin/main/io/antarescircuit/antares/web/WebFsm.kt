package io.antarescircuit.antares.web

import io.antarescircuit.antares.model.fsm.FSMDrawing
import io.antarescircuit.antares.model.fsm.FSMException
import io.antarescircuit.antares.model.fsm.FSMLibraryItem
import io.antarescircuit.antares.model.fsm.FSMTruthTableCreator
import io.antarescircuit.antares.model.truthtable.TruthTableLibraryItem
import io.antarescircuit.antares.view.fsm.FSMPanelController
import io.antarescircuit.antares.view.fsm.FSMPanelView
import io.antarescircuit.jabbah.base.Status
import io.antarescircuit.jabbah.base.StatusType
import io.antarescircuit.jabbah.base.event.PropertyChangeListener
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.ZoomStrategy
import io.antarescircuit.jabbah.draw.view.CanvasJs
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.editor.EditorImpl
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.Name
import org.w3c.dom.HTMLCanvasElement

/**
 * Finite state machines: creates them and hosts the desktop's shared [FSMPanelController] on a canvas
 * of the web application while an FSM document is shown (see `FSMGraphDesktopItemSwing`).
 */
@JsExport
class WebFsm internal constructor(private val ctx: WebEditorContext) {

	/** The editor of the FSM document, present while its canvas is bound. */
	private var controller: FSMPanelController? = null
	private var canvasJs: CanvasJs? = null

	private val editor: Editor get() = controller!!.editor

	private val properties = WebProperties({ editor }, { editor }, { false })

	/** Reports tool and zoom changes, which don't post events. */
	private val changeListener = PropertyChangeListener<Any> { e ->
		if (e.name == Editor.PROP_CURRENT_TOOL || e.name == Editor.PROP_LOCK_TOOL || e.name == View.PROP_TRANSFORMATION) {
			ctx.notifyChanged()
		}
	}

	private val openItem: FSMLibraryItem? get() = ctx.document()?.payload as? FSMLibraryItem

	/** Creates a new state machine in the open project and opens it. Returns its UUID, or "" on failure. */
	fun create(name: String): String = ctx.guarded("") {
		val project = ctx.project ?: return@guarded ""
		val item = FSMLibraryItem(TranslatableText(name))
		project.libraryService.addLibraryItem(project, item, project)
		ctx.openItem(item.uuid.id)
		item.uuid.id
	}

	/** ---- Canvas */

	/** Shows the open FSM document on [canvas] and makes it the active view. Call [unbind] when the canvas is removed. */
	fun bindCanvas(canvas: HTMLCanvasElement) = ctx.guarded(Unit) {
		unbind()
		val item = openItem ?: return@guarded
		val drawing = ctx.dataController().data?.content as? FSMDrawing ?: return@guarded
		val c = FSMPanelController(item)
		c.view = WebFsmPanelView
		// Like on the desktop, the canvas must exist before the drawing is set
		canvasJs = CanvasJs(canvas, c.drawingView)
		c.setDrawing(drawing)
		controller = c
		c.editor.addPropertyChangeListener(changeListener)
		c.drawingView.addPropertyChangeListener(changeListener)
		canvas.addEventListener("focus", { activate() })
		canvas.addEventListener("mousedown", { activate() })
		activate()
		c.drawingView.zoomStrategy = ZoomStrategy.NORMAL
		ctx.notifyChanged()
	}

	/** Releases the FSM editor, e.g. when the document is closed. */
	fun unbind() = ctx.guarded(Unit) {
		canvasJs?.dispose()
		canvasJs = null
		val c = controller ?: return@guarded
		controller = null
		c.editor.removePropertyChangeListener(changeListener)
		c.drawingView.removePropertyChangeListener(changeListener)
		if (DrawViewModule.viewManager.activeView === c.drawingView) DrawViewModule.viewManager.activeView = null
		c.editor.active = false
		c.dispose()
		ctx.notifyChanged()
	}

	/** Makes the FSM view the target of edit actions (undo, delete, select all, zoom, …). */
	private fun activate() {
		val view = controller?.drawingView ?: return
		if (DrawViewModule.viewManager.activeView !== view) DrawViewModule.viewManager.activeView = view
	}

	/** ---- State */

	/** Returns the state of the FSM editor as JSON, or `null` if no FSM is shown. */
	fun state(): String = ctx.guarded("null") {
		val c = controller ?: return@guarded "null"
		val drawing = c.drawingView.drawing
		val tool = when (c.editor.currentTool) {
			c.stateTool -> "state"
			c.transitionTool -> "transition"
			else -> "select"
		}
		val status = if (tool == "select") null else Status[StatusType.Tool]
		toJson(obj {
			this.tool = tool
			this.toolLock = c.editor.toolLock
			this.status = status
			this.zoom = c.drawingView.zoomFactor
			this.states = drawing.states.size
			this.transitions = drawing.transitions.size
			this.selectionCount = c.drawingView.selectionManager.selectionCount
			this.editable = c.drawingView.editable
		})
	}

	/** ---- Tools */

	/** Selects the tool `select`, `state` (click to add states) or `transition` (drag from state to state). */
	fun setTool(name: String) = ctx.guarded(Unit) {
		val c = controller ?: return@guarded
		if (!c.drawingView.editable) return@guarded
		c.editor.currentTool = when (name) {
			"state" -> c.stateTool
			"transition" -> c.transitionTool
			else -> c.editor.selectionTool
		}
		activate()
		c.drawingView.canvas.requestViewFocus()
	}

	/** Keeps the selected tool after use instead of returning to the selection tool. */
	fun setToolLock(lock: Boolean) = ctx.guarded(Unit) {
		(controller?.editor as? EditorImpl)?.toolLock = lock
	}

	/** ---- Zoom */

	fun zoomBy(factor: Double) = ctx.guarded(Unit) {
		val view = controller?.drawingView ?: return@guarded
		if (view.navigator.isZoomFactorInValidRange(view.zoomFactor * factor)) {
			view.navigator.multiplyZoomFactor(factor)
			view.zoomStrategy = ZoomStrategy.NONE
		}
	}

	fun zoomToFit() = ctx.guarded(Unit) {
		controller?.drawingView?.zoomStrategy = ZoomStrategy.FIT_MAX_NORMAL
	}

	fun zoomToNormal() = ctx.guarded(Unit) {
		controller?.drawingView?.zoomStrategy = ZoomStrategy.NORMAL
	}

	/** ---- Properties (FSMStateBeanInfo, FSMTransitionBeanInfo, FSMDrawingBeanInfo) */

	/** Returns the properties of the selected states/transitions, or of the FSM itself, as JSON. */
	fun properties(): String = ctx.guarded("null") {
		if (controller == null) "null" else properties.describe()
	}

	/** Sets property [key] to [value] (a JSON value) as an undoable command. */
	fun setProperty(key: String, value: String) = ctx.guarded(Unit) {
		if (controller == null) return@guarded
		properties.set(key, JSON.parse(value))
		ctx.notifyChanged()
	}

	/** ---- Truth table (FSMPanelController.createTruthTable) */

	/** Validates the FSM for creating a truth table. Returns the error message, or "" if it is valid. */
	fun truthTableError(): String = ctx.guarded("") {
		val drawing = controller?.drawingView?.drawing ?: return@guarded ""
		try {
			FSMTruthTableCreator(drawing).create()
			""
		} catch (e: FSMException) {
			e.message ?: ""
		}
	}

	/** Creates a truth table named [name] from the FSM next to it and opens it. Returns its UUID, or "" on failure. */
	fun createTruthTable(name: String): String = ctx.guarded("") {
		val c = controller ?: return@guarded ""
		val item = openItem ?: return@guarded ""
		val library = item.library ?: return@guarded ""
		val truthTable = try {
			FSMTruthTableCreator(c.drawingView.drawing).create()
		} catch (e: FSMException) {
			ctx.message("error", "Create truth table", e.message ?: "")
			return@guarded ""
		}
		truthTable.name = Name(name)
		val truthTableItem = TruthTableLibraryItem(truthTable)
		val directory = library.libraryService.getDirectoryOf(library, item)
		library.libraryService.addLibraryItem(library, truthTableItem, directory)
		ctx.openItem(truthTableItem.uuid.id)
		truthTableItem.uuid.id
	}
}

/** Questions are asked by the web application before calling [WebFsm.createTruthTable], so this view is passive. */
private object WebFsmPanelView : FSMPanelView {
	override fun askForTruthTableName(actionName: String, truthTableName: String): String? = null
	override fun showValidationError(actionName: String, msg: String) {}
	override fun dispose() {}
}
