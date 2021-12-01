package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.graph.model.param.GraphParamType
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Defines [BitWidth] as [GraphParamType].
 */
object BitWidthGraphParamType : GraphParamType<BitWidth> {

	override val name: String = "bitWidth"

	override fun writeValue(name: String, value: BitWidth, writer: StoreWriter) {
		writer.writeString(name, value.customName)
	}

	override fun readValue(name: String, reader: StoreReader): BitWidth =
		BitWidth.withName(reader.readString(name))
}