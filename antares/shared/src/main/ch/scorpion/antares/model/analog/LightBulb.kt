package ch.scorpion.antares.model.analog

import ch.scorpion.jabbah.graph.model.vertice.EmptyVerticeCalculator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Treating [LightBulb] as element with constant resistance, i.e. independent of changing
 * temperature when the current increases.
 */
class LightBulb : AbstractResistingAnalogVertice<LightBulb>(DEF_RESISTANCE, EmptyVerticeCalculator, "library.element.LightBulb") {

	companion object {
		private const val DEF_RESISTANCE = 20.0
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("resistance")) {
			// Backward compatability due to bug #740
			resistance = reader.readDouble("resistance")
		}
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeDouble("resistance", resistance)
	}
}