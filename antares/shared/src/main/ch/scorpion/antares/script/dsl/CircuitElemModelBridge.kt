package ch.scorpion.antares.script.dsl

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.script.AntaresScriptGateway
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.script.Script

open class CircuitElemModelBridge(
	val script: Script,
	private val vertice: Vertice,
	private val signalHandler: SignalHandler?,
	var data: GraphActorData?,
	private val store: AntaresScriptGateway.Store
) {

	/** Returns the model ID of this circuit element.*/
	open fun id(): Int = vertice.id

	/** Determines whether the value at the [InputPort] (lowest-priority bit) with the specified name has changed to logical 1.*/
	@Suppress("unused")
	fun portRaised(name: String): Boolean {
		return data!!.changedPort != null
			&& data!!.changedPort!!.name == name
			&& data!!.getSignal<DigitalSignal>(vertice.getInput<DigitalSignal>(name).portId)!!.bitAt(0).isSet
	}

	/** Returns the input signal at the first (or one and only) [InputPort] of this circuit element as a [String].*/
	fun input(): String = vertice.getInput<DigitalSignal>().getIncomingSignal().toString()

	/** Returns the input signal at the [InputPort] with the specified name as a [String].*/
	fun input(name: String): String = vertice.getInput<DigitalSignal>(name).getIncomingSignal().toString()

	/** Returns the input signal at the first (or one and only) [InputPort] of this circuit element as a [Word].*/
	@Suppress("unused")
	fun inputWord(): Word = Word(vertice.getInput<DigitalSignal>().getIncomingSignal() as ch.scorpion.antares.model.signal.Word)

	/** Returns the input signal at the [InputPort] with the specified name as a [Word].*/
	@Suppress("unused")
	fun inputWord(name: String): Word = Word(vertice.getInput<DigitalSignal>(name).getIncomingSignal() as ch.scorpion.antares.model.signal.Word)

	/** Returns the input signal at the [InputPort] with the specified ID as a [String].*/
	fun input(id: Int): String = vertice.getInput<DigitalSignal>(id).getIncomingSignal().toString()

	/** Returns the lowest-priority [Bit] input signal at the [InputPort] with the specified name as a [Boolean].*/
	@Suppress("unused")
	fun inputBit(name: String): Boolean = vertice.getInput<DigitalSignal>(name).getIncomingSignal()!!.bitAt(0).isSet

	fun inputBit(id: Int): Boolean = vertice.getInput<DigitalSignal>(id).getIncomingSignal()!!.bitAt(0).isSet

	fun inputBit(): Boolean = inputBit(1)

	/** Returns the output signal at the first (or one and only) [OutputPort] of this circuit element as a [String].*/
	fun output(): String = vertice.getOutput<DigitalSignal>().getOutgoingSignal().toString()

	/** Returns the output signal at the [OutputPort] with the specified ID as a [String].*/
	fun output(id: Int): String = vertice.getOutput<DigitalSignal>(id).getOutgoingSignal().toString()

	/** Returns the output signal of the first (or one and only) [OutputPort] as a [Word].*/
	@Suppress("unused")
	fun outputWord(): Word = Word(vertice.getOutput<DigitalSignal>().getOutgoingSignal() as ch.scorpion.antares.model.signal.Word)

	/** Returns the output signal at the [OutputPort] with the specified ID as a [Word].*/
	@Suppress("unused")
	fun outputWord(id: Int): Word = Word(vertice.getOutput<DigitalSignal>(id).getOutgoingSignal() as ch.scorpion.antares.model.signal.Word)

	/** Returns the output signal at the [OutputPort] with the specified name as a [Word].*/
	@Suppress("unused")
	fun outputWord(name: String): Word = Word(vertice.getOutput<DigitalSignal>(name).getOutgoingSignal() as ch.scorpion.antares.model.signal.Word)

	/** Sets the output signal of the first (or one and only) [OutputPort] to the specified hex value.*/
	@Suppress("unused")
	fun setOutput(hexValue: String) {
		setOutput(vertice.getOutput(), hexValue)
	}

	/** Sets the output signal of the [OutputPort] with ID [id] to the specified hex value.*/
	fun setOutput(id: Int, hexValue: String) {
		setOutput(vertice.getOutput(id), hexValue)
	}

	/** Sets the output signal of the [OutputPort] with the given name to the specified hex value.*/
	@Suppress("unused")
	fun setOutput(name: String, hexValue: String) {
		setOutput(vertice.getOutput(name), hexValue)
	}

	@Suppress("unused")
	fun setOutputWord(word: Word) {
		vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(word.word, signalHandler!!)
	}

	@Suppress("unused")
	fun setOutputWord(name: String, word: Word) {
		vertice.getOutput<DigitalSignal>(name).setOutgoingSignalBuffered(word.word, signalHandler!!)
	}

	@Suppress("unused")
	fun setOutputWord(id: Int, word: Word) {
		val outputPort = vertice.getOutput<DigitalSignal>(id) as DigitalPort
		outputPort.setOutgoingSignalBuffered(word.word, signalHandler!!)
	}

	/** Sets the output signal of the first (or one and only) [OutputPort] to the specified boolean value.*/
	@Suppress("unused")
	fun setOutputBit(bit: Boolean) {
		vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(ch.scorpion.antares.model.signal.Word.of(bit), signalHandler!!);
	}

	@Suppress("unused")
	fun setOutputBit(name: String, bit: Boolean) {
		val outputPort = vertice.getOutput<DigitalSignal>(name) as DigitalPort
		outputPort.setOutgoingSignalBuffered(ch.scorpion.antares.model.signal.Word.of(bit), signalHandler!!)
	}

	fun setOutputBit(id: Int, bit: Boolean) {
		val outputPort = vertice.getOutput<DigitalSignal>(id) as DigitalPort
		outputPort.setOutgoingSignalBuffered(ch.scorpion.antares.model.signal.Word.of(bit), signalHandler!!)
	}

	/** Checks whether any [Bit] in the specified [Word] is undefined.*/
	@Suppress("unused")
	fun anyBitUndefined(word: Word): Boolean {
		return word.word.containsUndefinedBit()
	}

	@Suppress("unused")
	fun store(name: String, value: Word) {
		store.put(vertice, name, value.word)
	}

	@Suppress("unused")
	fun load(name: String): Word? = store.get(vertice, name)?.let { Word(it) }

	/** Creates a new [Word] with the specified hexadecimal value.*/
	@Suppress("unused")
	fun hexWord(value: String, bitWidth: Int): Word {
		return Word(ch.scorpion.antares.model.signal.Word.of(BitWidth.of(bitWidth), BitOperation.hexToLong(value)))
	}

	@Suppress("unused")
	fun undefinedWord(bitWidth: Int): Word {
		return Word(ch.scorpion.antares.model.signal.Word.undefined(BitWidth.of(bitWidth)))
	}

	private fun setOutput(port: OutputPort<DigitalSignal>, hexValue: String) {
		val digitalPort = port as DigitalPort
		val signal = ch.scorpion.antares.model.signal.Word.of(digitalPort.bitWidth, hexValue)
		digitalPort.setOutgoingSignalBuffered(signal, signalHandler!!)
	}
}