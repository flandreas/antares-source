package ch.scorpion.antares

import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.antares.model.inout.DigitalCircuitInOutImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.BitWidthExpression
import ch.scorpion.antares.model.signal.BitWidthGraphParamType
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.gate.LogicGateView
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinition
import ch.scorpion.jabbah.graph.model.param.LongValueExpression
import ch.scorpion.jabbah.graph.model.param.LongValueGraphParamType
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Supports building [GraphView]s that contain Antares components.
 */
class TestCircuitBuilder(
	graphName: String,
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	private val eventBus: EventBus = BaseModule.eventBus
) : GraphViewBuilder<DigitalSignal>(graphName, AntaresGraphTypes.Digital) {

	/** Builds a [GraphView] that contains only an input and an output, i.e. that perform a "no operation".*/
	fun buildNOP(
		propagationDelay: Long = 0,
		bitWidth: BitWidth = BitWidth.BW_1,
		inputName: String = "I",
		outputName: String = "O",
		inputStartValue: DigitalSignal? = null
	): GraphView {
		connect(addInput(inputName, bitWidth, inputStartValue), addOutput(outputName, bitWidth))
		graph.overallPropagationDelay = propagationDelay
		return graphView
	}

	/** Build a "no operation" [GraphView] that contains the specified inner "no operation" [SubGraphVerticeView].*/
	fun buildOuterNOP(nop: SubGraphVerticeView<*>, propagationDelay: Long = 0): GraphView {
		graphView.add(nop)
		connect(addInput("A"), nop)
		connect(nop, addOutput("B"))
		graph.overallPropagationDelay = propagationDelay
		return graphView
	}

    /**
     * Builds a [GraphView] that contains a NOT gate along with [DigitalCircuitInOutView] for input and output.
     */
    fun buildCustomNot(): GraphView {
        val not = addVerticeView(LogicGateView.notGateView())
        connect(addInput(), not)
        connect(not, addOutput())
        return graphView
    }

    /**
     * Builds a [GraphView] that consists of a elementary AND gate and the provided custom "Not" gate view.
     * If [notView] is `null`, an elementary NOT gate is used.
     */
    fun buildCustomNAND(notView: SubGraphVerticeView<*>?, bitWidth: BitWidth = BitWidth.BW_1): GraphView {
	    val effNotView: VerticeView<*> = notView ?: LogicGateView.notGateView(bitWidth).also { it.bitWidth = bitWidth }
        graphView.add(effNotView)
        val andView = addVerticeView(LogicGateView.andGateView().also { it.bitWidth = bitWidth })

        connect(addInput(bitWidth = bitWidth), andView, andView.vertice.getInput(1))
        connect(addInput(bitWidth = bitWidth), andView, andView.vertice.getInput(2))
        connect(andView, effNotView)
        connect(effNotView, addOutput(bitWidth = bitWidth))

        return graphView
    }

	/**
	 * Build a [GraphView] that consists of a [DigitalCircuitInOutImpl] with [PortType.INOUT] connected to a
	 * [DigitalCircuitInOutImpl] with [PortType.OUTPUT].
	 */
	fun buildInOutToOut(): GraphView {
		connect(addInOut("IO"), addOutput("O"))
		return graphView
	}

	/**
	 * Build a [GraphView] that consists of a [DigitalCircuitInOutImpl] with [PortType.INOUT] connected to a
	 * [DigitalCircuitInOutImpl] with [PortType.INOUT].
	 */
	fun buildInOutToInOut(): GraphView {
		connect(addInOut("IO1"), addInOut("IO2"))
		return graphView
	}

	/**
	 * Builds a [GraphView] with 2 inputs and 1 output, no content circuitry, but a DSL execution script.
	 */
	fun buildScriptedBinaryFunction(input1Name: String, input2Name: String, outputName: String, script: String): GraphView {
		addInput(input1Name)
		addInput(input2Name)
		addOutput(outputName)
		graphView.graph!!.script = script
		return graphView
	}

	/**
	 * Builds a [GraphView] whose [Graph] consists of a [DigitalCircuitInOutImpl] of type [PortType.INPUT]
	 * with [BitWidthExpression] [inputExpression] and a [DigitalCircuitInOutImpl] of type [PortType.OUTPUT]
	 * with [BitWidthExpression] [outputExpression], both unconnected.
	 * Adds a [GraphParamDefinition] with a parameter of type [BitWidthGraphParamType] with name "BW"
	 */
	fun buildBitWidthExpressionInputOutput(
		inputExpression: String,
		outputExpression: String,
		graphScript: String? = null
	): GraphView {
		val input = addInput("I")
		input.bitWidth = BitWidthExpression(inputExpression)
		val output = addOutput("O")
		output.bitWidth = BitWidthExpression(outputExpression)

		graph.purelyScripted = true
		graphScript?.let { graph.script = it }

		graph.parameterDefinitions = graph.parameterDefinitions.withDefinition(
			GraphParamDefinition.create("BW", BitWidthGraphParamType, BitWidth.BW_4))

		return graphView
	}

	/**
	 * Builds a [GraphView] with an OR gate having [expression] as propagation delay expression.
	 * Adds a [GraphParamDefinition] with a parameter of type [LongValueGraphParamType] with name [parameterName].
	 */
	fun buildPropagationDelayExpressionOrGate(
		parameterName: String,
		expression: String
	): GraphView {
		val orGateView = addVerticeView(LogicGateView.orGateView())
		orGateView.propagationDelay = LongValueExpression(expression)

		graph.parameterDefinitions = graph.parameterDefinitions.withDefinition(
			GraphParamDefinition.create(parameterName, LongValueGraphParamType, LongValueImpl(20))
		)

		return graphView
	}

	fun addInput(
		name: String? = null,
		bitWidth: BitWidth = BitWidth.BW_1,
		inputStartValue: DigitalSignal? = null
	): DigitalCircuitInOutView = addInOut(name, PortType.INPUT, bitWidth, inputStartValue)

	fun addOutput(
		name: String? = null,
		bitWidth: BitWidth = BitWidth.BW_1
	): DigitalCircuitInOutView = addInOut(name, PortType.OUTPUT, bitWidth)

	fun addInOut(
		name: String? = null,
		bitWidth: BitWidth = BitWidth.BW_1
	): DigitalCircuitInOutView = addInOut(name, PortType.INOUT, bitWidth)

	private fun addInOut(
		name: String? = null,
		portType: PortType,
		bitWidth: BitWidth = BitWidth.BW_1,
		inputStartValue: DigitalSignal? = null
	): DigitalCircuitInOutView {
		val inout = DigitalCircuitInOutView(styleProvider, DigitalCircuitInOutImpl(eventBus, name, portType, bitWidth), eventBus)
		inout.model.startValue = inputStartValue
		graphView.add(inout)
		return inout
	}
}