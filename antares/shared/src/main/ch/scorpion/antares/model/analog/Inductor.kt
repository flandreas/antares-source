package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.engine.AnalogElementMixin
import ch.scorpion.jabbah.graph.model.vertice.EmptyVerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class Inductor(
    inductance: Double = DEF_INDUCTANCE
) : AbstractAnalogTwoPortVertice<Inductor>(
    EmptyVerticeCalculator,
    "library.element.Inductor",
    AnalogElementMixin(true)
) {

    companion object {
        /** The default inductance for new [Inductor]s (in microhenry)*/
        private const val DEF_INDUCTANCE = 10.0
    }

    /** The inductance of this [Inductor] in microhenry.*/
    var inductance: Double = inductance
        set(value) {
            if (field != value) {
                field = value
                stateChanged(reason = MAIN_PROPERTY_STATE)
            }
        }

    /** ---- [Storable] interface */

    override fun read(reader: StoreReader) {
        super.read(reader)
        inductance = reader.readDouble("inductance")
    }

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeDouble("inductance", inductance)
    }
}