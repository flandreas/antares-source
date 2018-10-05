package ch.scorpion.antares.model.output

import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.io.Storable

/**
 * A light emitting [Vertice] that turns on with [Bit.True].
 */
class LED(var text: String? = null) : CalculatingVertice("library.element.LED", CALCULATOR) {

    companion object {
        val CALCULATOR = object : VerticeCalculator<LED> {
            override fun calculate(vertice: LED, data: GraphActorData, signalHandler: SignalHandler) {
                vertice.isOn = (data.getSignal<DigitalSignal>(1) as Word).bitAt(0) == Bit.True
            }
        }
    }

    var isOn: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                stateChanged()
            }
        }

    init {
        addPort(DigitalPortImpl.createInput())
        propagationDelay = 0
    }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        if (StringUtils.isNotEmpty(text)) {
            writer.writeString("text", text!!)
        }
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        if (reader.hasAttribute("text")) {
            text = reader.readString("text")
        }
    }

    /** ---- [Actor] */

    override fun executionStopped(signalHandler: SignalHandler) {
        super.executionStopped(signalHandler)
        isOn = false
    }
}