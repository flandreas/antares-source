package ch.scorpion.antares.model.output

import ch.scorpion.antares.model.Logic
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

abstract class AbstractSegmentDisplay<T : Vertice>(
	calculator: VerticeCalculator<T>
) : CalculatingVertice(calculator) {

	var logic: Logic = Logic.POSITIVE

	abstract fun inputValueOf(bitName: String): Boolean

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (logic != Logic.POSITIVE) {
			writer.writeString("logic", logic.customName)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("logic")) {
			logic = Logic.withName(reader.readString("logic"))
		}
	}
}