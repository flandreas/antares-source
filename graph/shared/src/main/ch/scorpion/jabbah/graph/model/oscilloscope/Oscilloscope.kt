package ch.scorpion.jabbah.graph.model.oscilloscope

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
 * TODO Refactoring: Split PortFactory into model and view part (module dependecy)
 */
class Oscilloscope(
        private val portFactory: PortFactory = GraphViewModule.portFactory
) : AbstractVertice() {

    companion object {
        private val LOG by logger(Oscilloscope::class)
    }

    /** Maps a probe row number (starting with "1") to its [SignalHistory].*/
    private val signalHistories = mutableMapOf<String,SignalHistory<Any>>()

    /** ---- [AbstractVertice] */

    override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler) {
        LOG.debug("Oscilloscope: inputChanged of rowNumber ${input.name}")
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
        getPorts().forEach { signalHistories.put(it.name!!, SignalHistory()) }
    }

    override fun executionStopped(signalHandler: SignalHandler) {
        signalHistories.clear()
    }

    /** ---- [Oscilloscope] */

    fun getSignalHistory(rowNumber: String): SignalHistory<Any>? {
        return signalHistories.get(rowNumber)
    }

    private fun storeSignal(input: InputPort<*>, signalHandler: SignalHandler) {
        signalHistories.get(input.name!!)!!.add(
                SignalHistoryEntry(input.getIncomingSignal()!!, signalHandler.executionTime)
        )
    }
}