package ch.scorpion.antares.health

import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeImpl
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.port.SubCircuitPort
import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.SystemMalfunctionEvent
import ch.scorpion.jabbah.app.health.SystemHealthCheck
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.MetaGraph

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
                        return SystemMalfunctionEvent(Translations.getString("antares.system.inconsistentSubCircuitPortBitWidth.msg"))
                    }
                }
            }
        }
        return null
    }
}