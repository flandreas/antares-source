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
import ch.scorpion.jabbah.graph.model.PortType
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
	fun buildNOP(propagationDelay: Long = 0): GraphView {
		connect(addInput("I"), addOutput("O"))
		graph.propagationDelay = propagationDelay
		return graphView
	}

	/** Build a "no operation" [GraphView] that contains the specified inner "no operation" [SubGraphVerticeView].*/
	fun buildOuterNOP(nop: SubGraphVerticeView<*>, propagationDelay: Long = 0): GraphView {
		graphView.add(nop)
		connect(addInput("A"), nop)
		connect(nop, addOutput("B"))
		graph.propagationDelay = propagationDelay
		return graphView
	}

    /**
     * Builds a [GraphView] that contains a [NotGateView] along with [CircuitInOutView] for input and output.
     */
    fun buildCustomNot(): GraphView {
        val not = addVerticeView(NotGateView())
        connect(addInput(), not)
        connect(not, addOutput())
        return graphView
    }

    /**
     * Builds a [GraphView] that consists of a elementary [AndGateView] and the provided custom "Not" gate view.
     */
    fun buildCustomNAND(notView: SubGraphVerticeView<*>): GraphView {
        graphView.add(notView)
        val andView = addVerticeView(AndGateView())

        connect(addInput(), andView, andView.vertice.getInput(1))
        connect(addInput(), andView, andView.vertice.getInput(2))
        connect(andView, notView)
        connect(notView, addOutput())

        return graphView
    }

	/**
	 * Build a [GraphView] that consists of a [CircuitInOutImpl] with [PortType.INOUT] connected to a
	 * [CircuitInOutImpl] with [PortType.OUTPUT].
	 */
	fun buildInOutToOut(): GraphView {
		connect(addInOut("IO"), addOutput("O"))
		return graphView
	}

	/**
	 * Build a [GraphView] that consists of a [CircuitInOutImpl] with [PortType.INOUT] connected to a
	 * [CircuitInOutImpl] with [PortType.INOUT].
	 */
	fun buildInOutToInOut(): GraphView {
		connect(addInOut("IO1"), addInOut("IO2"))
		return graphView
	}

	fun addInput(name: String? = null): CircuitInOutView = addInOut(name, PortType.INPUT)

	fun addOutput(name: String? = null): CircuitInOutView = addInOut(name, PortType.OUTPUT)

	fun addInOut(name: String? = null): CircuitInOutView = addInOut(name, PortType.INOUT)

	private fun addInOut(name: String? = null, portType: PortType): CircuitInOutView {
		val inout = CircuitInOutView(styleProvider, CircuitInOutImpl(eventBus, name, portType), eventBus)
		graphView.add(inout)
		return inout
	}
}