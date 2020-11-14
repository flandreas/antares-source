package ch.scorpion.jabbah.edit.model.text.description

import ch.scorpion.jabbah.base.Language
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** An object containing a [Name] that can be determined and translated by the user. */
interface Namable {
	var name: Name
}

/** Posted on the system's [EventBus] when the name of a [Namable] has changed. */
data class NameChangedEvent(
	val owner: Namable,
	val name: Name,
	val oldValue: Name)

/** Creates a delegate property that posts a [NameChangedEvent] on the system's [EventBus]. */
fun observableName(initialValue: Name): ReadWriteProperty<Any?, Name> =
	object : ObservableProperty<Name>(initialValue) {
		override fun setValue(thisRef: Any?, property: KProperty<*>, value: Name) {
			val oldValue = getValue(thisRef, property)
			super.setValue(thisRef, property, value)
			BaseModule.eventBus.post(NameChangedEvent(thisRef as Namable, value, oldValue))
		}
	}

/**
 * Represents a name that can be determined and translated by the user.
 * Wraps an immutable [TranslatableText] in order to read/write to/from persistent store.
 */
class Name(text: TranslatableText = TranslatableText()) : Bean {

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

	override fun toString(): String = value

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
}
