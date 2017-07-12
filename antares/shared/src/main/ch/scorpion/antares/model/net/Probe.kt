package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalSource
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.base.logger

/**
 * Displays the value of a [DigitalSignal] within a circuit.
 */
class Probe(hasOutput: Boolean = false) : CalculatingVertice(CALCULATOR), DigitalSignalSource {

    companion object {
        val LOG by logger()
        val CALCULATOR = object : VerticeCalculator<Probe> {
            override fun calculate(vertice: Probe, data: GraphActorData, signalHandler: SignalHandler) {
                if (vertice.isLogging) {
                    LOG.info("${signalHandler.executionTime} Probe: ${data.getSignal<DigitalSignal>(1)}")
                }
                vertice.setSignal(data.getSignal<DigitalSignal>(1)!!, signalHandler)
            }
        }
    }

    /** Write a log message whenever the input changes */
    var isLogging: Boolean = false

    override var bitWidth: BitWidth
        get() = (getInput<DigitalSignal>() as DigitalPort).bitWidth
        set(value) {
            if (value != bitWidth) {
                (getInput<DigitalSignal>() as DigitalPort).bitWidth = value
                if (hasOutput) {
                    (getOutput<DigitalSignal>() as DigitalPort).bitWidth = value
                }
                stateChanged()
            }
        }

    var hasOutput: Boolean
        get() = outputCount > 0
        set(value) {
            if (value == hasOutput) {
                return
            }
            if (value) {
                val output = DigitalPortImpl.createOutput()
                output.bitWidth = bitWidth
                addPort(output)
            } else {
                removePort(getOutput<DigitalSignal>())
            }
        }

    init {
        addPort(DigitalPortImpl.createInput())
        this.hasOutput = hasOutput
    }

    /** ---- [DigitalSignalSource] interface */

    override var signal: DigitalSignal?
        get() = getInput<DigitalSignal>().getIncomingSignal()
        set(value) {throw UnsupportedOperationException()}

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeInt("bitWidth", bitWidth.width);
        writer.writeBoolean("hasOutput", hasOutput);
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        bitWidth = BitWidth.of(reader.readInt("bitWidth"))
        hasOutput = reader.readBoolean("hasOutput")
    }

    /** ---- [Proble] */

    private fun setSignal(signal: DigitalSignal, signalHandler: SignalHandler) {
        stateChanged()
        if (outputCount > 0) {
            getOutput<DigitalSignal>().setOutgoingSignalBuffered(signal, signalHandler)
        }
    }

}