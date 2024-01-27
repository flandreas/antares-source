package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.text.PropertiesFileParser
import kotlinx.browser.window
import kotlin.js.Promise

interface TranslationServiceJs {
	fun load(name: String): Promise<Map<String, String>>
}

/**
 * Loads translations as a properties file from the server, parses the entries,
 * and returns the translations as key-vale map.
 */
class TranslationServiceJsImpl(
	private val baseUrl: String
) : TranslationServiceJs {

	companion object {
		private val LOG by logger(TranslationServiceJsImpl::class)
	}

	override fun load(name: String): Promise<Map<String, String>> {
		val lang = System.currentLanguage().code
		val url = "$baseUrl/translation/$name/$lang"

		return window
			.fetch(url)
			.then { it.text() }
			.then { PropertiesFileParser.parse(it) }
			.catch {
				LOG.error("Error while loading translations from $url: $it")
				mapOf()
			}
	}
}