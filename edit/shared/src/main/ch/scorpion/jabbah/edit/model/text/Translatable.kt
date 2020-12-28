package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.io.*

/**
 * Represents an individual text in a particular [Language].
 * Mutable only for deserialization.
 */
class Translation(
	language: Language = Language.DEFAULT,
	text: String = ""
) : Storable {

	companion object {

		fun ofStaticKey(key: String): Translation {
			return Translation(System.currentLanguage(), Translations.getString(key))
		}
	}

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

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {}

	override fun getStorableChildren(): Iterator<Storable> = EmptyIterator()

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

	override fun toString(): String = "[${language.code}]='$text'"
}

interface Translatable {

	val isEmpty: Boolean
	val isNotEmpty: Boolean get() = !isEmpty

	fun getTranslation(): String = getTranslation(System.currentLanguage())

	/** Returns the translation in the specified [Language].*/
	fun getTranslation(language: Language): String

	/** Creates a new [Translatable] by removing the translation for the current system language.*/
	fun withoutTranslation(): Translatable = withoutTranslation(System.currentLanguage())

	/** Creates a new [Translatable] by removing the text in the specified [Language].*/
	fun withoutTranslation(language: Language): Translatable

	/** Creates a new [Translatable] by adding the specified translation for the current system language.*/
	fun withTranslation(text: String): Translatable =
		withTranslation(System.currentLanguage(), text)

	/** Creates a new [Translatable] by adding the specified translation for a particular [Language].*/
	fun withTranslation(language: Language, text: String): Translatable

	fun getOptionalTranslation(): String? = getOptionalTranslation(System.currentLanguage())

	fun getOptionalTranslation(language: Language): String?


	/**
	 * Determines whether this [Translatable] contains at least a translation for the default [Language].
	 * or the [System] language.
	 */
	fun hasDefaultOrSystemLanguage(): Boolean =
		hasTranslation(Language.DEFAULT) || hasTranslation(System.currentLanguage())

	/**
	 * Returns the [Language] also used in [getOptionalTranslation] if neither translation for the System
	 * nor the default [Language] is available.
	 */
	fun getFirstLanguage(): Language?

	/** Determines whether this [Translatable] contains a translation in the specified [Language].*/
	fun hasTranslation(language: Language): Boolean

	/** Returns all registered translations.*/
	fun allTranslations(): Iterator<Translation>

	fun isAnyEqualOf(other: TranslatableText): Boolean

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
class TranslatableText(translations: Collection<Translation>? = null) : Translatable {

	constructor(text: String) : this(System.currentLanguage(), text)
	constructor(translation: Translation) : this(listOf(translation))
	constructor(language: Language, text: String) : this(listOf(Translation(language, text)))

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

	override fun hashCode(): Int = translations.hashCode()

	/** ---- [Translatable] */

	override val isEmpty: Boolean get() =
		translations.values.all { it.text.isEmpty() }

	override fun hasTranslation(language: Language): Boolean =
		translations[language] != null

	override fun getTranslation(language: Language): String =
		getOptionalTranslation(language)
		?: throw IllegalArgumentException("no translation available")

	override fun withTranslation(language: Language, text: String): TranslatableText {
		checkArgument(StringUtils.isNotBlank(text), "text must not be empty")
		val values = translations.toMutableMap()
		values[language] = Translation(language, text)
		return TranslatableText(values.values)
	}

	override fun withoutTranslation(language: Language): TranslatableText {
		val values = translations.toMutableMap()
		values.remove(language)
		return TranslatableText(values.values)
	}

	override fun getFirstLanguage(): Language? = translations.values.firstOrNull()?.language

	override fun getOptionalTranslation(language: Language): String? =
		translations[language]?.text
		?: translations[Language.DEFAULT]?.text
		?: translations.values.firstOrNull()?.text

	/** Returns all registered translations.*/
	override fun allTranslations(): Iterator<Translation> =
		translations.values.iterator()

	override fun isAnyEqualOf(other: TranslatableText): Boolean =
		other.translations.any { getOptionalTranslation(it.key) == it.value.text }
}
