package io.antarescircuit.antares.web

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.TranslationServiceJs
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.text.PropertiesFileParser
import kotlinx.browser.window
import kotlin.js.Promise

/** Loads translation bundles as static `<name>_<lang>.properties` files below `<baseUrl>/translations`. */
class StaticTranslationServiceJs(private val baseUrl: String) : TranslationServiceJs {

	companion object {
		private val LOG by logger(StaticTranslationServiceJs::class)
	}

	private val requests = mutableMapOf<String, Promise<Map<String, String>>>()

	/** Loads each bundle only once, so bundles can be prefetched in parallel and applied in order later. */
	override fun load(name: String): Promise<Map<String, String>> = requests.getOrPut(name) { fetch(name) }

	private fun fetch(name: String): Promise<Map<String, String>> {
		val url = "$baseUrl/translations/${name}_${System.currentLanguage().code}.properties"
		return window.fetch(url)
			.then { it.text() }
			.then { PropertiesFileParser.parse(it) }
			.catch {
				LOG.error("Could not load translations from $url: $it")
				mapOf()
			}
	}
}
