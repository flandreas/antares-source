package ch.scorpion.jabbah.graph.model.param

import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

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