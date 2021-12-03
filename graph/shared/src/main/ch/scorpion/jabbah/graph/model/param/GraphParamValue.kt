package ch.scorpion.jabbah.graph.model.param

import ch.scorpion.jabbah.io.*

class GraphParamValue<T : Any> : Storable {
	lateinit var name: String
	lateinit var type: GraphParamType<T>
	lateinit var value: T

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeString("name", name)
		writer.writeString("type", type.name)
		type.writeValue("value", value, writer)
	}

	override fun read(reader: StoreReader) {
		name =reader.readString("name")
		type = GraphParamTypeRegistry.get(reader.readString("type"))
		value = type.readValue("value", reader)
	}
}

class GraphParamValues : Storable {

	private val values = mutableListOf<GraphParamValue<*>>()

	val isEmpty: Boolean get() = values.isEmpty()

	val isNotEmpty: Boolean get() = values.isNotEmpty()

	fun withName(name: String): GraphParamValue<*>? = values.firstOrNull { it.name == name }

	fun add(value: GraphParamValue<*>) {
		if (values.any { it.name == value.name}) {
			throw IllegalArgumentException("name '${value.name}' already exists")
		}
		values.add(value)
	}

	/** ---- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		if (isNotEmpty) {
			writer.writeStorables("values", values.iterator())
		}
	}

	override fun read(reader: StoreReader) {
		if (reader.hasElement("values")) {
			values.clear()
			values.addAll(reader.readStorables("values"))
		}
	}
}