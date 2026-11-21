package io.antarescircuit.antares.web

import io.antarescircuit.antares.AntaresApplication
import io.antarescircuit.antares.model.gate.LogicGateType
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType
import io.antarescircuit.antares.model.gate.UnaryLogicGateType
import io.antarescircuit.antares.view.container.DilCase
import io.antarescircuit.antares.view.gate.ChangeLogicGateTypeAction
import io.antarescircuit.antares.view.gate.GateMnemonic
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.draw.view.CanvasJs
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.draw.view.FocusDrawablePlayer
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.Tool
import io.antarescircuit.jabbah.edit.UndoableDataHolder
import io.antarescircuit.jabbah.edit.app.OneDownAction
import io.antarescircuit.jabbah.edit.app.OneUpAction
import io.antarescircuit.jabbah.edit.app.SelectNextAction
import io.antarescircuit.jabbah.edit.app.SelectPreviousAction
import io.antarescircuit.jabbah.edit.model.curve.CubicCurveComponent
import io.antarescircuit.jabbah.edit.model.curve.CubicCurveTool
import io.antarescircuit.jabbah.edit.model.curve.QuadCurveComponent
import io.antarescircuit.jabbah.edit.model.curve.QuadCurveTool
import io.antarescircuit.jabbah.edit.model.polyline.PolylineComponent
import io.antarescircuit.jabbah.edit.model.polyline.PolylineTool
import io.antarescircuit.jabbah.edit.model.rectangle.EllipseComponent
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleComponent
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleTool
import io.antarescircuit.jabbah.edit.model.text.ComponentAtLocationTool
import io.antarescircuit.jabbah.edit.model.text.LabelComponent
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.container.ContainerDrawing
import io.antarescircuit.jabbah.graph.container.editsubgraph.EditSubGraphVerticeViewCommand
import io.antarescircuit.jabbah.graph.library.AbstractContainerLibraryElementSavable
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.LibrarySavable
import io.antarescircuit.jabbah.graph.library.LibraryService
import io.antarescircuit.jabbah.graph.model.GraphPort
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.ui.GraphFrameController
import io.antarescircuit.jabbah.graph.ui.container.ContainerPanelController
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StorableCloner
import kotlinx.browser.window
import org.w3c.dom.HTMLCanvasElement

/**
 * Editing tools of the circuit editor, mirroring the desktop's drawing toolbars (`GraphPanelViewSwing`,
 * `ContainerToolBarBuilder`), the grid/snap toggles, gate type changes (`AntaresContextMenuProvider`),
 * selection and stacking order actions, extracting sub-circuits (`ExtractMetaGraphAction`), editing the symbol of
 * a single sub-circuit (`EditSubGraphVerticeViewAction`, `ResetSubGraphVerticeViewAction`), port renaming
 * (`GraphPortRenamingAction`), gate mnemonics (`GateMnemonicAction`) and embedding (`MetaGraphEmbedAction`).
 *
 * Tools and toggles apply to the editor shown: the circuit, the circuit's symbol, or the symbol of a single
 * sub-circuit while it is edited in a dialog (see [beginEditSymbol]).
 */
@JsExport
class WebTools internal constructor(private val ctx: WebEditorContext) {

	private val frame get() = ctx.frame()
	private val panel get() = frame.graphPanelViewController
	private val isSymbolView get() = frame.displayedView == GraphFrameController.DisplayedView.Container

	/** The symbol of a single sub-circuit being edited, if any. */
	private var symbolEdit: SymbolEdit? = null

	/** The editor the tools apply to. */
	private val activeEditor: Editor
		get() = symbolEdit?.controller?.editor ?: if (isSymbolView) frame.containerPanelController.editor else ctx.editor

	private val selectNextAction by lazy { SelectNextAction() }
	private val selectPreviousAction by lazy { SelectPreviousAction() }
	private val oneUpAction by lazy { OneUpAction() }
	private val oneDownAction by lazy { OneDownAction() }
	private val gateTypeActions = mutableMapOf<String, Action>()

	/** Tools finish on their own (returning to selection), so the tool strip must follow the editor's current tool. */
	private val toolListener = io.antarescircuit.jabbah.base.event.PropertyChangeListener<Any> { e ->
		if (e.name == Editor.PROP_CURRENT_TOOL) ctx.notifyChanged()
	}

	init {
		ctx.editor.addPropertyChangeListener(toolListener)
		frame.containerPanelController.editor.addPropertyChangeListener(toolListener)
	}

	private fun event(name: String) = ActionEvent(null, this, 0, name, System.currentTimeMillis())

	private fun circuitGraphView(): GraphView? = ctx.editor.view.drawing as? GraphView

	/** Whether the circuit (not a sub-circuit opened for viewing) is shown and can be edited. */
	private val circuitEditable: Boolean
		get() = symbolEdit == null && !isSymbolView && ctx.document() == null && ctx.editor.active && ctx.editor.view.editable &&
			!frame.applicationModeHolder.currentMode.isExecute() && ctx.dataController().data?.content is MetaGraph

	/** ---- Drawing tools */

	private val circuitTools: Map<String, Tool> by lazy {
		linkedMapOf(
			"select" to ctx.editor.selectionTool,
			"text" to panel.textTool,
			"rectangle" to panel.rectangleTool,
			"ellipse" to panel.ellipseTool,
			"polyline" to panel.polylineTool,
			"quadCurve" to panel.quadCurveTool,
			"cubicCurve" to panel.cubicCurveTool,
		)
	}

	/** The tools of symbol editors (`DigitalContainerToolBarBuilder`), per editor. */
	private val symbolTools = mutableMapOf<Editor, Map<String, Tool>>()

	private fun symbolToolsOf(editor: Editor): Map<String, Tool> = symbolTools.getOrPut(editor) {
		linkedMapOf(
			"select" to editor.selectionTool,
			"label" to ComponentAtLocationTool(editor, cursor = Cursor.TEXT, factory = { LabelComponent() }),
			"rectangle" to RectangleTool(editor, factory = { RectangleComponent() }),
			"ellipse" to RectangleTool(editor, factory = { EllipseComponent() }),
			"polyline" to PolylineTool(editor, factory = { PolylineComponent() }),
			"quadCurve" to QuadCurveTool(editor, factory = { QuadCurveComponent() }),
			"cubicCurve" to CubicCurveTool(editor, factory = { CubicCurveComponent() }),
			"dilCase" to ComponentAtLocationTool(editor, factory = { DilCase() }),
		)
	}

	private fun toolsOf(editor: Editor): Map<String, Tool> = if (editor === ctx.editor) circuitTools else symbolToolsOf(editor)

	/** Activates tool [id] (see [state]) in the editor shown. Tools return to selection after one use, like on desktop. */
	fun setTool(id: String) = ctx.guarded(Unit) {
		val editor = activeEditor
		if (!editor.active || !editor.view.editable) return@guarded
		val tool = toolsOf(editor)[id] ?: return@guarded
		editor.currentTool = tool
		ctx.notifyChanged()
	}

	/** ---- View options */

	/** Shows or hides the grid of the editor shown (`GridAction`). */
	fun toggleGrid() = ctx.guarded(Unit) {
		val view = activeEditor.view
		view.showGrid = !view.showGrid
		view.repaint()
		ctx.notifyChanged()
	}

	/** Toggles snapping to the grid (`GridSnapAction`). */
	fun toggleGridSnap() = ctx.guarded(Unit) {
		activeEditor.gridSnap = !activeEditor.gridSnap
		ctx.notifyChanged()
	}

	/** Toggles snapping to other components (`ComponentSnapAction`). */
	fun toggleComponentSnap() = ctx.guarded(Unit) {
		activeEditor.componentSnap = !activeEditor.componentSnap
		ctx.notifyChanged()
	}

	/** Toggles the switch representation drawn inside gates at high zoom levels (`GateMnemonicAction`). */
	fun toggleGateMnemonic() = ctx.guarded(Unit) {
		GateMnemonic.enabled = !GateMnemonic.enabled
		ctx.editor.view.repaint()
		ctx.notifyChanged()
	}

	/** ---- State */

	/**
	 * Describes the tools and actions for the editor shown as JSON: `target` (circuit|symbol|instance), `tools`
	 * (ids), `tool` (the current one), `toolsEnabled`, view options and which actions are available.
	 */
	fun state(): String = ctx.guarded("null") {
		val editor = activeEditor
		val tools = toolsOf(editor)
		val current = tools.entries.firstOrNull { it.value === editor.currentTool }?.key ?: "select"
		val target = when {
			symbolEdit != null -> "instance"
			isSymbolView -> "symbol"
			else -> "circuit"
		}
		val toolsEnabled = editor.active && editor.view.editable && !frame.applicationModeHolder.currentMode.isExecute()
		val gateTypes = gateTypes()
		val selected = singleSubCircuit()
		val canEditSymbol = circuitEditable && selected != null
		val canResetSymbol = canEditSymbol && selected!!.hasCustomizedContainerDrawing
		val canExtract = circuitEditable && ctx.editor.view.selectionManager.selection.isNotEmpty()
		val canRenamePorts = symbolEdit == null && !isSymbolView && ctx.document() == null && circuitGraphView() != null &&
			!frame.applicationModeHolder.currentMode.isExecute()
		val canEmbed = ctx.dataController().data?.savable is LibrarySavable
		val canSelect = !frame.applicationModeHolder.currentMode.isExecute() && ctx.document() == null
		val selectNext = canSelect && selectNextAction.enabled
		val selectPrevious = canSelect && selectPreviousAction.enabled
		val oneUp = canSelect && editor.view.editable && oneUpAction.enabled
		val oneDown = canSelect && editor.view.editable && oneDownAction.enabled
		toJson(obj {
			this.target = target
			this.tools = tools.keys.toList().toJsArray()
			this.tool = current
			this.toolsEnabled = toolsEnabled
			this.grid = editor.view.showGrid
			this.gridSnap = editor.gridSnap
			this.componentSnap = editor.componentSnap
			this.gateMnemonic = GateMnemonic.enabled
			this.gateTypes = gateTypes
			this.selectNext = selectNext
			this.selectPrevious = selectPrevious
			this.oneUp = oneUp
			this.oneDown = oneDown
			this.extract = canExtract
			this.editSymbol = canEditSymbol
			this.resetSymbol = canResetSymbol
			this.renamePorts = canRenamePorts
			this.embed = canEmbed
		})
	}

	/** ---- Selection and stacking order */

	/** Performs one of `selectNext`, `selectPrevious`, `oneUp`, `oneDown`. */
	fun perform(name: String) = ctx.guarded(Unit) {
		val action = when (name) {
			"selectNext" -> selectNextAction
			"selectPrevious" -> selectPreviousAction
			"oneUp" -> oneUpAction
			"oneDown" -> oneDownAction
			else -> return@guarded
		}
		if (action.enabled) action.execute(event(name))
		ctx.notifyChanged()
	}

	/** ---- Gate types */

	/**
	 * The gate types the selected gates can be changed to (all selected components must be gates with the same
	 * number of inputs kind, unary or not), like the desktop's context menu.
	 */
	private fun gateTypes(): Array<dynamic> {
		if (!circuitEditable) return emptyArray()
		val selection = ctx.editor.view.selectionManager.selection
		if (selection.isEmpty() || selection.any { it !is LogicGateView }) return emptyArray()
		val types = selection.map { (it as LogicGateView).model.gateType }
		val all: List<LogicGateType> = when {
			types.all { it is NonUnaryLogicGateType } -> NonUnaryLogicGateType.entries
			types.all { it is UnaryLogicGateType } -> UnaryLogicGateType.entries
			else -> return emptyArray()
		}
		val current = types.distinct().singleOrNull()
		return all.map { type ->
			val id = type.toString()
			val label = Translations.getString("${type.baseResourceKey}.name")
			val selected = type == current
			obj {
				this.id = id
				this.label = label
				this.current = selected
			}
		}.toTypedArray()
	}

	/** Changes the type of the selected gates to [id] (e.g. `Nand`) as an undoable command. */
	fun changeGateType(id: String) = ctx.guarded(Unit) {
		if (!circuitEditable) return@guarded
		val type: LogicGateType = NonUnaryLogicGateType.entries.firstOrNull { it.toString() == id }
			?: UnaryLogicGateType.entries.firstOrNull { it.toString() == id }
			?: return@guarded
		val action = gateTypeActions.getOrPut(id) { ChangeLogicGateTypeAction(type) }
		if (action.enabled) action.execute(event("changeGateType"))
		ctx.notifyChanged()
	}

	/** ---- Extract as sub-circuit */

	/**
	 * Returns why the selection can't be extracted, or an empty string if it can. The desktop shows the message
	 * of the graph type's import check.
	 */
	fun extractProblem(): String = ctx.guarded("") {
		val graph = circuitGraphView()?.graph ?: return@guarded ""
		graph.type.checkImport(graph.type) ?: ""
	}

	/**
	 * Replaces the selected components by a new sub-circuit [name] containing them, and saves the circuit.
	 * Like on desktop this can't be undone, so the undo history is cleared.
	 */
	fun extract(name: String) = ctx.guarded(Unit) {
		if (!circuitEditable) return@guarded
		val graphView = circuitGraphView() ?: return@guarded
		val text = TranslatableText(name.trim())
		MetaGraph.validateName(text)
		val savable = ctx.dataController().data?.savable as? AbstractContainerLibraryElementSavable ?: return@guarded
		val library = LibraryModule.libraryHolder.library
		ctx.saveNow()
		@Suppress("UNCHECKED_CAST")
		GraphViewModule.graphViewAppService.extractMetaGraph(
			text,
			graphView.graph!!.type,
			frame.graphPanelViewController.editViewController.drawingView,
			library.libraryService.getDirectoryOf(library, savable.item),
			ctx.dataController(),
		)
		ctx.editor.commandManager.reset()
		ctx.notifyChanged()
	}

	/** ---- Symbol of a single sub-circuit */

	private fun singleSubCircuit(): SubGraphVerticeView<*>? =
		ctx.editor.view.selectionManager.selection.singleOrNull() as? SubGraphVerticeView<*>

	private inner class SymbolEdit(
		val controller: ContainerPanelController,
		val verticeId: Int,
		val previousView: io.antarescircuit.jabbah.draw.view.ContentView<*>?,
	) : UndoableDataHolder {
		var canvas: CanvasJs? = null
		var checkpointOpen = false
		override fun getUndoableState(): Storable = controller.editor.drawing
		override fun setUndoableState(state: Storable) = controller.updateData(state as ContainerDrawing)
		override fun undoableStateEstablished(state: Storable) {}
	}

	/**
	 * Starts editing the symbol of the selected sub-circuit, which only changes how this one instance looks.
	 * Returns its name for the dialog title, or an empty string if editing isn't possible. Bind the dialog's canvas
	 * with [bindSymbolCanvas] and finish with [endEditSymbol].
	 */
	fun beginEditSymbol(): String = ctx.guarded("") {
		if (symbolEdit != null || !circuitEditable) return@guarded ""
		@Suppress("UNCHECKED_CAST")
		val vv = singleSubCircuit() as? SubGraphVerticeView<SubGraphVerticeRef> ?: return@guarded ""
		if (vv.model.isBroken) {
			ctx.message("info", "", Translations.getString("graph.element.brokenRef.cannotEdit.msg"))
			return@guarded ""
		}
		val drawingView = panel.editViewController.drawingView
		val controller = ContainerPanelController(
			frame.applicationContextHolder,
			displayGlobalMessages = false,
			drawingView,
			ctx.dataController(),
		)
		// Views of the controller's parts, like for the circuit's own symbol editor
		val passive = WebPassiveViews(ctx.notifyChanged)
		controller.view = WebContainerPanelView(ctx.notifyChanged)
		controller.propertyPanelController.view = passive
		controller.symbolComparatorController.view = passive
		controller.symbolComparatorController.libraryTreeViewController.view = WebLibraryViews {}
		controller.editor.preventDeletingPortViewComponents = true
		controller.editor.addPropertyChangeListener(toolListener)
		symbolEdit = SymbolEdit(controller, vv.id, DrawViewModule.viewManager.activeView)
		ctx.notifyChanged()
		val name = RichText.stripToPlainText(LibraryModule.libraryHolder.getMetaGraph(vv.subGraphVertice!!.graphUUID!!).name)
		name.ifBlank { Translations.getString("graph.action.editSubGraphVerticeView.name") }
	}

	/** Binds the canvas of the symbol dialog. */
	fun bindSymbolCanvas(canvas: HTMLCanvasElement) = ctx.guarded(Unit) {
		val edit = symbolEdit ?: return@guarded
		if (edit.canvas != null) return@guarded
		// The drawing can only be set once the view has been initialized by its canvas
		edit.canvas = CanvasJs(canvas, edit.controller.drawingView)
		@Suppress("UNCHECKED_CAST")
		val vv = panel.editViewController.drawingView.drawing.getWithId(edit.verticeId) as SubGraphVerticeView<SubGraphVerticeRef>
		val libraryGraph = LibraryModule.libraryHolder.getMetaGraph(vv.subGraphVertice!!.graphUUID!!)
		edit.controller.setData(
			graphView = libraryGraph.graph.graphView,
			containerDrawing = StorableCloner.clone(vv.getEditableContainerDrawing()),
			editable = true,
			isManualContainer = true,
		)
		// Deselect to get rid of the sub-circuit's selection model while its symbol is replaced
		panel.editViewController.drawingView.selectionManager.deselect(vv)
		vv.invalidate()
		ctx.editor.commandManager.openCheckpoint("subgraphContainerView", edit)
		edit.checkpointOpen = true
		canvas.addEventListener("mousedown", { activateSymbolEdit() })
		activateSymbolEdit()
		edit.controller.active = true
		window.setTimeout({ edit.controller.drawingView.navigator.fitMaxNormal() }, 0)
	}

	private fun activateSymbolEdit() {
		val edit = symbolEdit ?: return
		if (DrawViewModule.viewManager.activeView !== edit.controller.drawingView) {
			DrawViewModule.viewManager.activeView = edit.controller.drawingView
		}
	}

	/** Undoes or redoes ([redo]) the last change within the symbol dialog. */
	fun undoSymbol(redo: Boolean) = ctx.guarded(Unit) {
		if (symbolEdit == null) return@guarded
		val manager = ctx.editor.commandManager
		if (redo && manager.canRedo()) manager.redo() else if (!redo && manager.canUndo()) manager.undo()
		ctx.notifyChanged()
	}

	/** Describes undo/redo availability within the symbol dialog as JSON. */
	fun symbolHistory(): String = ctx.guarded("null") {
		if (symbolEdit == null) return@guarded "null"
		val manager = ctx.editor.commandManager
		val canUndo = manager.canUndo()
		val canRedo = manager.canRedo()
		toJson(obj {
			this.canUndo = canUndo
			this.canRedo = canRedo
		})
	}

	/** Finishes editing the symbol: applies it as one undoable command if [apply], discards it otherwise. */
	fun endEditSymbol(apply: Boolean) = ctx.guarded(Unit) {
		val edit = symbolEdit ?: return@guarded
		symbolEdit = null
		val manager = ctx.editor.commandManager
		if (edit.checkpointOpen) manager.closeCheckpoint()
		val drawingView = panel.editViewController.drawingView
		val vv = drawingView.drawing.getWithId(edit.verticeId)
		edit.controller.active = false
		if (apply && vv != null && edit.checkpointOpen) {
			manager.execute(EditSubGraphVerticeViewCommand(drawingView, edit.verticeId, edit.controller.editor.drawing as ContainerDrawing))
		}
		DrawViewModule.viewManager.activeView = edit.previousView
		drawingView.drawing.getWithId(edit.verticeId)?.let {
			it.invalidate()
			drawingView.selectionManager.select(it)
		}
		edit.canvas?.dispose()
		symbolTools.remove(edit.controller.editor)
		edit.controller.dispose()
		ctx.notifyChanged()
	}

	/** Resets the symbol of the selected sub-circuit to the one defined by the sub-circuit (`ResetSubGraphVerticeViewAction`). */
	fun resetSymbol() = ctx.guarded(Unit) {
		val vv = singleSubCircuit()?.takeIf { circuitEditable && it.hasCustomizedContainerDrawing } ?: return@guarded
		val drawingView = panel.editViewController.drawingView
		// Deselect/select to get rid of the invalid inner selection models
		drawingView.selectionManager.deselect(vv)
		ctx.editor.commandManager.execute(EditSubGraphVerticeViewCommand(drawingView, vv.id, null))
		drawingView.drawing.getWithId(vv.id)?.let { drawingView.selectionManager.select(it) }
		ctx.notifyChanged()
	}

	/** ---- Port renaming */

	/** Lists the circuit's inputs and outputs as JSON (id, name, type), sorted like the desktop's dialog. */
	fun ports(): String = ctx.guarded("[]") {
		val graphView = circuitGraphView() ?: return@guarded "[]"
		val ports = graphView.getGraphPortViews()
			.sortedWith(compareBy({ it.model.portType }, { it.model.name ?: "" }))
			.map { pv ->
				val id = pv.id
				val name = pv.model.name ?: ""
				val type = pv.model.portType.toString()
				val input = pv.model.portType.isInput && !pv.model.portType.isOutput
				obj {
					this.id = id
					this.name = name
					this.type = type
					this.input = input
				}
			}
		toJson(ports.toJsArray())
	}

	/** Whether the circuit's ports can be renamed (it is editable). */
	fun canEditPorts(): Boolean = ctx.guarded(false) { circuitEditable }

	/**
	 * Renames the port shown by view [id] to [name] as an undoable command (`GraphPortRenamingController`).
	 * Returns an error message if the name is invalid, otherwise an empty string.
	 */
	fun renamePort(id: Int, name: String): String = ctx.guarded("") {
		if (!circuitEditable) return@guarded ""
		val view = circuitGraphView()?.getWithId(id) ?: return@guarded ""
		val port = view.model as? GraphPort<*> ?: return@guarded ""
		val oldName = port.name
		if (oldName == name) return@guarded ""
		try {
			port.name = name
			ctx.editor.commandManager.register(io.antarescircuit.jabbah.graph.model.GraphPortNameCommand(ctx.editor, port.id, oldName, name))
			ctx.editor.view.repaint()
			ctx.notifyChanged()
			""
		} catch (e: Exception) {
			e.message ?: Translations.getString("base.error.txt")
		}
	}

	/** Selects the port shown by view [id] and, if [zoom], zooms to it. */
	fun focusPort(id: Int, zoom: Boolean) = ctx.guarded(Unit) {
		val view = ctx.editor.view
		val drawable = circuitGraphView()?.getWithId(id) ?: return@guarded
		view.selectionManager.deselectAll()
		view.selectionManager.select(drawable)
		if (zoom) FocusDrawablePlayer.playFocus(drawable, view)
		view.repaint()
		ctx.notifyChanged()
	}

	/** ---- Embedding */

	/** The names of the themes in which embedded circuits can be shown, as JSON. */
	fun embedThemes(): String = ctx.guarded("[]") {
		toJson(Themes.allThemes().asSequence().map { it.name }.toList().toJsArray())
	}

	/** Returns the HTML `<iframe>` showing the open circuit in the online viewer in theme [theme] (`MetaGraphEmbedPanel`). */
	fun embedCode(theme: String): String = ctx.guarded("") {
		val savable = ctx.dataController().data?.savable as? LibrarySavable ?: return@guarded ""
		val element = savable.element
		val library = element.library ?: return@guarded ""
		val name = RichText.stripToPlainText(element.name.value)
		val properties = io.antarescircuit.jabbah.base.module.BaseModule.properties
		val base = if (properties.contains(LibraryService.PROP_VIEWER_JS_URL)) properties.getString(LibraryService.PROP_VIEWER_JS_URL)
			else AntaresApplication.VIEWER_JS_PROD_URL
		val src = "$base?library=${library.uuid.id}&circuit=${element.uuid.id}&theme=${encodeURIComponent(theme)}"
		"""
			|<iframe
			|   style="border:1px solid gray;"
			|   title="$name"
			|   width="500px"
			|   height="500px"
			|   src="$src">
			|</iframe>
		""".trimMargin()
	}
}

private external fun encodeURIComponent(value: String): String
