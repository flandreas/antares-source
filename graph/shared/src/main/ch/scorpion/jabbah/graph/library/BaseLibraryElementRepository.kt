package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.checkState
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
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
		if (entries[entry.name] != null) {
			LOG.warn("BaseLibraryElementRepository: entry with name '${entry.name}' already present, will be replaced")
		}
		entries[entry.name] = entry
	}

	fun register(
		name: String,
		translationKey: String,
		iconPath: String?,
		clazz: KClass<out GraphElementView<*>>
	) {
		register(Entry(name, translationKey, iconPath, clazz))
	}

	fun register(
		name: String,
		translationKey: String,
		iconPath: String?,
		supplier: ((StorableCreator) -> GraphElementView<out GraphElement>)
	) {
		register(Entry(name, translationKey, iconPath, null, supplier))
	}

	fun <T : GraphElement> getNewInstance(name: String): GraphElementView<T> {
		val entry = entries[name]
		if (entry == null) {
			LOG.error("BaseLibraryElementRepository: attempt to create instance for unknown name $name")
			throw IllegalArgumentException("unknown entry name $name")
		}
		if (entry.supplier != null) {
			return entry.supplier.invoke(storableCreator) as GraphElementView<T>
		}
		return storableCreator.create(entry.clazz!!) as GraphElementView<T>
	}

	fun getIconPath(name: String): String? {
		return entries[name]?.iconPath
	}

	fun getTranslationKey(name: String): String? {
		return entries[name]?.translationKey
	}

	data class Entry(
		val name: String,
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