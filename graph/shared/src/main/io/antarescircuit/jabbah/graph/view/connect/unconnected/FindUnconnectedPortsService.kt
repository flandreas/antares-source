package io.antarescircuit.jabbah.graph.view.connect.unconnected

import io.antarescircuit.jabbah.base.*
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.vertice.NamedConnector
import io.antarescircuit.jabbah.graph.view.connect.unconnected.FindUnconnectedPortsType.*

enum class FindUnconnectedPortsType(val customName: String) {
    Inputs("inputs"),
    Outputs("outputs"),

    /** All unconnected ports, plus [NamedConnector]s without counterpart and wires with an open end. */
    All("all");

    companion object {

        fun withCustomName(customName: String): FindUnconnectedPortsType =
            entries.firstOrNull { it.customName == customName }
                ?: throw IllegalArgumentException("Unknown FindUnconnectedPortsType: $customName")
    }
}

/** The kinds of problems reported by [FindUnconnectedPortsService]. */
enum class UnconnectedKind {
    /** A vertice with unconnected ports. */
    Port,

    /** A local [NamedConnector] whose name occurs only once in its graph. */
    LoneConnector,

    /** A global [NamedConnector] whose name occurs only once in the library. */
    LoneGlobalConnector,

    /** A [NamedConnector] without name. */
    UnnamedConnector,

    /** An edge view with at least one open end. */
    OpenEdge,
}

/**
 * A problem found by [FindUnconnectedPortsService].
 *
 * @property viewId the ID of the vertice or edge view to focus on
 * @property portIds the unconnected ports if [kind] is [UnconnectedKind.Port]
 * @property name the name of the [NamedConnector], if any
 */
data class UnconnectedElement(
    val metaGraphId: UUID,
    val metaGraphName: String,
    val viewId: Int,
    val viewDescription: String,
    val kind: UnconnectedKind,
    val portIds: Set<Int> = emptySet(),
    val name: String? = null,
)

object FindUnconnectedPortsService {

    fun findInLibrary(library: Library, type: FindUnconnectedPortsType): Set<UnconnectedElement> {
        val globalNameCounts = if (type == All) countGlobalConnectorNames(library) else null
        val result = mutableSetOf<UnconnectedElement>()
        library.metaGraphIds.forEach { metaGraphId ->
            result.addAll(findInMetaGraphImpl(library.getMetaGraph(metaGraphId), type, globalNameCounts))
        }
        return result
    }

    /**
     * Global [NamedConnector]s are only checked if [library] is given, because their counterparts
     * may reside in any graph of [library] or its imports.
     */
    fun findInMetaGraph(metaGraph: MetaGraph, type: FindUnconnectedPortsType, library: Library? = null): Set<UnconnectedElement> {
        val globalNameCounts = if (type == All) library?.let { countGlobalConnectorNames(it) } else null
        return findInMetaGraphImpl(metaGraph, type, globalNameCounts)
    }

    private fun findInMetaGraphImpl(
        metaGraph: MetaGraph,
        type: FindUnconnectedPortsType,
        globalNameCounts: Map<String, Int>?,
    ): Set<UnconnectedElement> {
        val result = mutableSetOf<UnconnectedElement>()

        if (metaGraph.graph.model?.purelyScripted == true) {
            return result
        }

        val graphView = metaGraph.graph.graphView
        fun add(viewId: Int, viewType: String, kind: UnconnectedKind, portIds: Set<Int> = emptySet(), name: String? = null) {
            result.add(UnconnectedElement(metaGraph.uuid, metaGraph.name, viewId, "$viewType $viewId", kind, portIds, name))
        }

        graphView.getVerticeViews()
            .forEach { vv ->
                // Ports without view, such as the invisible port of a tunnel, cannot be connected by the user
                fun isUnconnected(p: Port<*>) = !p.isConnected && vv.getPortView(p) != null

                val portIds = mutableSetOf<Int>()
                if (type == Inputs || type == All) {
                    vv.model.getInputs().forEach { p ->
                        if (isUnconnected(p)) {
                            portIds.add(p.portId)
                        }
                    }
                }
                if (type == Outputs || type == All) {
                    vv.model.getOutputs().forEach { p ->
                        if (isUnconnected(p)) {
                            portIds.add(p.portId)
                        }
                    }
                }
                if (portIds.isNotEmpty()) {
                    add(vv.id, vv.type, UnconnectedKind.Port, portIds)
                }
        }

        if (type == All) {
            val connectorViews = graphView.getVerticeViews().filter { it.model is NamedConnector }
            val localNameCounts = connectorViews
                .map { it.model as NamedConnector }
                .filter { !it.isGlobal }
                .mapNotNull { it.name?.ifEmpty { null } }
                .groupingBy { it }
                .eachCount()

            connectorViews.forEach { vv ->
                val connector = vv.model as NamedConnector
                val name = connector.name
                when {
                    name.isNullOrEmpty() -> add(vv.id, vv.type, UnconnectedKind.UnnamedConnector)
                    !connector.isGlobal && localNameCounts[name] == 1 ->
                        add(vv.id, vv.type, UnconnectedKind.LoneConnector, name = name)
                    connector.isGlobal && globalNameCounts?.get(name) == 1 ->
                        add(vv.id, vv.type, UnconnectedKind.LoneGlobalConnector, name = name)
                }
            }

            graphView.getEdgeViews()
                .filter { !it.isFullyConnected }
                .forEach { add(it.id, it.type, UnconnectedKind.OpenEdge) }
        }

        return result
    }

    /** Counts the names of global [NamedConnector]s in all graphs of [library] and its imports. */
    private fun countGlobalConnectorNames(library: Library): Map<String, Int> =
        library.expandedImports.libraries
            .flatMap { lib -> lib.metaGraphIds.map { lib.getMetaGraph(it) } }
            .flatMap { metaGraph -> metaGraph.graph.graphView.getVerticeViews().map { it.model } }
            .filterIsInstance<NamedConnector>()
            .filter { it.isGlobal }
            .mapNotNull { it.name?.ifEmpty { null } }
            .groupingBy { it }
            .eachCount()

    fun postAsIssues(elements: Set<UnconnectedElement>, eventBus: EventBus, handler: (Issue) -> Unit) {
        elements.forEach { e ->
            val (title, desc) = when (e.kind) {
                UnconnectedKind.Port -> Translations.getString("element.unconnectedPort.name") to
                    Translations.getString("graph.action.findUnconnectedPorts.issueDesc", e.portIds.joinToString(","))
                UnconnectedKind.LoneConnector -> Translations.getString("element.loneConnector.name") to
                    Translations.getString("graph.action.findUnconnectedPorts.loneConnectorDesc", e.name ?: "")
                UnconnectedKind.LoneGlobalConnector -> Translations.getString("element.loneConnector.name") to
                    Translations.getString("graph.action.findUnconnectedPorts.loneGlobalConnectorDesc", e.name ?: "")
                UnconnectedKind.UnnamedConnector -> Translations.getString("element.loneConnector.name") to
                    Translations.getString("graph.action.findUnconnectedPorts.unnamedConnectorDesc")
                UnconnectedKind.OpenEdge -> Translations.getString("element.openEdge.name") to
                    Translations.getString("graph.action.findUnconnectedPorts.openEdgeDesc")
            }
            eventBus.post(
                IssueImpl(
                    IssueSeverity.Warning,
                    title,
                    desc,
                    e.metaGraphName,
                    e.viewDescription,
                    e,
                    handler
                )
            )
        }
    }
}
