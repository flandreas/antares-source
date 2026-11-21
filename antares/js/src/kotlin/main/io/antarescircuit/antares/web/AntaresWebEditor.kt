package io.antarescircuit.antares.web

import io.antarescircuit.antares.module.AntaresModuleJs
import io.antarescircuit.antares.view.theme.AntaresThemes
import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.jabbah.app.ApplicationVersion
import io.antarescircuit.jabbah.app.BeforeSaveEvent
import io.antarescircuit.jabbah.app.CurrentApplicationVersion
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.DataLocation
import io.antarescircuit.jabbah.base.LogLevel
import io.antarescircuit.jabbah.base.LogSystem
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventBusImpl
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.module.BaseModuleJs
import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.base.ui.Clipboard
import io.antarescircuit.jabbah.draw.ZoomStrategy
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.draw.view.CanvasJs
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.CommandEvent
import io.antarescircuit.jabbah.edit.CommandEventType
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.SnapResult
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.edit.app.*
import io.antarescircuit.jabbah.edit.auth.DesktopUser
import io.antarescircuit.jabbah.edit.auth.DesktopUserHolder
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import io.antarescircuit.jabbah.edit.auth.UserIdentity
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.execution.scheduler.SchedulerImpl
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.library.dictionary.LibraryDictionaryService
import io.antarescircuit.jabbah.graph.model.GraphType
import io.antarescircuit.jabbah.graph.project.Project
import io.antarescircuit.jabbah.graph.project.ProjectModule
import io.antarescircuit.jabbah.graph.ui.GraphDataViewController
import io.antarescircuit.jabbah.graph.ui.GraphFrameController
import io.antarescircuit.jabbah.graph.ui.NavigationStackEntry
import io.antarescircuit.jabbah.graph.ui.library.OpenContainerLibraryElementAction
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.graph.GraphViewCopyPasteService
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.io.DomXmlReader
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StorableCloner
import io.antarescircuit.jabbah.io.StoreXmlReader
import kotlinx.browser.window
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.Promise

/**
 * The bridge between the Antares editor core and the web editor UI.
 *
 * Builds the same controller structure as the desktop application (data, frame, panel and edit controllers)
 * with browser views, and exposes a small API to the web application. Structured results are returned as JSON.
 * The web application is notified about changes through [onChange] and re-reads [state] etc. as needed.
 *
 * @param store synchronous file storage for system libraries (`system/...`) and user projects (`projects/...`)
 * @param dataUrl base URL of static data such as translations
 * @param onChange called (debounced) whenever the editor state might have changed
 * @param onMessage called for messages to be shown to the user: (type, title, text)
 * @param onConfirm asks the user a yes/no question and calls `answer` with the result
 */
@JsExport
class AntaresWebEditor(
	private val store: WebFileStore,
	private val dataUrl: String,
	private val onChange: () -> Unit,
	private val onMessage: (type: String, title: String, text: String) -> Unit,
	onConfirm: (question: String, answer: (Boolean) -> Unit) -> Unit,
) {
	init {
		WebPrompts.confirm = { question, answer ->
			onConfirm(question) { yes ->
				guarded(Unit) { answer(yes) }
				notifyChanged()
			}
		}
	}

	companion object {
		private val LOG by logger(AntaresWebEditor::class)

		private val CODE_VERSION = ApplicationVersion("2.5.0")
		private val DATA_VERSION = ApplicationVersion("2.4.0")

		/** Command events that change the open document. */
		private val DOCUMENT_CHANGES = setOf(CommandEventType.COMMIT_TRANSACTION, CommandEventType.UNDO, CommandEventType.REDO)

		/** The local user owning all projects created in the browser. */
		private val LOCAL_USER = UserIdentity.ANYBODY

		private const val MIN_NOTIFY_INTERVAL = 100.0

		const val LIGHT_THEME = "Winter"
		const val DARK_THEME = "Darcula"
	}

	private lateinit var dataController: GraphDataViewController
	private lateinit var frame: io.antarescircuit.antares.view.AntaresFrameController
	private lateinit var editView: WebGraphEditView
	private lateinit var library: WebLibrary
	private lateinit var properties: WebProperties
	private val actions = mutableMapOf<String, Action>()

	/** The document shown instead of the circuit editor, e.g. a truth table; `null` while editing a circuit. */
	var document: WebDocumentItem? = null
		private set

	/** An additional document shown in a dialog, e.g. memory contents or the documentation of a sub-circuit. */
	var dialog: WebDocumentItem? = null
		private set

	private fun showMainItem(item: io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItem?) {
		document = item as? WebDocumentItem
		if (document != null && mainCanvas != null) {
			editor.active = false
			DrawViewModule.viewManager.activeView = null
		}
		notifyChanged()
	}

	private fun showChildItem(item: io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItem?) {
		dialog = item as? WebDocumentItem
		notifyChanged()
	}

	/** Sub-circuits opened in their own views next to the circuit. */
	private val sideViews = mutableListOf<WebSideView>()
	private val sideCanvases = mutableMapOf<Int, CanvasJs>()

	private fun updateSideViews(shown: io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItem?, closed: io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItem?) {
		when {
			shown is WebSideView -> sideViews.add(shown)
			closed is WebSideView -> sideViews.remove(closed)
			shown == null && closed == null -> sideViews.clear()
		}
		val open = sideViews.map { it.id }.toSet()
		sideCanvases.filterKeys { it !in open }.forEach { (id, canvas) ->
			canvas.dispose()
			sideCanvases.remove(id)
		}
		notifyChanged()
	}

	/** Binds the canvas of side view [id] (see `state().sideViews`). */
	fun bindSideCanvas(id: Int, canvas: HTMLCanvasElement) = guarded(Unit) {
		val view = sideViews.firstOrNull { it.id == id } ?: return@guarded
		if (sideCanvases.containsKey(id)) return@guarded
		sideCanvases[id] = CanvasJs(canvas, view.drawingView)
		canvas.addEventListener("mousedown", { DrawViewModule.viewManager.activeView = view })
	}

	/** Closes side view [id], like the close button of a desktop item. */
	fun closeSideView(id: Int) = guarded(Unit) {
		val view = sideViews.firstOrNull { it.id == id } ?: return@guarded
		if (DrawViewModule.viewManager.activeView === view) DrawViewModule.viewManager.activeView = editView
		BaseModule.eventBus.post(view.createCloseRequest())
	}

	/** Opens the selected sub-circuit in its own view next to the circuit. */
	fun openInSideView() = guarded(Unit) {
		val selected = editor.view.selectionManager.selection.singleOrNull() as? io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView<*>
			?: return@guarded
		BaseModule.eventBus.post(io.antarescircuit.jabbah.graph.view.vertice.OpenSubGraphRequest(selected, newView = true, quickMode = false))
	}

	/** Closes the document shown in a dialog. */
	fun closeDialog() {
		dialog = null
		notifyChanged()
	}

	/** The open library item, if it isn't a circuit. */
	private val openLibraryItem: LibraryItem?
		get() = (dataController.data?.savable as? AbstractLibraryItemSavable)?.item?.takeIf { it !is ContainerLibraryElement }

	private var mainCanvas: CanvasJs? = null
	private var containerCanvas: CanvasJs? = null

	private var notifyScheduled = false
	private var autosaveTimer: Int? = null
	private var saving = false

	/**
	 * Whether a command changed the open document since it was last saved. The command history alone can't tell:
	 * undoing back to the opened state leaves nothing to undo, but differs from what autosave stored.
	 */
	private var unsaved = false

	private val editor get() = frame.editor
	private val modeHolder get() = frame.applicationModeHolder
	private val scheduler get() = frame.applicationContextHolder.scheduler
	private val navigationStack get() = frame.graphPanelViewController.editViewController.graphNavigationViewController.navigationStackViewController.navigationStack

	private var lastNotify = 0.0

	/**
	 * Schedules a single change notification to the web application for all changes within one task,
	 * while simulating at most every [MIN_NOTIFY_INTERVAL] ms, because simulations post events continuously.
	 */
	private fun notifyChanged() {
		if (!notifyScheduled) {
			notifyScheduled = true
			val simulating = ::frame.isInitialized && modeHolder.currentMode.isExecute()
			val delay = if (simulating) (lastNotify + MIN_NOTIFY_INTERVAL - kotlin.js.Date.now()).coerceAtLeast(0.0) else 0.0
			window.setTimeout({
				notifyScheduled = false
				lastNotify = kotlin.js.Date.now()
				onChange()
			}, delay.toInt())
		}
	}

	/**
	 * Initializes all modules and controllers. Translations are loaded from [dataUrl] first.
	 * The returned [Promise] resolves when the editor is ready to open a project.
	 */
	fun start(dark: Boolean): Promise<Unit> {
		BaseModule.eventBus = NotifyingEventBus(EventBusImpl()) { event -> handleEvent(event) }
		BaseModuleJs.translationService = StaticTranslationServiceJs(dataUrl)
		BaseModuleJs.require()
		BaseModule.properties.set(DataLocation.PROP_SERVER_URL, dataUrl)

		// Like Swing's invokeLater, run deferred work after the current event has been processed
		System.invoker = { runnable ->
			window.setTimeout({
				try {
					runnable()
				} catch (t: Throwable) {
					reportError(t)
				}
			}, 0)
		}

		// Later bundles override keys of earlier ones (e.g. "antares" refines "jabbah-graph"), so they are
		// fetched in parallel but applied in this order
		val bundles = listOf("jabbah-base", "jabbah-draw", "jabbah-edit", "jabbah-execution", "jabbah-app", "jabbah-graph", "antares")
		val service = BaseModuleJs.translationService
		bundles.forEach { service.load(it) }
		return bundles.fold(Promise.resolve(Unit)) { previous, bundle ->
			previous.then { Translations.addBundleAsync(bundle) ?: Promise.resolve(Unit) }.then { }
		}.then {
			initialize(dark)
		}
	}

	private fun initialize(dark: Boolean) {
		EditAuthModule.require()
		EditAuthModule.userHolder = DesktopUserHolder(DesktopUser(LOCAL_USER, "", false))

		// Must be set before the view modules create the services that capture it
		EditModule.copyPasteService = GraphViewCopyPasteService()

		AntaresModuleJs.persistence = WebPersistence(store)
		AntaresModuleJs.require()
		BaseModule.settings.set(SchedulerImpl.SETTING_ENABLE_SOFT_BREAKPOINTS, true)

		// Defaults that the desktop's UI modules define
		BaseModule.properties.set(GraphFrameController.PROP_AUTO_SWITCH, false)
		BaseModule.properties.set(io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopView.PROP_DOCKING, false)
		BaseModule.properties.set(io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopView.PROP_ROWS_PER_COLUMN, 2)
		BaseModule.properties.set(
			io.antarescircuit.jabbah.graph.container.ContainerDrawingLayouter.PROP_CONTAINER_DRAWING_LAYOUTER,
			io.antarescircuit.jabbah.graph.container.ContainerDrawingLayouter.Narrow.customName
		)

		CurrentApplicationVersion.codeVersion = CODE_VERSION
		CurrentApplicationVersion.dataVersion = DATA_VERSION
		LogSystem.level = LogLevel.Info

		AntaresThemes.install(if (dark) DARK_THEME else LIGHT_THEME)

		// Texts used only by the web editor
		Translations.addKey("web.properties.multiple", "{0} components")
		Translations.addKey("web.import.invalid", "The file is not an exported Antares project")

		dataController = GraphDataViewController()
		dataController.view = WebApplicationDataView(::notifyChanged, onMessage)

		frame = io.antarescircuit.antares.view.AntaresFrameController(dataController)
		val panel = frame.graphPanelViewController
		val editViewController = panel.editViewController
		val navigationController = editViewController.graphNavigationViewController

		editView = WebGraphEditView(
			{ navigationController.navigationStackViewController },
			{ editViewController.drawingView },
			::notifyChanged
		)
		navigationController.navigationStackViewController.view = editView
		navigationController.navigationStackViewController.navigationStack.rootEntry = rootEntryFor(editView)
		navigationController.view = editView
		editViewController.view = editView
		val passive = WebPassiveViews(::notifyChanged)
		editViewController.scenarioViewController.view = WebScenarioView()
		editViewController.scenarioViewController.propertyPanelController.view = passive
		editViewController.usecaseViewController.view = WebUsecaseView()
		editViewController.usecaseViewController.propertyPanelController.view = passive

		val libraryViews = WebLibraryViews(::notifyChanged)
		panel.propertyPanelController.view = passive
		panel.libraryPanelController.libraryTreeViewController.view = libraryViews
		panel.libraryPanelController.libraryTreePanelController.view = libraryViews
		panel.libraryPanelController.view = libraryViews
		panel.desktopController.view = WebGraphDesktopView(::showMainItem, ::showChildItem, { vv, color, detached ->
			WebSideView.create(vv, color, detached, frame.applicationContextHolder, ::notifyChanged)
		}, ::updateSideViews)
		panel.issuesViewController.view = passive
		panel.logViewController.view = passive
		panel.graphHierarchyController.view = passive
		panel.view = WebGraphPanelView(editView, ::notifyChanged)

		frame.containerPanelController.view = WebContainerPanelView(::notifyChanged)
		frame.containerPanelController.propertyPanelController.view = passive
		frame.containerPanelController.symbolComparatorController.view = passive
		frame.containerPanelController.symbolComparatorController.libraryTreeViewController.view = WebLibraryViews {}
		frame.documentationPanelController.view = WebDocumentationPanelView(frame.documentationPanelController, ::notifyChanged)

		// Opens circuits requested via OpenContainerLibraryElementRequest
		OpenContainerLibraryElementAction(dataController, panel.libraryPanelController.libraryTreeViewController)

		library = WebLibrary(store, LOCAL_USER)
		properties = WebProperties({ editor }, { frame.containerPanelController.editor }, { isSymbolView })

		createActions()

		frame.view = WebAntaresFrame({ frame }, ::handleDisplayedViewChanged) { showChildItem(WebDocumentItem(WebItemKinds.MEMORY_CONTENTS, it)) }

		// Zoom doesn't post events, but the web UI displays the zoom factor. Panning alone needs no refresh.
		val symbolView = frame.containerPanelController.drawingView
		var lastZooms = editor.view.zoomFactor to symbolView.zoomFactor
		val viewListener = io.antarescircuit.jabbah.base.event.PropertyChangeListener<Any> { e ->
			if (e.name == io.antarescircuit.jabbah.draw.View.PROP_TRANSFORMATION) {
				val zooms = editor.view.zoomFactor to symbolView.zoomFactor
				if (zooms != lastZooms) {
					lastZooms = zooms
					notifyChanged()
				}
			}
		}
		editor.view.addPropertyChangeListener(viewListener)
		symbolView.addPropertyChangeListener(viewListener)
	}

	private fun createActions() {
		actions["undo"] = UndoAction()
		actions["redo"] = RedoAction()
		actions["cut"] = CutAction()
		actions["copy"] = CopyAction()
		actions["paste"] = PasteAction()
		actions["duplicate"] = DuplicateAction()
		actions["delete"] = DeleteAction()
		actions["selectAll"] = SelectAllAction()
		actions["rotateLeft"] = RotateAction(clockwise = false)
		actions["rotateRight"] = RotateAction(clockwise = true)
		actions["toFront"] = ToFrontAction()
		actions["toBack"] = ToBackAction()
		actions["group"] = GroupComponentsAction()
		actions["ungroup"] = UngroupComponentsAction()
	}

	private fun reportError(t: Throwable) {
		LOG.error("Unexpected error: ${t.message}")
		console.error(t.stackTraceToString())
		onMessage("error", Translations.getString("base.error.txt"), t.message ?: t.toString())
	}

	/** Runs [block], reporting any exception to the user instead of propagating it to the web application. */
	private fun <T> guarded(fallback: T, block: () -> T): T =
		try {
			block()
		} catch (t: Throwable) {
			reportError(t)
			fallback
		}

	/** ---- Events */

	private fun handleEvent(event: Any) {
		if (event is CommandEvent && event.type in DOCUMENT_CHANGES) {
			unsaved = true
			scheduleAutosave()
		}
		if (::library.isInitialized) library.handleEvent(event)
		notifyChanged()
	}

	/** ---- Feature modules */

	private val context by lazy {
		WebEditorContext(
			dataController = { dataController },
			frame = { frame },
			library = { library },
			notifyChanged = ::notifyChanged,
			message = onMessage,
			saveNow = { saveNow() },
			openItem = { openItem(it) },
			document = { document },
			dialog = { dialog },
			guardedAny = { fallback, block -> guarded(fallback, block) },
		)
	}

	/** Memory contents (RAM/ROM/lookup table) and memory library items. */
	val memory: WebMemory by lazy { WebMemory(context) }

	/** Truth tables, boolean expressions, circuit analysis and synthesis. */
	val truthTables: WebTruthTables by lazy { WebTruthTables(context) }

	/** Finite state machines. */
	val fsm: WebFsm by lazy { WebFsm(context) }

	/** Test cases, scenarios and use cases. */
	val testcases: WebTestcases by lazy { WebTestcases(context) }

	/** HDL export, circuit parameters and images. */
	val hdl: WebHdl by lazy { WebHdl(context) }

	/** Images of the project. */
	val images: WebImages by lazy { WebImages(context) }

	/** Documentation of circuits and sub-circuits. */
	val docs: WebDocs by lazy { WebDocs(context) }

	/** Find in the circuit. */
	val find: WebFind by lazy { WebFind(context) }

	/** Unconnected ports, issues, log, statistics, propagation delay, global tunnels and hierarchy. */
	val analysis: WebAnalysis by lazy { WebAnalysis(context) }

	/** Drawing tools, grid and snap, gate types, sub-circuit extraction and symbols, port renaming. */
	val tools: WebTools by lazy { WebTools(context) }

	/** Project tree (folders, moving items), project/library properties and user libraries. */
	val libraryManager: WebLibraries by lazy { WebLibraries(context) }

	/** The AI circuit assistant. */
	val ai: WebAi by lazy { WebAi(context) }

	/** Help pages of the user manual. */
	val help: WebHelp by lazy { WebHelp(context) }

	/** Posters of the project's circuits. */
	val poster: WebPoster by lazy { WebPoster(context) }

	/** Simulation options and view toggles. */
	val simulation: WebSimulation by lazy { WebSimulation(context) }

	/** SVG, raster image, VCD and circuit bundle export. */
	val exporter: WebExport by lazy { WebExport(context) }

	/** ---- Canvas binding */

	/** Binds the canvas displaying the circuit. */
	fun bindCanvas(canvas: HTMLCanvasElement) {
		mainCanvas = CanvasJs(canvas, editor.view)
		canvas.addEventListener("focus", { activateMainView() })
		canvas.addEventListener("mousedown", { activateMainView() })
		library.bindDropTarget(mainCanvas!!, { editor }, ::canPlace)
		DrawViewModule.viewManager.activeView = editView
	}

	/** Binds the canvas displaying the symbol (container) of the circuit. */
	fun bindSymbolCanvas(canvas: HTMLCanvasElement) {
		containerCanvas = CanvasJs(canvas, frame.containerPanelController.drawingView)
	}

	private fun activateMainView() {
		if (!isSymbolView && DrawViewModule.viewManager.activeView !== editView) {
			DrawViewModule.viewManager.activeView = editView
		}
	}

	private val isSymbolView get() = frame.displayedView == GraphFrameController.DisplayedView.Container

	private fun handleDisplayedViewChanged() {
		if (mainCanvas == null) return
		val symbol = isSymbolView
		editor.active = !symbol && dataController.data != null && !modeHolder.currentMode.isExecute()
		frame.containerPanelController.active = symbol
		DrawViewModule.viewManager.activeView = if (symbol) frame.containerPanelController.drawingView else editView
		modeHolder.updateEditorEditability()
		notifyChanged()
	}

	/** Shows either the circuit (`false`) or its symbol (`true`). */
	fun showSymbol(symbol: Boolean) = guarded(Unit) {
		if (symbol) frame.viewContainerAction.execute(actionEvent("symbol"))
		else frame.viewDesktopAction.execute(actionEvent("circuit"))
	}

	/** ---- State */

	/** Returns the current editor state as JSON. */
	fun state(): String = guarded("{}") {
		val data = dataController.data
		val metaGraph = data?.content as? MetaGraph
		val savable = data?.savable as? LibrarySavable
		val project = LibraryModule.libraryHolder.l
		val projectKind = if (project is Project) "project" else "library"
		val selection = editor.view.selectionManager.selection
		val commandManager = editor.commandManager
		val entries = mutableListOf<NavigationStackEntry<GraphView>>()
		navigationStack.iterator().forEach { entries.add(it) }

		toJson(obj {
			this.mode = if (modeHolder.currentMode.isExecute()) "simulate" else "edit"
			this.paused = scheduler.systemSpeedCategory.systemSpeed.isPaused
			this.speed = frame.applicationContextHolder.currentSystemSpeedCategory.systemSpeed.speed
			this.speedCategory = frame.applicationContextHolder.currentSystemSpeedCategory.systemSpeedCategory.toString()
			this.view = if (isSymbolView) "symbol" else "circuit"
			this.canUndo = commandManager.canUndo()
			this.canRedo = commandManager.canRedo()
			this.undoLabel = commandManager.getUndoDescription()
			this.redoLabel = commandManager.getRedoDescription()
			this.saving = saving || autosaveTimer != null
			this.project = project?.let { p -> obj { uuid = p.uuid.id; name = p.name.value; kind = projectKind } }
			this.circuit = if (metaGraph != null && savable != null) obj {
				uuid = metaGraph.uuid.id
				name = RichText.stripToPlainText(metaGraph.name)
				type = metaGraph.graph.model?.type?.toString()
				editable = savable.editable
			} else null
			this.breadcrumbs = entries.map { it.name }.toJsArray()
			this.editable = editView.editable && entries.size <= 1
			this.selectionCount = selection.size
			this.componentCount = editor.view.drawing.drawables.size
			this.zoom = (if (isSymbolView) frame.containerPanelController.drawingView else editor.view).zoomFactor
			this.actions = obj {
				for ((name, action) in actions) {
					this[name] = action.enabled
				}
			}
			this.issues = frame.graphPanelViewController.view.issuesSummary?.issuesCount ?: 0
			this.sideViews = sideViews.map { v ->
				val title = v.title
				val color = v.contextColor?.foregroundColor?.let { c -> "rgb(${c.red},${c.green},${c.blue})" }
				obj {
					this.id = v.id
					this.title = title
					this.color = color
				}
			}.toJsArray()
			this.canOpenInSideView = editor.view.selectionManager.selection.singleOrNull() is io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView<*>
			this.document = documentState()
			this.dialog = dialog?.let { d -> obj { this.kind = d.kind } }
		})
	}

	private fun documentState(): dynamic {
		val item = openLibraryItem ?: return null
		val doc = document ?: return null
		val kind = doc.kind
		val uuid = WebItemKinds.uuidOf(item)
		val name = RichText.stripToPlainText(item.name.value)
		val editable = (dataController.data?.savable?.editable ?: false) && !modeHolder.currentMode.isExecute()
		return obj {
			this.kind = kind
			this.uuid = uuid
			this.name = name
			this.editable = editable
		}
	}

	/** ---- Edit actions */

	private fun actionEvent(name: String) = ActionEvent(null, this, 0, name, System.currentTimeMillis())

	/** Executes an edit action by name, see [createActions]. Returns `false` if the action is unknown or disabled. */
	fun perform(name: String): Boolean = guarded(false) {
		val action = actions[name] ?: return@guarded false
		if (!action.enabled) return@guarded false
		action.execute(actionEvent(name))
		true
	}

	/**
	 * Undoes the last command. Unlike the shared undo action this also works for documents (truth tables, …),
	 * which have no active drawing view.
	 */
	fun undo() = guarded(Unit) {
		if (document == null) perform("undo") else if (editor.commandManager.canUndo()) editor.commandManager.undo()
		notifyChanged()
	}

	/** Redoes the last undone command, see [undo]. */
	fun redo() = guarded(Unit) {
		if (document == null) perform("redo") else if (editor.commandManager.canRedo()) editor.commandManager.redo()
		notifyChanged()
	}

	/** Makes [text] (e.g. from a browser paste event) the content to be pasted and pastes it. */
	fun pasteText(text: String): Boolean {
		Clipboard.setLocalContents(text)
		return perform("paste")
	}

	fun zoomIn() = zoomBy(1.25)

	fun zoomOut() = zoomBy(0.8)

	private fun zoomBy(factor: Double) = guarded(Unit) {
		val view = DrawViewModule.viewManager.activeView?.view ?: editor.view
		if (view.navigator.isZoomFactorInValidRange(view.zoomFactor * factor)) {
			view.navigator.multiplyZoomFactor(factor)
			view.zoomStrategy = ZoomStrategy.NONE
		}
	}

	private val activeView get() = DrawViewModule.viewManager.activeView?.view ?: editor.view

	/**
	 * Zooms the active view by [factor] around the point ([x], [y]) given in CSS pixels relative to the canvas,
	 * e.g. for pinch gestures. The factor is limited to the allowed zoom range.
	 */
	fun zoomAt(factor: Double, x: Double, y: Double) = guarded(Unit) {
		val view = activeView
		val target = view.zoomFactor * factor
		if (!view.navigator.isZoomFactorInValidRange(target)) return@guarded
		val ratio = window.devicePixelRatio
		view.zoomStrategy = ZoomStrategy.NONE
		view.navigator.multiplyZoomFactor(factor, Point2D(x * ratio, y * ratio))
	}

	/** Pans the active view by ([dx], [dy]) CSS pixels. */
	fun panBy(dx: Double, dy: Double) = guarded(Unit) {
		val view = activeView
		val ratio = window.devicePixelRatio
		view.zoomStrategy = ZoomStrategy.NONE
		view.navigator.panBy((dx * ratio).toInt(), (dy * ratio).toInt())
	}

	fun zoomToFit() = guarded(Unit) {
		(DrawViewModule.viewManager.activeView?.view ?: editor.view).zoomStrategy = ZoomStrategy.FIT_MAX_NORMAL
	}

	fun zoomToNormal() = guarded(Unit) {
		(DrawViewModule.viewManager.activeView?.view ?: editor.view).zoomStrategy = ZoomStrategy.NORMAL
	}

	/** Navigates back to the sub-circuit at stack position [index] (0 is the root circuit). */
	fun navigateTo(index: Int) = guarded(Unit) {
		navigationStack.entry(index)?.let { navigationStack.navigateBackTo(it) }
	}

	/** ---- Simulation */

	fun setSimulating(simulating: Boolean) = guarded(Unit) {
		if (simulating && dataController.data == null) return@guarded
		if (simulating) saveNow()
		modeHolder.setMode(if (simulating) ApplicationMode.EXECUTE else ApplicationMode.EDIT) {}
	}

	fun togglePause() = guarded(Unit) {
		frame.graphPanelViewController.pauseOrResumeAction.execute(actionEvent("pause"))
	}

	fun step() = guarded(Unit) {
		frame.graphPanelViewController.singleStepModeAction.execute(actionEvent("step"))
	}

	fun setSpeed(speed: Int) = guarded(Unit) {
		frame.applicationContextHolder.currentSystemSpeedCategory.systemSpeed.speed = speed
	}

	/** ---- Saving */

	private fun scheduleAutosave() {
		autosaveTimer?.let { window.clearTimeout(it) }
		autosaveTimer = window.setTimeout({
			autosaveTimer = null
			saveNow()
			notifyChanged()
		}, 700)
	}

	/**
	 * Stores the edited circuit in its project without resetting the undo history.
	 * The desktop's explicit save resets the history, which would hurt continuous saving.
	 */
	fun saveNow() = guarded(Unit) {
		autosaveTimer?.let { window.clearTimeout(it); autosaveTimer = null }
		val data = dataController.data ?: return@guarded
		val savable = data.savable as? AbstractLibraryItemSavable ?: return@guarded
		if (!savable.editable || modeHolder.currentMode.isExecute() || !(unsaved || editor.commandManager.hasChanges())) return@guarded
		val lib = savable.item.library ?: return@guarded
		saving = true
		try {
			BaseModule.eventBus.post(BeforeSaveEvent(savable))
			when {
				savable is LibrarySavable && data.content is MetaGraph ->
					lib.libraryService.updateContainerLibraryElement(lib, data.content as MetaGraph, savable.element, doClone = true)
				savable.item is UndoableStateLibraryItem<*> -> {
					// Store a copy, so later edits of the open document don't change the stored item
					@Suppress("UNCHECKED_CAST")
					(savable.item as UndoableStateLibraryItem<Storable>).updateStorable(StorableCloner.clone(data.content))
					lib.libraryService.updateLibraryItem(lib, savable.item)
				}
			}
			unsaved = false
		} finally {
			saving = false
			notifyChanged()
		}
	}

	/** ---- Library / palette */

	/** Returns the component palette (all libraries imported by the open project) as JSON. */
	fun palette(): String = guarded("[]") { library.palette() }

	/** Adds the palette item [key] at the center of the visible area. */
	fun placeAtCenter(key: String) = guarded(Unit) {
		val view = editor.view
		val center = view.viewToModel(Point2D(view.width / 2, view.height / 2))
		place(key, freeLocationNear(center))
	}

	/** Finds a location near [p] that is not covered by another component, so repeated clicks don't stack parts. */
	private fun freeLocationNear(p: Point2D): Point2D {
		val drawables = editor.view.drawing.drawables
		val step = editor.view.grid.distance * 4
		for (ring in 0..6) {
			for (dy in -ring..ring) {
				for (dx in -ring..ring) {
					if (maxOf(kotlin.math.abs(dx), kotlin.math.abs(dy)) != ring) continue
					val candidate = p.add(dx * step, dy * step * 0.75)
					if (drawables.none { it.boundingBox.contains(candidate) }) return candidate
				}
			}
		}
		return p
	}

	private fun canPlace(): Boolean =
		!isSymbolView && editView.editable && navigationStack.size <= 1 && !modeHolder.currentMode.isExecute()

	private fun place(key: String, location: Point2D) {
		if (!canPlace()) return
		val element = library.element(key) ?: return
		library.checkPlaceable(element, editor)?.let {
			onMessage("info", "", it)
			return
		}
		activateMainView()
		GraphViewModule.graphViewAppService.addGraphElementViewFromLibrary(
			element, snap(location), io.antarescircuit.jabbah.base.geom.Rotation.R0, editor
		)
		mainCanvas?.requestViewFocus()
	}

	private fun snap(p: Point2D): Point2D {
		val result = SnapResult()
		editor.view.grid.snap(p.x, p.y, result)
		return p.add(result.dx, result.dy)
	}

	private val thumbnails by lazy { WebThumbnails() }

	/**
	 * Renders a preview image of palette item [key] in the current theme and returns it as PNG data URL,
	 * or an empty string if it cannot be rendered.
	 */
	fun thumbnail(key: String, width: Int, height: Int, pixelRatio: Double): String =
		library.element(key)?.let { thumbnails.render(it, width, height, pixelRatio) } ?: ""

	/** Returns a description and preview availability of the palette item [key] as JSON. */
	fun paletteItemInfo(key: String): String = guarded("null") { library.info(key) }

	/** ---- Properties */

	/** Returns the editable properties of the current selection (or the circuit) as JSON. */
	fun properties(): String = guarded("null") { properties.describe() }

	/** Sets property [key] to [value] (a JSON value) as an undoable command. */
	fun setProperty(key: String, value: String) = guarded(Unit) {
		properties.set(key, JSON.parse(value))
		notifyChanged()
	}

	/** ---- Projects */

	/** Lists all projects stored in the browser as JSON. */
	fun projects(): String = guarded("[]") { library.projects() }

	/** Creates a new project with one empty circuit and opens it. Returns the project's UUID. */
	fun createProject(name: String): String = guarded("") {
		saveNow()
		val project = library.createProject(name)
		openProject(project.uuid.id)
		project.uuid.id
	}

	fun openProject(uuid: String) = guarded(Unit) {
		// Reopening would load a second instance of the open project, detaching the edited circuit from it
		if (LibraryModule.libraryHolder.l?.uuid?.id == uuid) return@guarded
		saveNow()
		if (modeHolder.currentMode.isExecute()) modeHolder.setMode(ApplicationMode.EDIT) {}
		editor.commandManager.reset()
		dataController.openProject(LibraryIdentification(UUID(uuid), LOCAL_USER))
	}

	fun renameProject(uuid: String, name: String) = guarded(Unit) {
		library.renameProject(UUID(uuid), name)
	}

	fun deleteProject(uuid: String) = guarded(Unit) {
		if (LibraryModule.libraryHolder.l?.uuid?.id == uuid) {
			editor.commandManager.reset()
			dataController.closeData()
			LibraryModule.libraryHolder.l = null
		}
		library.deleteProject(UUID(uuid))
	}

	/** Imports a project from exported JSON (see [exportProject]) and returns its new UUID. */
	fun importProject(json: String): String = guarded("") {
		// Replacing the open project: close it, so that opening it again loads the imported version
		val uuid = (JSON.parse<dynamic>(json).uuid as? String)
		if (uuid != null && LibraryModule.libraryHolder.l?.uuid?.id == uuid) {
			if (modeHolder.currentMode.isExecute()) modeHolder.setMode(ApplicationMode.EDIT) {}
			editor.commandManager.reset()
			dataController.closeData()
			LibraryModule.libraryHolder.l = null
		}
		library.importProject(json)
	}

	/** Imports the bundled sample project [uuid] as a new, editable project and returns its UUID. */
	fun importSample(uuid: String): String = guarded("") { library.importSample(UUID(uuid)) }

	/** Exports the open project with all its circuits as JSON. */
	fun exportProject(): String = guarded("") {
		saveNow()
		library.exportProject(LibraryModule.libraryHolder.l as Project)
	}

	/** ---- Libraries used by the open project */

	/** Lists the system libraries as JSON, with whether the open project imports them. */
	fun libraries(): String = guarded("[]") {
		val project = LibraryModule.libraryHolder.l as? Project
		val direct = project?.importedLibraryIds ?: emptySet()
		val expanded = project?.expandedImports
		toJson(LibraryModule.systemLibraryDictionaryService.getEntries().map { entry ->
			val imported = direct.contains(entry.uuid)
			val indirect = !imported && expanded?.contains(entry.uuid) == true
			val description = RichText.stripToPlainText(entry.description.value ?: "")
			obj {
				this.uuid = entry.uuid.id
				this.name = entry.name.value
				this.description = description
				this.imported = imported
				this.indirect = indirect
			}
		}.toJsArray())
	}

	/** Makes the components of system library [uuid] available in the open project. */
	fun addLibrary(uuid: String) = guarded(Unit) {
		val project = LibraryModule.libraryHolder.l as? Project ?: return@guarded
		project.libraryService.addImport(project, UUID(uuid))
	}

	/** Removes system library [uuid] from the open project, unless circuits still use its components. */
	fun removeLibrary(uuid: String) = guarded(Unit) {
		ProjectModule.projectManagementService.removeImport(UUID(uuid), emptySet())
	}

	/** ---- Circuits */

	/** Returns all items of the open project (circuits, truth tables, state machines, …) as JSON tree. */
	fun items(): String = guarded("null") {
		library.items(LibraryModule.libraryHolder.l, openItemUuid)
	}

	private val openItemUuid: String?
		get() = (dataController.data?.savable as? AbstractLibraryItemSavable)?.item?.let { WebItemKinds.uuidOf(it) }

	/** Opens the project item [uuid]: a circuit in the editor, other items as documents. */
	fun openItem(uuid: String) = guarded(Unit) {
		val item = library.item(uuid) ?: return@guarded
		if (openItemUuid == uuid) return@guarded
		saveNow()
		if (modeHolder.currentMode.isExecute()) modeHolder.setMode(ApplicationMode.EDIT) {}
		editor.commandManager.reset()
		when (item) {
			is ContainerLibraryElement -> BaseModule.eventBus.post(OpenContainerLibraryElementRequest(item))
			is UndoableStateLibraryItem<*> -> dataController.openLibraryItem(item, uuid, item.name.value)
			is io.antarescircuit.jabbah.graph.model.image.ImageLibraryElement ->
				BaseModule.eventBus.post(io.antarescircuit.jabbah.graph.model.image.OpenImageLibraryElementRequest(item))
		}
	}

	/** Renames project item [uuid]. */
	fun renameItem(uuid: String, name: String) = guarded(Unit) {
		val item = library.item(uuid) ?: return@guarded
		if (item is ContainerLibraryElement) {
			renameCircuit(uuid, name)
			return@guarded
		}
		val lib = item.library ?: return@guarded
		saveNow()
		item.name = Name(TranslatableText(name))
		// The open document is a copy of the item's content; rename it too, so autosave keeps the new name
		if (openItemUuid == uuid) (dataController.data?.content as? io.antarescircuit.jabbah.edit.model.text.description.Namable)?.name = Name(TranslatableText(name))
		lib.libraryService.updateLibraryItem(lib, item)
	}

	/** Deletes project item [uuid], closing it if it is open. */
	fun deleteItem(uuid: String) = guarded(Unit) {
		val item = library.item(uuid) ?: return@guarded
		val lib = item.library ?: return@guarded
		val isOpen = openItemUuid == uuid
		BaseModule.eventBus.postTwoPhase(
			DeleteLibraryItemRequest(item),
			thenHandler = {
				if (isOpen) {
					editor.commandManager.reset()
					dataController.closeData()
				}
				lib.libraryService.removeLibraryItem(lib, item)
			},
			elseHandler = { e -> onMessage("info", "", e.message ?: "") }
		)
	}

	/** Creates a new circuit in the open project and opens it. [type] is "digital" or "analog". */
	fun createCircuit(name: String, type: String) = guarded(Unit) {
		val project = LibraryModule.libraryHolder.l ?: return@guarded
		val graphType: GraphType = if (type == "analog") AntaresGraphTypes.Analog else AntaresGraphTypes.Digital
		MetaGraph.validateName(TranslatableText(name))
		saveNow()
		val metaGraph = MetaGraph.create(TranslatableText(name), graphType)
		val element = project.libraryService.addContainerLibraryElement(project, metaGraph, project)
		openItem(element.uuid.id)
	}

	fun renameCircuit(uuid: String, name: String) = guarded(Unit) {
		val element = LibraryModule.libraryHolder.getContainerLibraryElement(UUID(uuid)) ?: return@guarded
		MetaGraph.validateName(TranslatableText(name))
		saveNow()
		element.library!!.libraryService.renameContainerLibraryElement(element, TranslatableText(name))
		(dataController.data?.content as? MetaGraph)?.let {
			if (it.uuid == element.uuid) it.graph.model?.name = Name(TranslatableText(name))
		}
	}

	fun duplicateCircuit(uuid: String) = guarded(Unit) {
		val element = LibraryModule.libraryHolder.getContainerLibraryElement(UUID(uuid)) ?: return@guarded
		val lib = element.library!!
		saveNow()
		val copy = lib.libraryService.duplicateContainerLibraryElement(
			lib.libraryService.getDirectoryOf(lib, element),
			element,
			TranslatableText(Translations.getString("library.action.duplicateGraph.copyOf", element.name.value))
		)
		openItem(copy.uuid.id)
	}

	fun setDefaultCircuit(uuid: String) = guarded(Unit) {
		// Projects and user libraries both open their default circuit first
		val project = LibraryModule.libraryHolder.l ?: return@guarded
		project.libraryService.setDefaultElement(project, UUID(uuid))
	}

	/** ---- Preferences */

	/** Sets how wires are drawn: "autoLayout" (drag from pin to pin) or "setPoints" (click pins and waypoints). */
	fun setConnectMethod(method: String) = guarded(Unit) {
		val connectMethod = io.antarescircuit.jabbah.graph.view.connect.ConnectMethod.withName(method)
		BaseModule.properties.set(io.antarescircuit.jabbah.graph.view.connect.ConnectMethod.PROP_CONNECT_METHOD, connectMethod.customName)
		io.antarescircuit.jabbah.graph.view.connect.CurrentConnectMethod.defaultMethod = connectMethod
	}

	/** ---- Appearance */

	fun setDark(dark: Boolean) = guarded(Unit) {
		Themes.store(if (dark) DARK_THEME else LIGHT_THEME)
		AntaresThemes.install()
		mainCanvas?.let { it.backgroundColor = io.antarescircuit.jabbah.draw.style.DrawStyleModule.styleProvider.getStyle(io.antarescircuit.jabbah.draw.style.StyleType.BACKGROUND).color.backgroundColor; it.repaint() }
		containerCanvas?.let { it.backgroundColor = io.antarescircuit.jabbah.draw.style.DrawStyleModule.styleProvider.getStyle(io.antarescircuit.jabbah.draw.style.StyleType.BACKGROUND).color.backgroundColor; it.repaint() }
		editor.view.repaint()
	}
}

/** An [EventBus] that reports every posted event, so the web application can refresh after any change. */
private class NotifyingEventBus(
	private val delegate: EventBus,
	private val listener: (Any) -> Unit,
) : EventBus by delegate {
	override fun post(event: Any) {
		delegate.post(event)
		listener(event)
	}
}
