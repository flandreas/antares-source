package ch.scorpion.jabbah.edit.model.text.description

import ch.scorpion.jabbah.base.Language
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.Bean
import ch.scorpion.jabbah.edit.model.text.Translatable
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.Translation
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** An object containing a [Name] that can be determined and translated by the user. */
interface Namable {
	var name: Name
}

const val BASE_KEY_NAME = "edit.property.name"

/** Posted on the system's [EventBus] when the name of a [Namable] has changed. */
data class NameChangedEvent(
	val owner: Namable,
	val name: Name,
	val oldValue: Name)

/**
 * Creates a delegate property that posts a [NameChangedEvent] on the system's [EventBus].
 */
fun observableName(initialValue: Name, handler: (Name) -> Unit = { }): ReadWriteProperty<Any?, Name> =
	object : ObservableProperty<Name>(initialValue) {
		override fun setValue(thisRef: Any?, property: KProperty<*>, value: Name) {
			val oldValue = getValue(thisRef, property)
			super.setValue(thisRef, property, value)
			handler(value)
			if (thisRef !is Storable || !thisRef.isReading ) {
				BaseModule.eventBus.post(NameChangedEvent(thisRef as Namable, value, oldValue))
			}
		}
	}

class Name(text: TranslatableText = TranslatableText()) : Bean, Translatable {

	companion object {

		/** Reads a [Name] with the specified [externalName], or return [orElse] if not found.*/
		fun read(externalName: String, reader: StoreReader, orElse: Name = Name("")): Name {
			if (reader.hasElement(externalName)) {
				return Name(TranslatableText(reader.readStorables(externalName)))
			}
			return orElse
		}
	}

	constructor(value: String = ""): this(TranslatableText(value))

	/** The displayable name in the current system [Language]. */
	val value: String get() = translation.getTranslation()

	/** Contains translations of [value] .*/
	val translation: TranslatableText = text

	/** Writes the properties of this [Name].*/
	fun write(externalName: String, writer: StoreWriter) {
		if (!translation.isEmpty) {
			writer.writeStorables(externalName, translation.allTranslations())
		}
	}

	/** ---- [Any] */

	override fun toString(): String = value

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other == null || this::class != other::class) return false

		other as Name

		if (translation != other.translation) return false

		return true
	}

	override fun hashCode(): Int = translation.hashCode()

	/** ---- [Translatable] interface */

	// Cannot used delegated interface implementation because factory methods
	// `with...` would instantiate delegate instead of delegating Name

	override val isEmpty: Boolean get() = translation.isEmpty

	override fun getTranslation(language: Language): String =
		translation.getTranslation(language)

	override fun withoutTranslation(language: Language): Translatable =
		Name(translation.withoutTranslation(language))

	override fun withTranslation(language: Language, text: String): Translatable =
		Name(translation.withTranslation(language, text))

	override fun getOptionalTranslation(language: Language): String? =
		translation.getOptionalTranslation(language)

	override fun getFirstLanguage(): Language? = translation.getFirstLanguage()

	override fun hasTranslation(language: Language): Boolean = translation.hasTranslation(language)

	override fun allTranslations(): Iterator<Translation> = translation.allTranslations()

	override fun isAnyEqualOf(other: TranslatableText): Boolean = translation.isAnyEqualOf(other)
}
