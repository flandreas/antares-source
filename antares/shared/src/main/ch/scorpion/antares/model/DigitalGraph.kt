package ch.scorpion.antares.model

import ch.scorpion.antares.model.net.Tunnel
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.graph.GraphImpl
import ch.scorpion.jabbah.base.logger

/**
 * A [GraphImpl] that forwards [DigitalSignal]s.
 */
class DigitalGraph(eventBus: EventBus = BaseModule.eventBus) : GraphImpl(eventBus) {

    private val LOG by logger(DigitalGraph::class)

    /** Forwards signal changes of a [Tunnel] to all other [Tunnel]s with the same name.*/
    private val tunnelHandler = TunnelHandler()

    init {
        LOG.debug("DigitalGraph created")
    }

    override fun handleGraphElementAdded(graphElem: GraphElement) {
        super.handleGraphElementAdded(graphElem)
        if (graphElem is Tunnel) {
            graphElem.addGraphElementListener(tunnelHandler)
        }
    }

    override fun handleGraphElementRemoved(graphElem: GraphElement) {
        super.handleGraphElementRemoved(graphElem)
        if (graphElem is Tunnel) {
            graphElem.removeGraphElementListener(tunnelHandler)
        }
    }

    private fun getTunnels(name: String): ImmutableList<Tunnel> {
        return ImmutableList(elements
                .filter { it is Tunnel && it.name == name }
                .map { it as Tunnel })
    }

    /** Forwards signal changes of a [Tunnel] to all other [Tunnel]s with the same name.*/
    private inner class TunnelHandler : GraphElementAdapter() {
        override fun stateChanged(e: GraphElementEvent) {
            if (e.signalHandler != null) {
                val tunnel = e.element as Tunnel
                if (StringUtils.isNotEmpty(tunnel.name)) {
                    val isOutputDominant = (tunnel.getPort<DigitalSignal>() as DigitalPort).isOutputDominant
                    val signal = tunnel.getInput<DigitalSignal>().getIncomingSignal()!!
                    LOG.debug("Propagating signal '$signal' through tunnel '${tunnel.name}', isOutputDominant is $isOutputDominant")
                    getTunnels(tunnel.name!!)
                            .filter { e.element != it}
                            .forEach {
                                it.setSignal(signal, e.signalHandler!!)
                            }
                }
            }
        }
    }
}