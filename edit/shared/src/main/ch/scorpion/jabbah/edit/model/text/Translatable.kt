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

	override var storableId: Int = -1

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

	/** ---- [Any] */

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other == null) return false
		if (this::class != other::class) return false

		other as Translation

		if (language != other.language) return false
		if (text != other.text) return false

		return true
	}

	override fun hashCode(): Int {
		var result = language.hashCode()
		result = 31 * result + text.hashCode()
		return result
	}
}

/**
 * Represents a dynamic, user provided text that can be translated into various [Language]s.
 *
 * Unlike [Translations], which is used for static translations maintained in property files,
 * [TranslatableText] is used for dynamic, user provided text and stored the translated text
 * within the serialized representation of a [Storable] that contains a [TranslatableText] property.
 *
 * Designed to be immutable.
 *
 * @param translations the [Translation]s to be included in this [TranslatableText].
 */
class TranslatableText(translations: Collection<Translation>? = null) {

	constructor(text: String): this(System.get().currentLanguage(), text)
	constructor(language: Language, text: String): this(listOf(Translation(language, text)))

	private val translations: MutableMap<Language, Translation> = mutableMapOf()

	init {
		translations?.forEach { this.translations[it.language] = it }
	}

	/** ---- [Any] */

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other == null) return false
		if (this::class != other::class) return false

		other as TranslatableText

		if (translations != other.translations) return false

		return true
	}

	override fun hashCode(): Int {
		return translations.hashCode()
	}

	/** ---- [TranslatableText] */

	val isEmpty: Boolean get() = translations.isEmpty()

	/** Creates a new [Translation] by adding the specified translation for the current system language.*/
	fun withTranslation(text: String): TranslatableText {
		return withTranslation(System.get().currentLanguage(), text)
	}

	/** Creates a new [Translation] by adding the specified translation for a particular [Language].*/
	fun withTranslation(language: Language, text: String): TranslatableText {
		checkArgument(StringUtils.isNotBlank(text), "text must not be empty")
		val values = translations.toMutableMap()
		values[language] = Translation(language, text)
		return TranslatableText(values.values)
	}

	/** Returns the translation in the current system language.*/
	fun getTranslation(): String {
		return getTranslation(System.get().currentLanguage())
	}

	fun getOptionalTranslation(): String? {
		return getOptionalTranslation(System.get().currentLanguage())
	}

	/** Returns the translation in the specified [Language].*/
	fun getTranslation(language: Language): String {
		return getOptionalTranslation(language)
			?: throw IllegalArgumentException("no translation available")
	}

	/** Returns the translation in the specified [Language].*/
	fun getOptionalTranslation(language: Language): String? {
		return translations[language]?.text
			?: translations[Language.DEFAULT]?.text
			?: translations.values.firstOrNull()?.text
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

	/** Returns the [Language] also used in [getOptionalTranslation] if neither translation for the System nor the default [Language] is available.*/
	fun getFirstLanguage(): Language? = translations.values.firstOrNull()?.language

}
