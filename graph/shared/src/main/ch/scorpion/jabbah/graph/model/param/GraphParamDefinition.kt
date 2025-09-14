package ch.scorpion.jabbah.graph.model.param

import ch.scorpion.jabbah.base.help.HelpId
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.edit.semantic.Semantic
import ch.scorpion.jabbah.edit.semantic.SemanticRegistry
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
			defaultValue: T,
			semantic: Semantic? = null,
			description: Description = Description("")
		) : GraphParamDefinition<T> {
			val definition = GraphParamDefinition<T>(name)
			definition.type = type
			definition.defaultValue = defaultValue
			definition.semantic = semantic
			definition.description = description
			return definition
		}
	}

	var name: String = name

	lateinit var type: GraphParamType<T>

	lateinit var defaultValue: T

	var semantic: Semantic? = null

	val hasSemantic: Boolean get() = semantic != null

	var description: Description = Description(TranslatableText())

	fun createDefaultValue(): GraphParamValue<T> = type.createValue(name, defaultValue, semantic)

	fun createValue(value: T): GraphParamValue<T> = type.createValue(name, value, semantic)

	/** ---- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeString("name", name)
		writer.writeString("type", type.name)
		type.writeValue("defaultValue", defaultValue, writer)
		if (semantic != null) {
			writer.writeString("semantic", semantic!!.customName)
		}
		description.write("desc", writer)
	}

	override fun read(reader: StoreReader) {
		name = reader.readString("name")
		type = GraphParamTypeRegistry.get(reader.readString("type"))
		defaultValue = type.readValue("defaultValue", reader)
		if (reader.hasAttribute("semantic")) {
			semantic = SemanticRegistry.withCustomName(reader.readString("semantic"))
		}
		if (reader.hasElement("desc")) {
			description = Description.read("desc", reader)
		}
	}
}

class GraphParamDefinitions : AbstractStorable(), Iterable<GraphParamDefinition<*>> {

	companion object {
		val HELP_ID = HelpId("graph.paramDefinitions")
	}

	private var _definitions = mutableListOf<GraphParamDefinition<*>>()
	val definitions: Collection<GraphParamDefinition<*>> get() = _definitions

	val size: Int get() = _definitions.size

	val isEmpty: Boolean get() = _definitions.isEmpty()

	val isNotEmpty: Boolean get() = _definitions.isNotEmpty()

	fun get(index: Int): GraphParamDefinition<*> = _definitions[index]

	fun get(name: String): GraphParamDefinition<*>? = _definitions.firstOrNull { it.name == name }

	override fun iterator(): Iterator<GraphParamDefinition<*>> = _definitions.iterator()

	fun contains(name: String): Boolean = _definitions.any { it.name == name }

	fun hasAnyWithSemantic(semantic: Semantic): Boolean = _definitions.any { it.semantic == semantic }

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