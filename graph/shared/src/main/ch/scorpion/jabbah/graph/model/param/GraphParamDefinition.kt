package ch.scorpion.jabbah.graph.model.param

import ch.scorpion.jabbah.io.*


/**
 * Default constructor must be "empty" in order to be readable as [Storable].
 */
class GraphParamDefinition<T : Any>(
	name: String = ""
) : Storable {

	companion object {
		fun <T : Any> create(
			name: String,
			type: GraphParamType<T>,
			defaultValue: T
		) : GraphParamDefinition<T> {
			val definition = GraphParamDefinition<T>(name)
			definition.type = type
			definition.defaultValue = defaultValue
			return definition
		}
	}

	var name: String = name

	lateinit var type: GraphParamType<T>

	lateinit var defaultValue: T

	fun createDefaultValue(): GraphParamValue<T> = type.createValue(name, defaultValue)

	/** ---- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeString("name", name)
		writer.writeString("type", type.name)
		type.writeValue("defaultValue", defaultValue, writer)
	}

	override fun read(reader: StoreReader) {
		name = reader.readString("name")
		type = GraphParamTypeRegistry.get(reader.readString("type"))
		defaultValue = type.readValue("defaultValue", reader)
	}
}

class GraphParamDefinitions : Storable {

	private val definitions = mutableListOf<GraphParamDefinition<*>>()

	val size: Int get() = definitions.size

	val isEmpty: Boolean get() = definitions.isEmpty()

	val isNotEmpty: Boolean get() = definitions.isNotEmpty()

	fun iterator(): Iterator<GraphParamDefinition<*>> = definitions.iterator()

	fun contains(name: String): Boolean = definitions.any { it.name == name }

	fun withName(name: String): GraphParamDefinition<*>? = definitions.firstOrNull { it.name == name }

	fun add(definition: GraphParamDefinition<*>) {
		if (definitions.any { it.name == definition.name }) {
			throw IllegalArgumentException("name '${definition.name}' already exists")
		}
		definitions.add(definition)
	}

	/** ---- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		if (isNotEmpty) {
			writer.writeStorables("paramDefs", definitions.iterator())
		}
	}

	override fun read(reader: StoreReader) {
		if (reader.hasElement("paramDefs")) {
			definitions.clear()
			definitions.addAll(reader.readStorables("paramDefs"))
		}
	}
}