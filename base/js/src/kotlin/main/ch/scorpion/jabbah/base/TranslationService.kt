package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.text.PropertiesFileParser
import kotlinx.browser.window
import kotlin.js.Promise

interface TranslationService {
	fun load(name: String): Promise<Map<String, String>>
}

/**
 * Loads translations as a properties file from the server, parses the entries,
 * and returns the translations as key-vale map.
 */
class TranslationServiceImpl(
	private val baseUrl: String = BASE_URL
) : TranslationService {

	companion object {
		private const val BASE_URL = ".."
	}

	override fun load(name: String): Promise<Map<String, String>> {
		val lang = System.currentLanguage().code
		val url = "$baseUrl/${name}_$lang.properties"

		return window
			.fetch(url)
			.then { it.text() }
			.then { PropertiesFileParser.parse(it) }
			.catch {
				console.error("Error while loading translations from $url: $it")
				mapOf()
			}
	}
}