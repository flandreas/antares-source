package ch.scorpion.jabbah.graph.model.param

import ch.scorpion.jabbah.io.*

/**
 * Holds an quasi-immutable named value of a [GraphParamType].
 * Quasi-immutable means immutable interface, but for technical reasons updated during deserialization.
 */
class GraphParamValue<T : Any> : Storable {

	companion object {
		fun <T : Any> create(name: String, type: GraphParamType<T>, value: T): GraphParamValue<T> {
			return GraphParamValue<T>().also {
				it._name = name
				it._type = type
				it._value = value
			}
		}
	}

	private lateinit var _name: String
	val name: String get() = _name

	private lateinit var _type: GraphParamType<T>
	val type: GraphParamType<T> get() = _type

	private lateinit var _value: T
	val value: T get() = _value

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeString("name", name)
		writer.writeString("type", type.name)
		type.writeValue("value", value, writer)
	}

	override fun read(reader: StoreReader) {
		_name =reader.readString("name")
		_type = GraphParamTypeRegistry.get(reader.readString("type"))
		_value = type.readValue("value", reader)
	}
}

/**
 * A [Storable], quasi-immutable collection of [GraphParamValue].
 * Quasi-immutable means immutable interface, but for technical reasons updated during deserialization.
 */
class GraphParamValues : Storable {

	companion object {
		fun withDefaults(defs: GraphParamDefinitions): GraphParamValues =
			GraphParamValues().also {
				it._values = defs.map { def -> def.createDefaultValue() }.toMutableList()
			}
	}

	private var _values = mutableListOf<GraphParamValue<*>>()
	val values: Collection<GraphParamValue<*>> get() = _values

	val isEmpty: Boolean get() = values.isEmpty()

	val isNotEmpty: Boolean get() = values.isNotEmpty()

	fun getValue(name: String): GraphParamValue<*>? = values.firstOrNull { it.name == name }

	fun <T : Any> getTypedValue(name: String): GraphParamValue<T>? = values.firstOrNull { it.name == name } as GraphParamValue<T>?

	fun withValue(value: GraphParamValue<*>): GraphParamValues =
		GraphParamValues().also { newValues ->
			newValues._values = values.filter { it.name != value.name }.toMutableList()
			newValues._values.add(value)
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
			_values.clear()
			_values.addAll(reader.readStorables("values"))
		}
	}
}