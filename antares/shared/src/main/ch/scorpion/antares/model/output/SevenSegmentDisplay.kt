package ch.scorpion.antares.model.output

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class SevenSegmentDisplay(portScheme: SevenSegmentDisplayScheme) : CalculatingVertice(CALCULATOR) {
    constructor(): this(SevenSegmentDisplayScheme.COMBINED)

    companion object {
        val CALCULATOR = object : VerticeCalculator<SevenSegmentDisplay> {
            override fun calculate(vertice: SevenSegmentDisplay, data: GraphActorData, signalHandler: SignalHandler) {
                vertice.stateChanged()
            }
        }
    }

    var portScheme: SevenSegmentDisplayScheme = portScheme
        set(value) {
            if (value != field) {
                field = value
                clearPorts()
                field.createPorts(this)
                stateChanged()
            }
        }

    init {
        portScheme.createPorts(this)
    }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("portScheme", portScheme.customName)
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        portScheme = SevenSegmentDisplayScheme.withName(reader.readString("portScheme"))
    }
}