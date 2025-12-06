package ch.scorpion.jabbah.graph.view.connect.unconnected

import ch.scorpion.jabbah.base.IssueImpl
import ch.scorpion.jabbah.base.IssueSeverity
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.view.connect.unconnected.FindUnconnectedPortsType.All
import ch.scorpion.jabbah.graph.view.connect.unconnected.FindUnconnectedPortsType.Inputs
import ch.scorpion.jabbah.graph.view.connect.unconnected.FindUnconnectedPortsType.Outputs

enum class FindUnconnectedPortsType(val customName: String) {
    Inputs("inputs"),
    Outputs("outputs"),
    All("all");

    companion object {

        fun withCustomName(customName: String): FindUnconnectedPortsType =
            entries.firstOrNull { it.customName == customName }
                ?: throw IllegalArgumentException("Unknown FindUnconnectedPortsType: $customName")
    }
}

data class UnconnectedPort(
    val metaGraphId: UUID,
    val metaGraphName: String,
    val verticeViewId: Int,
    val verticeViewDescription: String,
    val portIds: Set<Int>
)

object FindUnconnectedPortsService {

    fun findInLibrary(library: Library, type: FindUnconnectedPortsType): Set<UnconnectedPort> {
        val result = mutableSetOf<UnconnectedPort>()
        library.metaGraphIds.forEach { metaGraphId ->
            result.addAll(findInMetaGraph(library.getMetaGraph(metaGraphId), type))
        }
        return result
    }

    fun findInMetaGraph(metaGraph: MetaGraph, type: FindUnconnectedPortsType): Set<UnconnectedPort> {
        val result = mutableSetOf<UnconnectedPort>()

        if (metaGraph.graph.model?.purelyScripted == true) {
            return result
        }

        metaGraph.graph.graphView.getVerticeViews()
            .forEach { vv ->
                val portIds = mutableSetOf<Int>()
                if (type == Inputs || type == All) {
                    vv.model.getInputs().forEach { p ->
                        if (!p.isConnected) {
                            portIds.add(p.portId)
                        }
                    }
                }
                if (type == Outputs || type == All) {
                    vv.model.getOutputs().forEach { p ->
                        if (!p.isConnected) {
                            portIds.add(p.portId)
                        }
                    }
                }
                if (portIds.isNotEmpty()) {
                    result.add(UnconnectedPort(
                        metaGraph.uuid,
                        metaGraph.name,
                        vv.id,
                        vv.type,
                        portIds)
                    )
                }
        }

        return result
    }

    fun postAsIssues(unconnectedPorts: Set<UnconnectedPort>, eventBus: EventBus) {
        unconnectedPorts.forEach { up ->
            eventBus.post(
                IssueImpl(
                    IssueSeverity.Warning,
                    Translations.getString("element.unconnectedPort.name"),
                    // TODO I18N
                    "Port ${up.portIds.joinToString(",")}",
                    up.metaGraphName,
                    "${up.verticeViewDescription} ${up.verticeViewId}",
                )
            )
        }
    }
}