package ch.scorpion.jabbah.graph.model.graph

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.param.GraphParamType
import ch.scorpion.jabbah.graph.model.param.GraphParamValue
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.reflect.KClass

object StringGraphParamType : GraphParamType<String> {

	override val name: String = "String"

	override val displayableName: String by lazy { Translations.getString("graph.paramType.string.name") }

	override val valueClass: KClass<String>
		get() = String::class

	override fun toString(): String = displayableName

	override fun createValue(name: String, value: String): GraphParamValue<String> =
		GraphParamValue.create(name, this, value)

	override fun writeValue(name: String, value: String, writer: StoreWriter) {
		writer.writeString(name, value)
	}

	override fun readValue(name: String, reader: StoreReader): String =
		reader.readString(name)

	override fun toDslValue(value: String): Any = value

	override fun evaluateIn(graph: Graph, value: String): String = value
}