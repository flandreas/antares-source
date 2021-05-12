package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJs

/**
 * Implements [Translations] for the JavaScript target.
 */
actual object Translations {

	private val LOG by logger(Translations::class)

	private val map = mutableMapOf<String,String>()

	private val bundleNames = mutableListOf<String>()

	actual var language: Language
		get() = System.currentLanguage()
		set(@Suppress("UNUSED_PARAMETER") value) {
			throw NotImplementedError()
		}


	actual fun withAnyKey() { }

	actual fun addBundle(name: String) {
		LOG.info("Adding translation bundle '$name'")
		BaseModuleJs.translationService.load(name)
			.then {
				translation -> translation.forEach { addKey(it.key, it.value) }
				bundleNames.add(name)
				BaseModule.eventBus.post(TranslationBundleAdded(name))
			}
    }

	actual fun hasBundle(name: String): Boolean =
		bundleNames.contains(name)

	actual fun addKey(key: String, value: String) {
		map[key] = value
	}

	actual fun getString(key: String, vararg params: Any): String {
		// TODO Replace variables
		return map.getOrElse(key) { key }
	}

	actual fun getOptionalString(key: String): String? = map[key]
}