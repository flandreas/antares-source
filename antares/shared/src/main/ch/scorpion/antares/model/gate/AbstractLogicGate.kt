package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.BitWidthExpression
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.truthtable.TruthTableModel
import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A digital gate is a [Vertice] that performs a basic logical operation on [DigitalSignal]s, whose number
 * of [InputPort]s can be chosen by the user up to a certain limit.
 */
abstract class AbstractLogicGate(
	gateType: LogicGateType,
	inputCount: PortCount,
	bitWidth: BitWidth = BitWidth.BW_1,
	val minInputCount: PortCount = DEF_MIN_INPUT_COUNT,
	val maxInputCount: PortCount = DEF_MAX_INPUT_COUNT
) : CalculatingVertice(gateType.calculator), MultiSignalSource<DigitalSignal> {

    companion object {
        val LOG by logger(AbstractLogicGate::class)

		/** The name of the [Long] property in [Properties] for the default gate propagation delay. */
		const val PROP_DEFAULT_PROPAGATION_DELAY = "antares.model.gate.defaultPropagationDelay"

	    val DEFAULT_PROPAGATION_DELAY by lazy {
			LongValueImpl(BaseModule.properties.getInt(PROP_DEFAULT_PROPAGATION_DELAY).toLong())
		}

        val DEF_MIN_INPUT_COUNT = PortCount.TWO
        val DEF_MAX_INPUT_COUNT = PortCount.EIGHT
    }

	var gateType: LogicGateType = gateType
		set(value) {
			if (field != value) {
				field = value
				(getOutput<DigitalSignal>() as DigitalPort).logic = gateType.outputLogic
				stateChanged()
			}
		}

	val chosenInputCount: PortCount get() = PortCount.of(inputCount)

	var bitWidth: BitWidth = bitWidth
		set(value) {
			if (field != value) {
				field = value
				getInputs().map { it as DigitalPort }.forEach { it.bitWidth = value }
				(getOutput<DigitalSignal>() as DigitalPort).bitWidth = value
				stateChanged()
			}
		}

    init {
	    require(inputCount.count >= minInputCount.count) { "InputCount ${inputCount.count} below min ${minInputCount.count}" }
	    require(inputCount.count <= maxInputCount.count) { "InputCount ${inputCount.count} above max ${maxInputCount.count}" }

	    propagationDelay = DEFAULT_PROPAGATION_DELAY
	    setupInputCount(inputCount)
    }

	private fun setupInputCount(inputCount: PortCount) {
		clearPorts()
		for (i in 1..inputCount.count) {
			addPort(createInputPort())
		}
		addPort(createOutputPort())
	}

	/** ---- [CalculatingVertice] */

	override val calculator: VerticeCalculator<*> get() = gateType.calculator

	/** ---- [GraphElement] */

	override fun graphParamsChanged(graph: Graph) {
		super.graphParamsChanged(graph)
		(bitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> bitWidth = bw } }
	}

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
	    if (chosenInputCount.count > 1) {
		    writer.writeInt("inputCount", chosenInputCount.count)
	    }
        if (StringUtils.isNotEmpty(getOutput<DigitalSignal>().name)) {
            writer.writeString("outputName", getOutput<DigitalSignal>().name!!)
        }
	    writer.writeIntegers("negatedInputs", negatedInputPortIds)
	    if (bitWidth is BitWidthExpression || bitWidth.width != BitWidth.BW_1.width) {
		    bitWidth.write("bitWidth", writer)
	    }
	    for (i in 1..chosenInputCount.count) {
			if (StringUtils.isNotEmpty(getInput<DigitalSignal>(i).name)) {
				writer.writeString("inputName$i", getInput<DigitalSignal>(i).name!!)
			}
	    }
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
	    if (reader.hasAttribute("inputCount")) {
			// Backward compatibility: NotGate was stored without inputCount
		    setupInputCount(PortCount.of(reader.readInt("inputCount")))
	    }
        if (reader.hasAttribute("outputName")) {
            getOutput<DigitalSignal>().name = reader.readString("outputName")
        }
	    if (reader.hasAttribute("negatedInputs")) {
	    	reader.readIntegers("negatedInputs").forEach {
			    (getInput<DigitalSignal>(it) as DigitalPort).logic = Logic.NEGATIVE
		    }
	    }
	    if (reader.hasAttribute("bitWidth")) {
		    bitWidth = BitWidth.read("bitWidth", reader)
	    }
	    for (i in 1.. chosenInputCount.count) {
			val propName = "inputName$i"
		    if (reader.hasAttribute(propName)) {
			    getInput<DigitalSignal>(i).name = reader.readString(propName)
		    }
	    }
    }

	/** ---- [MultiSignalSource] */

	override val signalCount: Int get() = inputCount

	override fun getSignal(id: Int): DigitalSignal {
		val inputPort = getInput<DigitalSignal>(id) as DigitalPort
		return inputPort.logic.evaluate(inputPort.getIncomingSignal()!!)
	}

    /** ---- [Actor] interface */

    override fun executionStart(signalHandler: SignalHandler) {
        super.executionStart(signalHandler)
        requestActingAfter(signalHandler, propagationDelay.value / 2, createActorData(null))
    }

    /** ---- [AbstractLogicGate] */

	open fun createInputPort(): InputPort<DigitalSignal> = DigitalPortImpl.createInput(logic = Logic.POSITIVE, name = null, bitWidth = bitWidth)

	/** ---- [AbstractLogicGate] */

    /**
     * Called by setter [chosenInputCount] which establishes the required [InputPort] and a single [OutputPort],
     * which is created by this method.
     */
    protected open fun createOutputPort(): OutputPort<DigitalSignal> =
		DigitalPortImpl.createOutput(logic = gateType.outputLogic, name = null, bitWidth = bitWidth)

	fun getNegateInput(portId: Int): Boolean = (getInput<DigitalSignal>(portId) as DigitalPort).logic == Logic.NEGATIVE

	fun setNegateInput(portId: Int, value: Boolean) {
		if (value != getNegateInput(portId)) {
			(getInput<DigitalSignal>(portId) as DigitalPort).logic = Logic.negated(value)
		}
	}

	fun calculateTruthTable(): TruthTableModel {
		val inputColumns = mutableListOf<TruthTableModel.Column>()
		for (portId in 1..chosenInputCount.count) {
			val port = getInput<DigitalSignal>(portId) as DigitalPort
			inputColumns.add(TruthTableModel.Column("I$portId", port.logic))
		}
		return TruthTableModel(inputColumns, listOf("O")).calculate {
			(calculator as AbstractLogicGateCalculator).calculateMultiBit(it)
		}
	}


	private val negatedInputPortIds: List<Int> get() =
		getInputs()
			.map { it as DigitalPort }
			.filter { it.logic == Logic.NEGATIVE }
			.map { it.portId }
}