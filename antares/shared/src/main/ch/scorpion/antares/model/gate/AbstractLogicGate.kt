package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.model.truthtable.TruthTableModel
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

abstract class AbstractLogicGateCalculator : VerticeCalculator<AbstractLogicGate> {

	fun calculateMultiBit(source: MultiSignalSource<DigitalSignal>, filter: (portId: Int) -> Boolean = { true }): DigitalSignal {
		val inputValues = (1..source.signalCount)
			.filter(filter)
			.map { effectiveGateInputValue(it, source) }

		val outputBits = mutableListOf<Bit>()
		for (bitIndex in 0 until inputValues.first().bitWidth.width) {
			outputBits.add(calculateBit(inputValues, bitIndex))
		}

		return DigitalSignalFactory.ofBits(outputBits)
	}

	fun calculateSingleBit(source: MultiSignalSource<DigitalSignal>, filter: (portId: Int) -> Boolean = { true }): DigitalSignal =
		DigitalSignalFactory.of(calculateBit(
			(1..source.signalCount)
				.filter(filter)
				.map { DigitalSignalFactory.of(effectiveGateInputBit(source.getSignal(it).bitAt(0))) },
			0))

	abstract fun calculateBit(input: Collection<DigitalSignal>, bitIndex: Int): Bit

	/** The fast lane for BitWidth 1 inputs.*/
	abstract fun calculateBit(input: MultiSignalSource<DigitalSignal>): Bit

	override fun calculate(vertice: AbstractLogicGate, data: GraphActorData, signalHandler: SignalHandler) {
		if (vertice.bitWidth.width == BitWidth.BW_1.width) {
			vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(DigitalSignalFactory.of(calculateBit(vertice)), signalHandler)
		} else {
			vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(calculateMultiBit(vertice), signalHandler)
		}
	}
}

fun effectiveGateInputBit(bit: Bit): Bit =
	if (bit == Bit.Undefined) {
		CurrentUndefinedGateInputBehavior.value.definedBit
	} else {
		bit
	}

fun effectiveGateInputWord(input: DigitalSignal): DigitalSignal =
	DigitalSignalFactory.ofBits(input.bits.map { bit -> effectiveGateInputBit(bit) })

fun effectiveGateInputValue(portId: Int, source: MultiSignalSource<DigitalSignal>): DigitalSignal =
	effectiveGateInputWord(source.getSignal(portId))

/**
 * A digital gate is a [Vertice] that performs a basic logical operation on [DigitalSignal]s, whose number
 * of [InputPort]s can be chosen by the user up to a certain limit.
 */
abstract class AbstractLogicGate(
	calculator: AbstractLogicGateCalculator,
	inputCount: PortCount,
	bitWidth: BitWidth = BitWidth.BW_1,
	val minInputCount: PortCount = DEF_MIN_INPUT_COUNT,
	val maxInputCount: PortCount = DEF_MAX_INPUT_COUNT
) : CalculatingVertice(calculator), MultiSignalSource<DigitalSignal> {

    companion object {
        val LOG by logger(AbstractLogicGate::class)
	    const val DEFAULT_PROPAGATION_DELAY = 20L
        val DEF_MIN_INPUT_COUNT = PortCount.TWO
        val DEF_MAX_INPUT_COUNT = PortCount.EIGHT
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

	/** ---- [GraphElement] */

	override fun graphParamsChanged(graph: Graph) {
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
	    if (bitWidth.width != BitWidth.BW_1.width) {
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
        requestActingAfter(signalHandler, propagationDelay / 2, createActorData(null))
    }

    /** ---- [AbstractLogicGate] */

	open fun createInputPort(): InputPort<DigitalSignal> = DigitalPortImpl.createInput(logic = Logic.POSITIVE, name = null, bitWidth = bitWidth)

	/** ---- [AbstractLogicGate] */

    /**
     * Called by setter [chosenInputCount] which establishes the required [InputPort] and a single [OutputPort],
     * which is created by this method.
     */
    protected open fun createOutputPort(): OutputPort<DigitalSignal> = DigitalPortImpl.createOutput(logic = Logic.POSITIVE, name = null, bitWidth = bitWidth)

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