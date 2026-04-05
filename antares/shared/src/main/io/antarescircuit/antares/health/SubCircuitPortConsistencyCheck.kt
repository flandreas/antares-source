package io.antarescircuit.antares.health

import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeImpl
import io.antarescircuit.jabbah.graph.container.ContainerDrawing
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.port.SubCircuitPort
import io.antarescircuit.antares.model.inout.DigitalCircuitInOut
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.app.SystemMalfunctionEvent
import io.antarescircuit.jabbah.app.health.SystemHealthCheck
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.graph.MetaGraph

/**
 * Checks if all [SubCircuitPort.bitWidth] in a [SubGraphVerticeImpl] (model of [ContainerDrawing])
 * are consistent with the corresponding [DigitalCircuitInOut.bitWidth] in the [DigitalGraph]
 */
object SubCircuitPortConsistencyCheck : SystemHealthCheck {

    override fun execute(data: ApplicationData): SystemMalfunctionEvent? {
        if (data.content !is MetaGraph) {
            return null
        }
        return execute(data.content as MetaGraph)
    }

    private fun execute(metaGraph: MetaGraph): SystemMalfunctionEvent? {
        val model = metaGraph.containerDrawing.model
        if (model is SubGraphVerticeImpl) {
            model.getSubGraphPorts().forEach { subGraphPort ->
                val graphPort = metaGraph.graph.model!!.getGraphPort<DigitalSignal>(subGraphPort.name!!)
                if (subGraphPort is SubCircuitPort && graphPort is DigitalCircuitInOut) {
                    if (graphPort.bitWidth.width != subGraphPort.bitWidth.width) {
                        val desc = StringBuilder(Translations.getString("antares.system.inconsistentSubCircuitPortBitWidth.msg"))
                        desc.append(" - Port name: '${graphPort.name}', BitWidth in graph:${graphPort.bitWidth.width}, BitWidth in symbol: ${subGraphPort.bitWidth.width}")
                        return SystemMalfunctionEvent(desc.toString())
                    }
                }
            }
        }
        return null
    }
}