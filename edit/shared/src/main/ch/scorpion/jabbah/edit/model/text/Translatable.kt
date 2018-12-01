package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.Language
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.io.*
import java.util.*

/**
 * Represents an individual text in a particular [Language].
 * Mutable only for deserialization.
 */
class Translation(
	language: Language = Language.DEFAULT,
	text: String = ""
) : Storable {

	var language: Language = language
		private set

	var text: String = text
		private set

	/** ---- [Storable] interface */

	override var storableId: Int = 0

	override fun write(writer: StoreWriter) {
		writer.writeString("lang", language.code)
		writer.writeString("text", text)
	}

	override fun read(reader: StoreReader) {
		language = Language.withCode(reader.readString("lang"))
		text = reader.readString("text")
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun getStorableChildren(): Iterator<Storable> = Collections.emptyIterator()
}

/**
 * Represents a dynamic, user provided text that can be translated into various [Language]s.
 *
 * Unlike [Translations], which is used for static translations maintained in property files,
 * [TranslatableText] is used for dynamic, user provided text and stored the translated text
 * within the serialized representation of a [Storable] that contains a [TranslatableText] property.
 *
 * @param translations the [Translation]s to be added to this [TranslatableText].
 */
class TranslatableText(translations: Collection<Translation>? = null) {

	private val translations: MutableMap<Language, Translation> = mutableMapOf()

	init {
		translations?.forEach { this.translations[it.language] = it }
	}

	/** Sets the translation for the current system language.*/
	fun setTranslation(text: String) {
		setTranslation(System.get().currentLanguage(), text)
	}

	/** Sets the translation for a particular [Language].*/
	fun setTranslation(language: Language, text: String) {
		checkArgument(StringUtils.isNotBlank(text), "text must not be empty")
		translations[language] = Translation(language, text)
	}

	/** Returns the translation in the current system language.*/
	fun getTranslation(): String {
		return getTranslation(System.get().currentLanguage())
	}

	/** Returns the translation in the specified [Language].*/
	fun getTranslation(language: Language): String {
		return translations[language]?.text
			?: translations[Language.DEFAULT]?.text
			?: throw IllegalArgumentException("no translation for language '$language' available")
	}

	/** Determines whether this [TranslatableText] contains a translation in the specified [Language].*/
	fun hasTranslation(language: Language): Boolean = translations[language] != null

	/** Returns all registered translations.*/
	fun allTranslations(): Iterator<Translation> = translations.values.iterator()

	/**
	 * Determines whether this [TranslatableText] contains at least a translation for the default [Language].
	 * or the [System] language.
	 */
	fun hasDefaultOrSystemLanguage(): Boolean = hasTranslation(Language.DEFAULT) || hasTranslation(System.get().currentLanguage())
}
