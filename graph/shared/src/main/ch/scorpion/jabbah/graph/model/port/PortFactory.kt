package ch.scorpion.jabbah.graph.model.port

import ch.scorpion.jabbah.graph.model.*

/**
 * A factory for creating various instances of [Port] related classes.
 */
interface PortFactory {

    fun <T: Any> createSubGraphPort(graphPort: GraphPort<T>, type: GraphType): Port<T>

    fun <T: Any> createOscilloscopeProbePort(name: String?, type: GraphType): InputPort<T>
}

/** Undefined implementation of the [PortFactory] interface according to the null pattern.*/
class UndefinedPortFactory : PortFactory {

    override fun <T : Any> createSubGraphPort(graphPort: GraphPort<T>, type: GraphType): Port<T> {
        throw UnsupportedOperationException("not implemented")
    }

    override fun <T : Any> createOscilloscopeProbePort(name: String?, type: GraphType): InputPort<T> {
        throw UnsupportedOperationException("not implemented")
    }
}