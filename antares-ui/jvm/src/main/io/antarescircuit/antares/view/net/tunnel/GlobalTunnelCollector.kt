package io.antarescircuit.antares.view.net.tunnel

import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.net.Tunnel
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.library.LibraryModule

/**
 * Collects information about all global [Tunnel]s in all [DigitalGraph]s of the current [Library],
 * including all its imported [Libraries][Library].
 */
class GlobalTunnelCollector {

    private val result = mutableMapOf<String, MutableList<GlobalTunnelUsage>>()

    fun collect(): GlobalTunnelCollectionResult {
        result.clear()
        LibraryModule.libraryHolder.library.expandedImports.libraries.forEach {
            searchLibrary(it)
        }
        return result
    }

    private fun searchLibrary(library: Library) {
        library.metaGraphIds.forEach {
            searchMetaGraph(library.getMetaGraph(it))
        }
    }

    private fun searchMetaGraph(metaGraph: MetaGraph) {
        metaGraph.graph.graphView
            .getDrawables { it is TunnelView && it.isGlobal }
            .map { it as TunnelView }
            .filter { StringUtils.isNotBlank(it.name) }
            .forEach { tunnelView ->
                val usages = result.getOrPut(tunnelView.model.name!!) { mutableListOf() }

                var usage = usages.firstOrNull { it.graphUUID == metaGraph.uuid }
                if (usage == null) {
                    usage = GlobalTunnelUsage(metaGraph.uuid, metaGraph.name)
                    usages.add(usage)
                }
                with (usage) {
                    inFlowDirection = inFlowDirection || tunnelView.flowDirection == TunnelFlowDirection.In
                    outFlowDirection = outFlowDirection || tunnelView.flowDirection == TunnelFlowDirection.Out
                    inOutFlowDirection = inOutFlowDirection || tunnelView.flowDirection == TunnelFlowDirection.InOut
                }
            }
    }
}

typealias GlobalTunnelCollectionResult = Map<String, List<GlobalTunnelUsage>>

data class GlobalTunnelUsage(
    val graphUUID: UUID,
    val circuitName: String,
    var inFlowDirection: Boolean = false,
    var outFlowDirection: Boolean = false,
    var inOutFlowDirection: Boolean = false
)