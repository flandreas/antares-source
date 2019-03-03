package ch.scorpion.jabbah.graph.script

import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * Bridge to access {@link GraphView} information from javascript code.
 * Defines a kind of DSL.
 */

class GraphViewBridge(private val view: DrawingView<GraphView<GraphElementView<*>>>) {

    /** Returns the name of the [Graph] of this [GraphView].*/
    fun name(): String = view.drawing.graph!!.name.value

    /** Returns a scripting bridge for the [GraphElementView] with the specified ID. */
    @Suppress("unused")
    fun elem(id: Int): GraphElementViewBridge {
        return GraphElementViewBridge(view.drawing.getWithId(id)!!)
    }
}

class GraphElementViewBridge(private val graphElementView: GraphElementView<*>) {

    /** Returns the ID of this [GraphElementView].*/
    fun id(): Int = graphElementView.id

    /** Returns the output signal of the first (or one and only) [OutputPort].*/
    fun output(): Any? {
        return (graphElementView.model as Vertice).getOutput<Any>().getOutgoingSignal()
    }

    /** Returns the output signal of the */
    fun output(id: Int): Any? {
        return (graphElementView.model as Vertice).getOutput<Any>(id).getOutgoingSignal()
    }
}

class VerticeBridge(private val vertice: Vertice, private val signalHandler: SignalHandler) {

    /** Returns the input signal of the first (or one and only) [InputPort].*/
    fun input(): Any? {
        return vertice.getInput<Any>().getIncomingSignal()
    }

    fun input(id: Int): Any? {
        return vertice.getInput<Any>(id).getIncomingSignal()
    }

    @Suppress("unused")
    fun setOutput(value: Any) {
        vertice.getOutput<Any>().setOutgoingSignal(value, signalHandler)
    }

    @Suppress("unused")
    fun setOutput(id: Int, value: Any) {
        vertice.getOutput<Any>(id).setOutgoingSignal(value, signalHandler)
    }
}