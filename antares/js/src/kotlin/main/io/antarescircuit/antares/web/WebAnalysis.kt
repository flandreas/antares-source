package io.antarescircuit.antares.web

import io.antarescircuit.antares.view.net.tunnel.TunnelFlowDirection
import io.antarescircuit.antares.view.net.tunnel.TunnelView
import io.antarescircuit.jabbah.base.IssueSeverity
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.draw.graphics.FontImpl
import io.antarescircuit.jabbah.draw.view.FocusDrawablePlayer
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.OpenContainerLibraryElementRequest
import io.antarescircuit.jabbah.graph.model.element.GraphElementCollector
import io.antarescircuit.jabbah.graph.model.element.GraphElementCollectorResultEntry
import io.antarescircuit.jabbah.graph.model.graph.GraphPropagationDelayCalculator
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.ui.NavigationStackEntry
import io.antarescircuit.jabbah.graph.ui.logview.LogView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.connect.unconnected.FindUnconnectedPortsService
import io.antarescircuit.jabbah.graph.view.connect.unconnected.FindUnconnectedPortsType
import io.antarescircuit.jabbah.graph.view.connect.unconnected.UnconnectedElement
import io.antarescircuit.jabbah.graph.view.vertice.OpenHierarchySubGraphRequest
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.draw.ZoomStrategy
import io.antarescircuit.jabbah.draw.ViewTransformation
import io.antarescircuit.jabbah.draw.view.ZoomedPointTranslation

/**
 * Analysis and navigation views of the desktop's graph panel: unconnected ports (`FindUnconnectedPortsAction`),
 * issues (`IssuesViewSwing`), the log of probes (`LogViewSwing`), circuit statistics (`GraphStatistics`),
 * the propagation delay (`CalculatePropagationDelayAction`), global tunnels (`GlobalTunnelPanel`) and the
 * sub-circuit hierarchy (`GraphHierarchyViewSwing`).
 */
@JsExport
class WebAnalysis internal constructor(private val ctx: WebEditorContext) {

	private companion object {
		const val MAX_LOG_ROWS = 1000
	}

	private val panel get() = ctx.frame().graphPanelViewController
	private val navigation get() = panel.editViewController.graphNavigationViewController
	private val metaGraph get() = ctx.dataController().data?.content as? MetaGraph

	init {
		// The passive default view ignores log changes, which arrive outside of editor events
		panel.logViewController.view = object : LogView {
			override fun refresh(oldColumnsCount: Int) = ctx.notifyChanged()
			override fun dispose() {}
		}
	}

	/** ---- Unconnected ports */

	/**
	 * Finds unconnected ports in the open circuit ([scope] `circuit`) or in all circuits of the project (`project`);
	 * [type] is `inputs`, `outputs` or `all` (which includes lone tunnels and open wires). Like on the desktop, the
	 * results are posted as issues that show the element when opened. Returns the number of results.
	 */
	fun unconnected(scope: String, type: String): Int = ctx.guarded(0) {
		val findType = FindUnconnectedPortsType.withCustomName(type)
		val library = LibraryModule.libraryHolder.library
		// The library must contain the current state of the open circuit
		ctx.saveNow()
		val results = if (scope == "project") {
			FindUnconnectedPortsService.findInLibrary(library, findType)
		} else {
			val metaGraph = metaGraph ?: return@guarded 0
			FindUnconnectedPortsService.findInMetaGraph(metaGraph, findType, library)
		}
		issuesController.clearIssues()
		val sorted = results.sortedWith(compareBy({ RichText.stripToPlainText(it.metaGraphName) }, { it.viewId })).toSet()
		FindUnconnectedPortsService.postAsIssues(sorted, BaseModule.eventBus) { issue ->
			(issue.data as? UnconnectedElement)?.let { reveal(it.metaGraphId.id, it.viewId) }
		}
		ctx.notifyChanged()
		results.size
	}

	/**
	 * Selects and focuses the component with [viewId] in the circuit [circuit], opening that circuit first if
	 * necessary (desktop: `AbstractFindUnconnectedPortsAction.handleIssue`).
	 */
	fun reveal(circuit: String, viewId: Int) = ctx.guarded(Unit) {
		if (metaGraph?.uuid?.id == circuit) {
			val stack = navigation.navigationStack
			if (stack.size > 1) stack.entry(0)?.let { stack.navigateBackTo(it, quickMode = true) }
			val view = ctx.editor.view
			view.drawing.getWithId(viewId)?.let {
				view.selectionManager.deselectAll()
				view.selectionManager.select(it)
				FocusDrawablePlayer.playFocus(it, view)
			}
		} else {
			LibraryModule.libraryHolder.library.getContainerLibraryElement(UUID(circuit))?.let {
				BaseModule.eventBus.post(OpenContainerLibraryElementRequest(it, viewId))
			}
		}
		ctx.notifyChanged()
	}

	/** ---- Issues */

	private val issuesController get() = panel.issuesViewController

	/** The collected issues as JSON array of `{index, severity, name, description, origin, context, canOpen}`. */
	fun issues(): String = ctx.guarded("[]") {
		val error = IssueSeverity.Error
		toJson(issuesController.issueCollector.issues.mapIndexed { index, issue ->
			val severity = if (issue.severity == error) "error" else "warning"
			val name = RichText.stripToPlainText(issue.name)
			val description = issue.description?.let { RichText.stripToPlainText(it) }
			val origin = RichText.stripToPlainText(issue.origin)
			val context = issue.context?.let { RichText.stripToPlainText(it) }
			val canOpen = issue.actionHandler != null
			obj {
				this.index = index
				this.severity = severity
				this.name = name
				this.description = description
				this.origin = origin
				this.context = context
				this.canOpen = canOpen
			}
		}.toJsArray())
	}

	/** Shows the origin of the issue at [index], like double-clicking it in the desktop's issues table. */
	fun openIssue(index: Int) = ctx.guarded(Unit) {
		val issue = issuesController.issueCollector.issues.getOrNull(index) ?: return@guarded
		val handler = issue.actionHandler
		if (handler == null) {
			ctx.message("info", Translations.getString("graph.issues.action.open.title"), Translations.getString("graph.issues.action.open.noContent"))
		} else {
			handler(issue)
		}
		ctx.notifyChanged()
	}

	fun clearIssues() = ctx.guarded(Unit) {
		issuesController.clearIssues()
		ctx.notifyChanged()
	}

	/** ---- Log of probes */

	/** The logged values as JSON `{columns: [name], rows: [{time, values: [value]}], truncated}` (latest rows only). */
	fun log(): String = ctx.guarded("null") {
		val history = panel.logViewController.logEventHistory
		val columns = (0 until history.eventColumnsCount).map { RichText.stripToPlainText(history.getEventColumnName(it)) }
		val first = (history.rowsCount - MAX_LOG_ROWS).coerceAtLeast(0)
		val rows = (first until history.rowsCount).map { row ->
			val time = history.getTime(row).toDouble()
			val values = columns.indices.map { history.getValue(row, it) }.toJsArray()
			obj {
				this.time = time
				this.values = values
			}
		}
		val truncated = first > 0
		toJson(obj {
			this.columns = columns.toJsArray()
			this.rows = rows.toJsArray()
			this.truncated = truncated
		})
	}

	fun clearLog() = ctx.guarded(Unit) {
		panel.logViewController.clearAction.execute(ActionEvent(null, this, 0, "clearLog", System.currentTimeMillis()))
		ctx.notifyChanged()
	}

	/** ---- Statistics and propagation delay */

	/**
	 * Counts the components of the displayed (sub-)circuit as JSON `{circuit, immediate, flat, deep}`, each a list of
	 * `{name, count, subCircuit, scripted}` sorted by count (desktop: `GraphStatisticsPanel`).
	 */
	fun statistics(): String = ctx.guarded("null") {
		val graphView = ctx.editor.view.drawing as? GraphView ?: return@guarded "null"
		val graph = graphView.graph ?: return@guarded "null"
		val result = GraphElementCollector(font = FontImpl()).collect(graph)
		val name = RichText.stripToPlainText(graph.name.getTranslation())
		toJson(obj {
			this.circuit = name
			this.immediate = entries(result.immediate)
			this.flat = entries(result.flat)
			this.deep = entries(result.deep)
		})
	}

	private fun entries(entries: Collection<GraphElementCollectorResultEntry>): Array<dynamic> {
		val subGraphClass = SubGraphVerticeRef::class
		return entries.sortedWith(compareByDescending<GraphElementCollectorResultEntry> { it.count }.thenBy { it.name })
			.map { e ->
				val name = RichText.stripToPlainText(e.name)
				val subCircuit = e.clazz == subGraphClass
				obj {
					this.name = name
					this.count = e.count
					this.subCircuit = subCircuit
					this.scripted = e.isScripted
				}
			}.toJsArray()
	}

	/** The longest propagation delay through the open circuit, or -1 if it is undefined (e.g. due to loops). */
	fun propagationDelay(): Double = ctx.guarded(-1.0) {
		val graph = metaGraph?.graph?.model ?: return@guarded -1.0
		GraphPropagationDelayCalculator().calculate(graph).toDouble()
	}

	/** ---- Global tunnels */

	/**
	 * Global tunnels of the project and its imported libraries as JSON array of
	 * `{name, usages: [{circuit, circuitName, inFlow, outFlow, inOut}]}` (desktop: `GlobalTunnelCollector`).
	 */
	fun globalTunnels(): String = ctx.guarded("[]") {
		data class Usage(val circuit: String, val circuitName: String, var inFlow: Boolean = false, var outFlow: Boolean = false, var inOut: Boolean = false)
		val result = mutableMapOf<String, MutableList<Usage>>()
		val flowIn = TunnelFlowDirection.In
		val flowOut = TunnelFlowDirection.Out
		val flowInOut = TunnelFlowDirection.InOut
		// The library must contain the current state of the open circuit
		ctx.saveNow()
		LibraryModule.libraryHolder.library.expandedImports.libraries.forEach { library ->
			library.metaGraphIds.forEach { id ->
				val metaGraph = library.getMetaGraph(id)
				metaGraph.graph.graphView.getDrawables { it is TunnelView && it.isGlobal }
					.map { it as TunnelView }
					.filter { !it.model.name.isNullOrBlank() }
					.forEach { tunnel ->
						val usages = result.getOrPut(tunnel.model.name!!) { mutableListOf() }
						val usage = usages.firstOrNull { it.circuit == metaGraph.uuid.id }
							?: Usage(metaGraph.uuid.id, RichText.stripToPlainText(metaGraph.name)).also { usages.add(it) }
						usage.inFlow = usage.inFlow || tunnel.flowDirection == flowIn
						usage.outFlow = usage.outFlow || tunnel.flowDirection == flowOut
						usage.inOut = usage.inOut || tunnel.flowDirection == flowInOut
					}
			}
		}
		toJson(result.entries
			.map { (name, usages) -> RichText.stripToPlainText(name) to usages }
			.sortedBy { it.first.lowercase() }
			.map { (name, usages) ->
				obj {
					this.name = name
					this.usages = usages.sortedBy { it.circuitName.lowercase() }.map { u ->
						obj {
							this.circuit = u.circuit
							this.circuitName = u.circuitName
							this.inFlow = u.inFlow
							this.outFlow = u.outFlow
							this.inOut = u.inOut
						}
					}.toJsArray()
				}
			}.toJsArray())
	}

	/** ---- Sub-circuit hierarchy */

	/** Sub-circuit views of hierarchy nodes by path (IDs of sub-circuit components from the root, joined by "/"). */
	private val hierarchyViews = mutableMapOf<String, GraphView>()
	private var hierarchyRoot: GraphView? = null

	private fun parsePath(path: String): List<Int> = path.split("/").filter { it.isNotEmpty() }.map { it.toInt() }

	/** Returns the graph view at [path] below the circuit's root, creating sub-circuit views like the desktop's tree. */
	private fun hierarchyView(path: List<Int>): GraphView? {
		val root = panel.graphHierarchyController.rootGraphView ?: return null
		if (root !== hierarchyRoot) {
			hierarchyViews.clear()
			hierarchyRoot = root
		}
		var graphView: GraphView = root
		for (i in path.indices) {
			val parent = graphView
			graphView = hierarchyViews.getOrPut(path.take(i + 1).joinToString("/")) {
				val vv = parent.getWithId(path[i]) as? SubGraphVerticeView<*> ?: return null
				if (vv.model.getGraphIfNotBroken() == null) return null
				vv.createSubGraphView(null)
			}
		}
		return graphView
	}

	/**
	 * The sub-circuits inside the hierarchy node at [path] (empty for the root circuit, which also refreshes the
	 * hierarchy) as JSON array of `{path, id, name, circuit, hasChildren}`, or `null` without circuit.
	 */
	fun hierarchy(path: String): String = ctx.guarded("null") {
		if (path.isEmpty()) hierarchyViews.clear()
		val ids = parsePath(path)
		val graphView = hierarchyView(ids) ?: return@guarded "null"
		toJson(graphView.getSubGraphVerticeViews()
			.filter { it.model.getGraphIfNotBroken() != null }
			.sortedBy { RichText.stripToPlainText(it.describingName).lowercase() }
			.map { vv ->
				val graph = vv.model.getGraphIfNotBroken()!!
				val childPath = (ids + vv.id).joinToString("/")
				val name = RichText.stripToPlainText(vv.describingName)
				val circuit = RichText.stripToPlainText(graph.name.getTranslation())
				val hasChildren = graph.script == null && graph.elements.any { it is SubGraphVerticeRef }
				obj {
					this.path = childPath
					this.id = vv.id
					this.name = name
					this.circuit = circuit
					this.hasChildren = hasChildren
				}
			}.toJsArray())
	}

	/** The name of the circuit at the root of the hierarchy, or `null`. */
	fun hierarchyRootName(): String? = ctx.guarded(null) {
		panel.graphHierarchyController.rootGraphView?.graph?.name?.getTranslation()?.let { RichText.stripToPlainText(it) }
	}

	/** Shows the sub-circuit at [path] in the main view by descending into it level by level. */
	fun navigateHierarchy(path: String) = ctx.guarded(Unit) {
		val stack = navigation.navigationStack
		if (stack.size > 1) stack.entry(0)?.let { stack.navigateBackTo(it, quickMode = true) }
		val drawingView = navigation.drawingView
		val signalHandler = ctx.frame().applicationContextHolder.signalHandlerIfActive
		for (id in parsePath(path)) {
			val vv = drawingView.drawing.getWithId(id) as? SubGraphVerticeView<*> ?: break
			if (vv.model.getGraphIfNotBroken() == null) break
			// Remember zoom and pan of the level being left and where to return to, like GraphNavigationViewController
			stack.peek().content.transformation = ViewTransformation(drawingView.zoomPan, drawingView.transformation.affineTransform.clone())
			stack.peek().voyageOrigin = ZoomedPointTranslation(vv.boundingBox.center, drawingView.modelToView(vv.boundingBox.center), drawingView.zoomFactor)
			stack.push(NavigationStackEntry(subGraphVerticeView = vv, content = drawingView.createContent(vv.createSubGraphView(signalHandler))))
		}
		drawingView.zoomStrategy = ZoomStrategy.FIT_MAX_NORMAL
		ctx.notifyChanged()
	}

	/** Opens the sub-circuit at [path] in a side view, like the desktop hierarchy's "Open" action. */
	fun openHierarchyInSideView(path: String) = ctx.guarded(Unit) {
		val ids = parsePath(path)
		if (ids.isEmpty()) return@guarded
		val parent = hierarchyView(ids.dropLast(1)) ?: return@guarded
		val vv = parent.getWithId(ids.last()) as? SubGraphVerticeView<*> ?: return@guarded
		BaseModule.eventBus.post(OpenHierarchySubGraphRequest(vv, hierarchyRoot ?: return@guarded))
		ctx.notifyChanged()
	}
}
