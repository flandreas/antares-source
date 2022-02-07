package ch.scorpion.jabbah.graph.model.param

import ch.scorpion.jabbah.io.*

/**
 * Default constructor must be "empty" in order to be readable as [Storable].
 */
class GraphParamDefinition<T : Any>(
	name: String = ""
) : AbstractStorable() {

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

	fun createValue(value: T): GraphParamValue<T> = type.createValue(name, value)

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

class GraphParamDefinitions : AbstractStorable(), Iterable<GraphParamDefinition<*>> {

	private var _definitions = mutableListOf<GraphParamDefinition<*>>()
	val definitions: Collection<GraphParamDefinition<*>> get() = _definitions

	val size: Int get() = _definitions.size

	val isEmpty: Boolean get() = _definitions.isEmpty()

	val isNotEmpty: Boolean get() = _definitions.isNotEmpty()

	fun get(index: Int): GraphParamDefinition<*> = _definitions[index]

	fun get(name: String): GraphParamDefinition<*>? = _definitions.firstOrNull { it.name == name }

	override fun iterator(): Iterator<GraphParamDefinition<*>> = _definitions.iterator()

	fun contains(name: String): Boolean = _definitions.any { it.name == name }

	fun withDefinition(def: GraphParamDefinition<*>): GraphParamDefinitions =
		GraphParamDefinitions().also { newDefs ->
			newDefs._definitions = _definitions.filter { it.name != def.name}.toMutableList()
			newDefs._definitions.add(def)
		}

	fun withReplacedDefinition(name: String, def: GraphParamDefinition<*>): GraphParamDefinitions =
		GraphParamDefinitions().also { newDefs ->
			newDefs._definitions = _definitions.filter { it.name != name}.toMutableList()
			newDefs._definitions.add(def)
		}

	fun withoutDefinition(name: String): GraphParamDefinitions =
		GraphParamDefinitions().also { newDefs ->
			newDefs._definitions = _definitions.filter { it.name != name}.toMutableList()
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
			_definitions.clear()
			_definitions.addAll(reader.readStorables("paramDefs"))
		}
	}
}