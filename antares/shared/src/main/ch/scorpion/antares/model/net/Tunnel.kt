package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.GraphActorDataImpl
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.vertice.AbstractVertice
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.base.logger

/**
 * A [Tunnel] forwards a signal to other [Tunnel]s with the same name without the
 * need to explicitly connect them by a [Net].
 * [Tunnel]s with the same name are only connected within the same [Graph].
 * The owning [Graph] will be informed by [stateChanged()], which gets already
 * called by [AbstractVertice.inputChanged].
 */
class Tunnel(
    name: String? = null
) : CalculatingVertice(CALCULATOR) {

    companion object {
        private val LOG by logger(Tunnel::class)
        val CALCULATOR = object : VerticeCalculator<Tunnel> {
            override fun calculate(vertice: Tunnel, data: GraphActorData, signalHandler: SignalHandler) {
                (vertice.getPort<DigitalSignal>() as DigitalPort).isOutputDominant = true
                LOG.debug("Calculate Tunnel ${vertice.id} with signal '${data.getSignal<DigitalSignal>(1)}', outputDominant = ${(vertice.getPort<DigitalSignal>() as DigitalPort).isOutputDominant}")
                vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(data.getSignal(1), signalHandler)
            }
        }
    }

    init {
        this.name = name
        addPort(DigitalPortImpl.createInOut())
    }

    var bitWidth: BitWidth
        get() = (getOutput<DigitalSignal>() as DigitalPort).bitWidth
        set(newValue) {
            if (newValue != bitWidth) {
                (getOutput<DigitalSignal>() as DigitalPort).bitWidth = newValue
                stateChanged()
            }
        }

    /** ---- [AbstractVertice] */

    override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler) {
        (getPort<DigitalSignal>() as DigitalPort).isOutputDominant = false
        stateChanged(signalHandler)
    }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeInt("bitWidth", bitWidth.width)
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        bitWidth = BitWidth.of(reader.readInt("bitWidth"))
    }

    /** ---- [Actor] */

    override fun executionStarted(signalHandler: SignalHandler) {
        super.executionStarted(signalHandler)
        val signal = Word.allOf(bitWidth, Bit.Undefined)
        requestActingAfter(signalHandler, propagationDelay, GraphActorDataImpl(null, signal))
    }

    /** ---- [Tunnel] */

    /**
     * Called by the owning [Graph] after detection of a signal change from a [Tunnel]
     * with the same name like this [Tunnel].
     */
    fun setSignal(signal: DigitalSignal, signalHandler: SignalHandler) {
        if (signal != getOutput<DigitalSignal>().getOutgoingSignal()) {
            LOG.debug("Tunnel $id: setSignal '$signal'")
            requestActingAfter(signalHandler, propagationDelay, GraphActorDataImpl(null, signal))
        }
    }

    fun getIncomingSignal(): DigitalSignal {
        return getInput<DigitalSignal>().getIncomingSignal()!!
    }

    fun getOutgoingSignal(): DigitalSignal {
        return getOutput<DigitalSignal>().getOutgoingSignal()!!
    }

    fun getInOrOutSignal(): DigitalSignal {
        if ((getIncomingSignal() as Word).isAllOf(Bit.Undefined)) {
            return getOutgoingSignal()
        }
        return getIncomingSignal()
    }
}
