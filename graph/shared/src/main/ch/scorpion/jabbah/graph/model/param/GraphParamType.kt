package ch.scorpion.jabbah.graph.model.param

import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.io.Storable
import kotlin.reflect.KClass

interface GraphParamType<T : Any> {

	/** The technical name also used as [Storable] key. */
	val name: String

	/** The translated name to be used in UIs.*/
	val displayableName: String

	val valueClass: KClass<T>

	fun writeValue(name: String, value: T, writer: StoreWriter)

	fun readValue(name: String, reader: StoreReader): T

	fun createValue(name: String, value: T): GraphParamValue<T>

	/** Convert [value] to a type supported by the DSL in order to evaluate expressions such as addition.*/
	fun toDslValue(value: T): Any

	fun evaluateIn(graph: Graph, value: T): T
}

typealias GraphParamTypeProvider = () -> GraphParamType<*>

object GraphParamTypeRegistry {

	private val providers = mutableMapOf<String, GraphParamTypeProvider>()

	fun clear() {
		providers.clear()
	}

	fun getFirst(): GraphParamType<*>? = providers[providers.keys.first()]?.invoke()

	fun getAll(): Collection<GraphParamType<*>> = providers.values.map { it.invoke() }

	fun register(name: String, provider: GraphParamTypeProvider) {
		if (providers.containsKey(name)) {
			throw IllegalArgumentException("provider GraphParamType '$name' already registered")
		}
		providers[name] = provider
	}

	fun <T : Any> get(name: String): GraphParamType<T> =
		(providers[name]?.invoke() as GraphParamType<T>?) ?: throw IllegalArgumentException("no provider for GraphParamType '$name' registered")
}