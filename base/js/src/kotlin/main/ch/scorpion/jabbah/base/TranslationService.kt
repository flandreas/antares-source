package ch.scorpion.jabbah.base

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
		private val LOG by logger(TranslationService::class)
		private const val BASE_URL = ".."
	}

	override fun load(name: String): Promise<Map<String, String>> {
		val lang = System.currentLanguage().code
		val url = "$baseUrl/${name}_$lang.properties"

		return window
			.fetch(url)
			.then { it.text() }
			.then { parse(it) }
			.catch {
				console.error("Error while loading translations from $url: $it")
				mapOf()
			}
	}

	private fun parse(text: String): Map<String, String> {
		val translations = mutableMapOf<String, String>()
		text
			.lines()
			.filter { !it.startsWith('#') && it.isNotBlank() }
			.forEach { line ->
				try {
					val (key, value) = line.split('=')
					translations[key.trim()] = value.trim()
				} catch (e: Throwable) {
					LOG.error("Error while parsing translation line '$line': $e")
				}
			}
		return translations
	}
}