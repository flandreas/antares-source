package ch.scorpion.jabbah.graph.view.port

import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.container.PortViewComponent

/**
 * A factory for creating various instances of {@link Port} related classes.
 * @param T the type of signal that the {@link Port}s process.
 */
interface PortFactory{

    fun <T: Any> createSubGraphPort(graphPort: GraphPort<T>): Port<T>

    fun <T: Any> createPortView(port: Port<T>): PortView<T>

    fun <T: Any> createPortViewComponent(portView: PortView<T>): PortViewComponent<T>
}

/** Undefined implemenation of the [PortFactory] interface according to the null pattern.*/
class UndefinedPortFactory() : PortFactory {

    override fun <T : Any> createSubGraphPort(graphPort: GraphPort<T>): Port<T> {
        throw UnsupportedOperationException("not implemented")
    }

    override fun <T : Any> createPortView(port: Port<T>): PortView<T> {
        throw UnsupportedOperationException("not implemented")
    }

    override fun <T : Any> createPortViewComponent(portView: PortView<T>): PortViewComponent<T> {
        throw UnsupportedOperationException("not implemented")
    }
}