package ch.scorpion.jabbah.base.text

import ch.scorpion.jabbah.base.logger

/**
 * Used on the JS platform to parse property files as defined on the JVM platform.
 * Requirements:
 * - Line: Name=Value
 * - Lines are separated by newlines
 * - Lines starting with # are ignored (comments)
 * - Empty lines are ignored
 * - Values can span several lines when terminated by \
 */
object PropertiesFileParser {

	private val LOG by logger(PropertiesFileParser::class)

	fun parse(text: String): Map<String, String> {
		val translations = mutableMapOf<String, String>()
		var currentKey: String? = null
		var currentValue: String? = null
		text
			.lines()
			.filter { !it.startsWith('#') && it.isNotBlank() }
			.forEach { line ->
				try {
					if (currentKey == null) {
						val (key, value) = line.split('=')
						currentKey = key.trim()
						currentValue = value.trim()
					} else {
						currentValue = currentValue?.plus(line.trim())
					}
					if (currentValue!!.endsWith('\\')) {
						currentValue = currentValue!!.removeSuffix("\\")
					} else {
						translations[currentKey!!] = currentValue!!
						currentKey = null
						currentValue = null
					}
				} catch (e: Throwable) {
					LOG.error("Error while parsing translation line '$line': $e")
				}
			}
		if (currentKey != null) {
			translations[currentKey!!] = currentValue!!
		}
		return translations
	}
}