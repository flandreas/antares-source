package ch.scorpion.jabbah.graph.model.oscilloscope

import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.AbstractVertice
import ch.scorpion.jabbah.graph.model.GraphElementListener
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortFactory
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A [Vertice] that collects signals from multiple [OscilloscopeProbeVertice]s.
 * [Oscilloscope] has a variable amount of [InputPort]s. Changes of values at the [InputPort]
 * are not processed through the [SignalHandler], but directly communicated to registered [GraphElementListener]s.
 *
 * TODO Refactoring: Split PortFactory into model and view part (module dependency)
 */
class Oscilloscope(
        private val portFactory: PortFactory = GraphViewModule.portFactory
) : AbstractVertice() {

    companion object {
        private val LOG by logger(Oscilloscope::class)
    }

    /** Maps a probe row number (starting with "1") to its [SignalHistory].*/
    private val signalHistories = mutableMapOf<String,SignalHistoryImpl<Any>>()

    /** Holds the largest time of all [SignalHistoryEntry] in all [SignalHistories][SignalHistory].*/
    var maxTime: Long = 0
        private set

    /** ---- [AbstractVertice] */

    override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler) {
        storeSignal(input, signalHandler)
        stateChanged(signalHandler)
    }

    /** ---- [Storable] */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeInt("portsCount", portsCount)
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        for (i in 1..reader.readInt("portsCount")) {
            val port = portFactory.createPort<Any>(PortType.INPUT)
            port.name = i.toString()
            addPort(port)
        }
    }

    /** ---- [Actor] interface */

    override fun executionStarted(signalHandler: SignalHandler) {
        signalHistories.clear()
        maxTime = 0
        getPorts().forEach { signalHistories.put(it.name!!, SignalHistoryImpl()) }
        super.executionStarted(signalHandler)
    }

    override fun executionStopped(signalHandler: SignalHandler) {
        signalHistories.clear()
        super.executionStopped(signalHandler)
    }

    /** ---- [Oscilloscope] */

    fun getSignalHistory(rowNumber: String): SignalHistory<Any>? {
        return signalHistories.get(rowNumber)
    }

    /**
     * Removes all entries from all [SignalHistories][SignalHistory] of this [Oscilloscope]
     * that are older than the specified time.
     */
    fun truncate(time: Long) {
        signalHistories.forEach { it.value.truncate(time) }
    }

    private fun storeSignal(input: InputPort<*>, signalHandler: SignalHandler) {
        val signal = input.getIncomingSignal()!!
        LOG.debug("Oscilloscope ${input.name}: storing signal '$signal' at time ${signalHandler.executionTime}")
        signalHistories.get(input.name!!)!!.add(SignalHistoryEntry(signal, signalHandler.executionTime))
        maxTime = Math.max(maxTime, signalHandler.executionTime)
    }
}