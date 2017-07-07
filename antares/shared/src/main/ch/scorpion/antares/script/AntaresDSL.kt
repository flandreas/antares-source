package ch.scorpion.antares.script

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.GraphInput
import ch.scorpion.jabbah.graph.model.GraphOutput
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.base.logger

/**
 * Bridge to access {@link CircuitView} information from javascript code.
 * Defines a kind of DSL.
 */

class CircuitViewBridge(
        private val view: DrawingView<GraphView<GraphElementView<*>>>,
        private val signalHandler: SignalHandler?
) {

    companion object {
        val LOG by logger()
    }

    /** Returns the name of the circuit model.*/
    fun name(): String = view.drawing.graph!!.name

    /** Returns the signal of the [GraphInput] with the specified name as a [String].*/
    fun input(name: String): String = view.drawing.graph!!.getGraphInput<DigitalSignal>(name)!!.getOutput<DigitalSignal>().getOutgoingSignal().toString()

    /** Returns the signal of the [GraphOutput] with the specified name as a [String].*/
    fun output(name: String): String = view.drawing.graph!!.getGraphOutput<DigitalSignal>(name)!!.getInput<DigitalSignal>().getIncomingSignal().toString()

    /** Returns the circuit element with the specified ID.*/
    @Suppress("unused")
    fun elem(id: Int): CircuitElementViewBridge = CircuitElementViewBridge(
        view.drawing.getWidthId(id)!! as VerticeView, signalHandler)

    /** Returns the ID of the current [Scenario], or an empty [String] if none is active.*/
    fun scenario(): String {
        val scenario = view.drawing.currentScenario
        return scenario?.id.toString()
    }

    /** Highlights the circuit elements with the specified IDs.*/
    fun highlight(vararg ids: Int) {
        view.highlighter.highlight(*ids)
    }

    /** Removes all highlights from the circuit.*/
    @Suppress("unused")
    fun unhighlight() {
        view.highlighter.unhighlightAll()
    }
}

open class CircuitElemModelBridge(
        private val vertice: Vertice,
        private val signalHandler: SignalHandler?,
        private val data: GraphActorData?,
        @Suppress("unused") private val store: AntaresScriptGateway.Store?
) {

    /** Returns the model ID of this circuit element.*/
    open fun id(): Int = vertice.id

    /** Determines whether the value at the [InputPort] (lowest-priority bit) with the specified name has changed to logical 1.*/
    @Suppress("unused")
    fun portRaised(name: String): Boolean {
        return data!!.changedPort != null
            && data.changedPort!!.name == name
            && data.getSignal<DigitalSignal>(vertice.getInput<DigitalSignal>(name).portId)!!.bitAt(0).isSet
    }

    /** Returns the input signal at the first (or one and only) [InputPort] of this circuit element as a [String].*/
    fun input(): String = vertice.getInput<DigitalSignal>().getIncomingSignal().toString()

    /** Returns the input signal at the [InputPort] with the specified name as a [String].*/
    fun input(name: String): String = vertice.getInput<DigitalSignal>(name).getIncomingSignal().toString()

    /** Returns the input signal at the first (or one and only) [InputPort] of this circuit element as a [Word].*/
    @Suppress("unused")
    fun inputWort(): Word = vertice.getInput<DigitalSignal>().getIncomingSignal() as Word

    /** Returns the input signal at the [InputPort] with the specified name as a [Word].*/
    @Suppress("unused")
    fun inputWord(name: String): Word = vertice.getInput<DigitalSignal>(name).getIncomingSignal() as Word

    /** Returns the input signal at the [InputPort] with the specified ID as a [String].*/
    fun input(id: Int): String = vertice.getInput<DigitalSignal>(id).getIncomingSignal().toString()

    /** Returns the lowest-priority [Bit] input signal at the [InputPort] with the specified name as a [Boolean].*/
    @Suppress("unused")
    fun inputBit(name: String): Boolean = vertice.getInput<DigitalSignal>(name).getIncomingSignal()!!.bitAt(0).isSet

    /** Returns the output signal at the first (or one and only) [OutputPort] of this circuit element as a [String].*/
    fun output(): String = vertice.getOutput<DigitalSignal>().getOutgoingSignal().toString()

    /** Returns the output signal at the [OutputPort] with the specified ID as a [String].*/
    fun output(id: Int): String = vertice.getOutput<DigitalSignal>(id).getOutgoingSignal().toString()

    /** Returns the output signal of the first (or one and only) [OutputPort] as a [Word].*/
    @Suppress("unused")
    fun outputWord(): Word = vertice.getOutput<DigitalSignal>().getOutgoingSignal() as Word

    /** Returns the output signal at the [OutputPort] with the specified ID as a [Word].*/
    @Suppress("unused")
    fun outputWord(id: Int): Word = vertice.getOutput<DigitalSignal>(id).getOutgoingSignal() as Word

    /** Returns the output signal at the [OutputPort] with the specified name as a [Word].*/
    @Suppress("unused")
    fun outputWord(name: String): Word = vertice.getOutput<DigitalSignal>(name).getOutgoingSignal() as Word

    /** Sets the output signal of the first (or one and only) [OutputPort] to the specified hex value.*/
    @Suppress("unused")
    fun setOutput(hexValue: String) {
        setOutput(1, hexValue)
    }

    /** Sets the output signal of the [OutputPort] with ID [id] to the specified hex value.*/
    fun setOutput(id: Int, hexValue: String) {
        setOutput(vertice.getOutput<DigitalSignal>(id), hexValue)
    }

    /** Sets the output signal of the [OutputPort] with the given name to the specified hex value.*/
    @Suppress("unused")
    fun setOutput(name: String, hexValue: String) {
        setOutput(vertice.getOutput<DigitalSignal>(name), hexValue)
    }

    @Suppress("unused")
    fun setOutputWord(word: Word) {
        setOutputWord(1, word)
    }

    @Suppress("unused")
    fun setOutputWord(name: String, word: Word) {
        vertice.getOutput<DigitalSignal>(name).setOutgoingSignalBuffered(word, signalHandler!!)
    }

    fun setOutputWord(id: Int, word: Word) {
        val outputPort = vertice.getOutput<DigitalSignal>(id) as DigitalPort
        outputPort.setOutgoingSignalBuffered(word, signalHandler!!)
    }

    @Suppress("unused")
    fun setOutputBit(name: String, bit: Boolean) {
        val outputPort = vertice.getOutput<DigitalSignal>(name) as DigitalPort
        outputPort.setOutgoingSignalBuffered(Word.of(bit), signalHandler!!)
    }

    /** Checks whether any [Bit] in the specified [Word] is undefined.*/
    @Suppress("unused")
    fun anyBitUndefined(word: Word): Boolean {
        return word.containsUndefinedBit()
    }

    private fun setOutput(port: OutputPort<DigitalSignal>, hexValue: String) {
        val digitalPort = port as DigitalPort
        val signal = Word.of(digitalPort.bitWidth, hexValue)
        digitalPort.setOutgoingSignalBuffered(signal, signalHandler!!)
    }

}

class CircuitElementViewBridge(
    private val verticeView: VerticeView<*>,
    signalHandler: SignalHandler?
) : CircuitElemModelBridge(verticeView.model!!, signalHandler, null, null) {

    /** Returns the ID of the circuit element view.*/
    override fun id(): Int = verticeView.id
}