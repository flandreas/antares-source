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
	graphName: String,
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	private val eventBus: EventBus = BaseModule.eventBus
) : GraphViewBuilder<DigitalSignal>(graphName) {

	/** Builds a [GraphView] that contains only an input and an output, i.e. that perform a "no operation".*/
	fun buildNOP(propagationDelay: Long = 0): GraphView<GraphElementView<out GraphElement>> {
		connect(addInput("I"), addOutput("O"))
		graph.propagationDelay = propagationDelay
		return graphView
	}

	/** Build a "no operation" [GraphView] that contains the specified inner "no operation" [SubGraphVerticeView].*/
	fun buildOuterNOP(nop: SubGraphVerticeView<*>, propagationDelay: Long = 0): GraphView<GraphElementView<*>> {
		graphView.add(nop)
		connect(addInput("A"), nop)
		connect(nop, addOutput("B"))
		graph.propagationDelay = propagationDelay
		return graphView
	}

    /**
     * Builds a [GraphView] that contains a [NotGateView] along with [CircuitInOutView] for input and output.
     */
    fun buildCustomNot(): GraphView<GraphElementView<out GraphElement>> {
        val not = addVerticeView(NotGateView())
        connect(addInput(), not)
        connect(not, addOutput())
        return graphView
    }

    /**
     * Builds a [GraphView] that consists of a elementary [AndGateView] and the provided custom "Not" gate view.
     */
    fun buildCustomNAND(notView: SubGraphVerticeView<*>): GraphView<GraphElementView<*>> {
        graphView.add(notView)
        val andView = addVerticeView(AndGateView())

        connect(addInput(), andView, andView.vertice.getInput(1))
        connect(addInput(), andView, andView.vertice.getInput(2))
        connect(andView, notView)
        connect(notView, addOutput())

        return graphView
    }

	fun addInput(name: String? = null): CircuitInOutView {
		val inout = CircuitInOutView(styleProvider, CircuitInOutImpl(eventBus, name, PortType.INPUT), eventBus)
		graphView.add(inout)
		return inout
	}

	private fun addOutput(name: String? = null): CircuitInOutView {
		val inout = CircuitInOutView(styleProvider, CircuitInOutImpl(eventBus, name, PortType.OUTPUT), eventBus)
		graphView.add(inout)
		return inout
	}
}