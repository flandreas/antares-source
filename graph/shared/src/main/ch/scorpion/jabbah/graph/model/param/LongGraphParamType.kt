package ch.scorpion.jabbah.graph.model.param

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.reflect.KClass

object LongGraphParamType : GraphParamType<Long> {

	override val name: String get() = "Long"

	override val displayableName: String by lazy { Translations.getString("graph.paramType.long.name") }

	override val valueClass: KClass<Long> get() = Long::class

	override fun toString(): String = displayableName

	override fun writeValue(name: String, value: Long, writer: StoreWriter) {
		writer.writeLong(name, value)
	}

	override fun readValue(name: String, reader: StoreReader): Long =
		reader.readLong(name)

	override fun createValue(name: String, value: Long): GraphParamValue<Long> =
		GraphParamValue.create(name, this, value)

	override fun toDslValue(value: Long): Any = value

	override fun evaluateIn(graph: Graph, value: Long): Long = value
}