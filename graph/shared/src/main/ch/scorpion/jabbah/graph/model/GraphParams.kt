package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Namable
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.edit.model.text.description.observableName
import ch.scorpion.jabbah.io.*

interface GraphParamType<T> {
	val name: String
	fun writeValue(name: String, value: T, writer: StoreWriter)
	fun readValue(name: String, reader: StoreReader): T
}

typealias GraphParamTypeProvider = () -> GraphParamType<*>

object GraphParamTypeRegistry {

	private val providers = mutableMapOf<String, GraphParamTypeProvider>()

	fun clear() {
		providers.clear()
	}

	fun register(name: String, provider: GraphParamTypeProvider) {
		if (providers.containsKey(name)) {
			throw IllegalArgumentException("provider GraphParamType '$name' already registered")
		}
		providers[name] = provider
	}

	fun <T> get(name: String): GraphParamType<T> =
		providers[name]?.invoke() as GraphParamType<T> ?: throw IllegalArgumentException("no provider for GraphParamType '$name' registered")
}

/**
 * Default constructor must be "empty" in order to be readable as [Storable].
 */
class GraphParamDefinition<T : Any>(
	name: TranslatableText = TranslatableText()
) : Storable, Namable {

	companion object {
		fun <T : Any> create(
			name: TranslatableText,
			type: GraphParamType<T>,
			defaultValue: T
		) : GraphParamDefinition<T> {
			val definition = GraphParamDefinition<T>(name)
			definition.type = type
			definition.defaultValue = defaultValue
			return definition
		}
	}

	override var name: Name by observableName(Name(name))

	lateinit var type: GraphParamType<T>

	lateinit var defaultValue: T

	/** ---- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		name.write("name", writer)
		writer.writeString("type", type.name)
		type.writeValue("defaultValue", defaultValue, writer)
	}

	override fun read(reader: StoreReader) {
		name = Name.read("name", reader)
		type = GraphParamTypeRegistry.get(reader.readString("type"))
		defaultValue = type.readValue("defaultValue", reader)
	}
}