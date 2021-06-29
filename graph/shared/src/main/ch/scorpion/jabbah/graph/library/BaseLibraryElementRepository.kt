package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.checkState
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCreator
import kotlin.reflect.KClass

/**
 * Contains registrable information for building [GraphElementView]s used by [BaseLibraryElement].
 */
class BaseLibraryElementRepository(
	private val storableCreator: StorableCreator = IOModule.storableCreator
) {

	companion object {
		private val LOG by logger(BaseLibraryElementRepository::class)
	}

	private val entries = mutableMapOf<String,Entry>()

	fun register(entry: Entry) {
		if (entries[entry.id] != null) {
			LOG.warn("entry with ID '${entry.id}' already present, will be replaced")
		}
		entries[entry.id] = entry
	}

	fun register(
		id: String,
		translationKey: String,
		iconPath: String?,
		clazz: KClass<out GraphElementView<*>>
	) {
		register(Entry(id, translationKey, iconPath, clazz))
	}

	fun register(
		id: String,
		translationKey: String,
		iconPath: String?,
		supplier: ((StorableCreator) -> GraphElementView<out GraphElement>)
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
			return entry.supplier.invoke(storableCreator) as GraphElementView<T>
		}
		return storableCreator.create(entry.clazz!!) as GraphElementView<T>
	}

	fun getIconPath(id: String): String? {
		return entries[id]?.iconPath
	}

	fun getTranslationKey(id: String): String? {
		return entries[id]?.translationKey
	}

	data class Entry(
		val id: String,
		val translationKey: String,
		val iconPath: String?,
		val clazz: KClass<out GraphElementView<*>>?,
		val supplier: ((StorableCreator) -> GraphElementView<out GraphElement>)? = null
	) {
		init {
			checkState((clazz != null) || supplier != null, "either StorableCreator/clazz or supplier must be provided")
		}
	}
}