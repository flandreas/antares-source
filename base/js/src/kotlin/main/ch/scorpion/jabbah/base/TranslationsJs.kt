package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.module.BaseModuleJs
import kotlin.js.Promise

/**
 * Implements [Translations] for the JavaScript target.
 */
actual object Translations {

	private val LOG by logger(Translations::class)

	private val map = mutableMapOf<String,String>()

	private val requestedBundleNames = mutableListOf<String>()

	private val loadedBundleNames = mutableListOf<String>()

	actual var language: Language
		get() = System.currentLanguage()
		set(@Suppress("UNUSED_PARAMETER") value) {
			throw NotImplementedError()
		}

	actual val bundleNames: Set<String> get() = loadedBundleNames.toSet()

	actual fun withAnyKey() { }

	actual fun addBundle(name: String) {
		addBundleAsync(name)
    }

	fun addBundleAsync(name: String): Promise<Unit>? {
		if (!hasBundle(name)) {
			LOG.trace("Adding translation bundle '$name'")
			requestedBundleNames.add(name)
			return BaseModuleJs.translationService.load(name)
				.then { translation ->
					translation.forEach { addKey(it.key, it.value) }
					loadedBundleNames.add(name)
				}
		}
		return null
	}

	actual fun hasBundle(name: String): Boolean =
		loadedBundleNames.contains(name)

	actual fun hasAllBundles(): Boolean = requestedBundleNames.all { loadedBundleNames.contains(it) }

	actual fun addKey(key: String, value: String) {
		map[key] = value
	}

	actual fun getString(key: String, vararg params: Any): String {
		var value = map.getOrElse(key) { key }
		params.forEachIndexed { index, param ->
			value = value.replace("{$index}", "$param")
		}
		return value
	}

	actual fun getOptionalString(key: String): String? = map[key]
}