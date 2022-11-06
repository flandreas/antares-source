package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.view.GraphElementView
import kotlin.reflect.KClass

/**
 * Contains registrable information for building [GraphElementView]s used by [BaseLibraryElement].
 */
class BaseLibraryElementRepository {

	companion object {
		private val LOG by logger(BaseLibraryElementRepository::class)
	}

	private val entries = mutableMapOf<String,Entry>()

	private fun register(entry: Entry) {
		if (entries[entry.id] != null) {
			LOG.warn("entry with ID '${entry.id}' already present, will be replaced")
		}
		entries[entry.id] = entry
	}

	fun register(
		id: String,
		translationKey: String,
		iconPath: () -> String?,
		clazz: KClass<out GraphElementView<*>>
	) {
		register(Entry(id, translationKey, iconPath, clazz))
	}

	fun register(
		id: String,
		translationKey: String,
		iconPath: () -> String?,
		supplier: () -> GraphElementView<out GraphElement>
	) {
		register(Entry(id, translationKey, iconPath, null, supplier))
	}

	fun <T : GraphElement> getNewInstance(id: String): GraphElementView<T> {
		val entry = entries[id]
		if (entry == null) {
			LOG.error("attempt to create instance for unknown ID $id")
			throw IllegalArgumentException("unknown entry ID $id")
		}
		if (entry.supplier != null) {
			return entry.supplier.invoke() as GraphElementView<T>
		}
		return System.instantiate(entry.clazz!!) as GraphElementView<T>
	}

	fun getIconPath(id: String): String? = entries[id]?.iconPath?.invoke()

	fun getTranslationKey(id: String): String? = entries[id]?.translationKey

	private data class Entry(
		val id: String,
		val translationKey: String,
		val iconPath: () -> String?,
		val clazz: KClass<out GraphElementView<*>>?,
		val supplier: (() -> GraphElementView<out GraphElement>)? = null
	) {
		init {
			check((clazz != null) || supplier != null) { "either StorableCreator/clazz or supplier must be provided" }
		}
	}
}