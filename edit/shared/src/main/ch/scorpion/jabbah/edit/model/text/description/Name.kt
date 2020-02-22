package ch.scorpion.jabbah.edit.model.text.description

import ch.scorpion.jabbah.base.Language
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/** An object containing a [Name] that can be determined and translated by the user. */
interface Namable {

	/**
	 * Contains the name of this [Namable].
	 * This property is read-only in order to be used as Kotlin delegation property.
	 */
	val name: Name
}

/** A standard implementation of the [Namable] interface. */
data class NamableImpl(override val name: Name) : Namable {
	constructor(value: String, eventBus: EventBus? = null): this(Name(value, eventBus))
	constructor(value: TranslatableText, eventBus: EventBus? = null): this(Name(value, eventBus))
}

/**
 * Represents a name that can be determined and translated by the user.
 */
class Name(
	text: TranslatableText = TranslatableText(),
	private val eventBus: EventBus? = null
) {

	constructor(value: String = "", eventBus: EventBus? = null): this(TranslatableText(value), eventBus)

	override fun toString(): String = value

	/** The displayable name in the current system [Language]. */
	var value: String
		get() = translation.getTranslation()
		set(newValue) {
			if (value != newValue) {
				translation = translation.withTranslation(newValue)
			}
		}

	/** Contains translations of [value] .*/
	var translation: TranslatableText = text
		set(value) {
			if (field != value) {
				val oldValue = field
				field = value
				eventBus?.post(NameChangedEvent(this, oldValue))
			}
		}

	/** Reads the properties of this [Name].*/
	fun read(externalName: String, reader: StoreReader) {
		if (reader.hasElement(externalName)) {
			translation = TranslatableText(reader.readStorables(externalName))
		}
	}

	/** Writes the properties of this [Name].*/
	fun write(externalName: String, writer: StoreWriter) {
		if (!translation.isEmpty) {
			writer.writeStorables(externalName, translation.allTranslations())
		}
	}
}

/** Posted on a [Name]'s [EventBus] when its value or translations have changed.*/
data class NameChangedEvent(val name: Name, val oldValue: TranslatableText)