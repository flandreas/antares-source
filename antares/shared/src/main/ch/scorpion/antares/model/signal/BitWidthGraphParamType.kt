package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.graph.model.param.GraphParamType
import ch.scorpion.jabbah.graph.model.param.GraphParamValue
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.reflect.KClass

/**
 * Defines [BitWidth] as [GraphParamType].
 */
object BitWidthGraphParamType : GraphParamType<BitWidth> {

	override val name: String = "bitWidth"

	override val valueClass: KClass<BitWidth> get() = BitWidth::class

	override fun createValue(name: String, value: BitWidth): GraphParamValue<BitWidth> =
		GraphParamValue.create(name, this, value)

	override fun writeValue(name: String, value: BitWidth, writer: StoreWriter) {
		writer.writeString(name, value.customName)
	}

	override fun readValue(name: String, reader: StoreReader): BitWidth =
		BitWidth.withName(reader.readString(name))
}