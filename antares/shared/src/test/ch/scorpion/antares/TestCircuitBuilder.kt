package ch.scorpion.antares

import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.gate.AndGateView
import ch.scorpion.antares.view.gate.NotGateView
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Supports building [GraphView]s that contain Antares components.
 */
class TestCircuitBuilder(
    val graphName: String,
    val styleProvider: StyleProvider,
    val eventBus: EventBus
) : GraphViewBuilder<DigitalSignal>() {

    constructor(graphName: String): this(graphName, DrawStyleModule.styleProvider, BaseModule.eventBus)

    /**
     * Builds a [GraphView] that contains a [NotGateView] along with [CircuitInOutView] for input and output.
     */
    fun buildCustomNot(): GraphView<GraphElementView<out GraphElement>> {
        val not = addVertice(NotGateView())
        connect(addInput(), not)
        connect(not, addOutput())
        graph.name = graphName
        return graphView
    }

    /**
     * Builds a [GraphView] that consists of a elemtary [AndView] and the provided custom "Not" gate view.
     */
    fun buildCustomNAND(notView: SubGraphVerticeView<*>): GraphView<GraphElementView<*>> {
        graphView.add(notView)
        val andView = addVertice(AndGateView())

        connect(addInput(), andView, andView.vertice.getInput(1))
        connect(addInput(), andView, andView.vertice.getInput(2))
        connect(andView, notView)
        connect(notView, addOutput())

        graph.name = graphName
        return graphView
    }

    fun buildCircuitUsingCustomNAND(customNANDView: SubGraphVerticeView<*>): GraphView<GraphElementView<*>> {
        graphView.add(customNANDView)

        connect(addInput(), customNANDView, customNANDView.vertice.getInput(1))
        connect(addInput(), customNANDView, customNANDView.vertice.getInput(2))
        connect(customNANDView, addOutput())

        graph.name = graphName
        return graphView
    }

    private fun addInput(): CircuitInOutView {
        val inout = CircuitInOutView(styleProvider, CircuitInOutImpl(eventBus, PortType.INPUT), eventBus)
        graphView.add(inout)
        return inout
    }

    private fun addOutput(): CircuitInOutView {
        val inout = CircuitInOutView(styleProvider, CircuitInOutImpl(eventBus, PortType.OUTPUT), eventBus)
        graphView.add(inout)
        return inout
    }


}