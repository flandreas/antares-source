package ch.scorpion.jabbah.graph.model.graph

import ch.scorpion.jabbah.graph.model.GraphParamType
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

object StringGraphParamType : GraphParamType<String> {

	override val name: String = "String"

	override fun writeValue(name: String, value: String, writer: StoreWriter) {
		writer.writeString(name, value)
	}

	override fun readValue(name: String, reader: StoreReader): String =
		reader.readString(name)
}