package io.antarescircuit.jabbah.graph.model.param

import io.antarescircuit.jabbah.base.dsl.DslError
import io.antarescircuit.jabbah.edit.semantic.Semantic
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
import io.antarescircuit.jabbah.io.Storable
import kotlin.reflect.KClass

interface GraphParamType<T : Any> {

	companion object {
		const val EXPRESSION_OP = '='
	}

	/** The technical name also used as [Storable] key. */
	val name: String

	/** The translated name to be used in UIs.*/
	val displayableName: String

	val valueClass: KClass<T>

	fun writeValue(name: String, value: T, writer: StoreWriter)

	fun readValue(name: String, reader: StoreReader): T

	fun createValue(name: String, value: T, semantic: Semantic?): GraphParamValue<T>

	/** Convert [value] to a type supported by the DSL in order to evaluate expressions such as addition.*/
	fun toDslValue(value: T): Any

	/**
	 * Evaluates [value] using the [GraphParamValues] currently available in [graph].
	 * @throws DslError if evaluation results in an error
	 */
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

	@Suppress("UNCHECKED_CAST")
	fun <T : Any> get(name: String): GraphParamType<T> =
		(providers[name]?.invoke() as GraphParamType<T>?) ?: throw IllegalArgumentException("no provider for GraphParamType '$name' registered")
}