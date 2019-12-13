package ch.scorpion.jabbah.edit.model.text.description

import ch.scorpion.jabbah.base.Language
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.Translation
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/** An object with a description that can be determined and translated by the user. */
interface Describable {

	/**
	 * Contains the description of this [Describable].
	 * This property is read-only in order to be used as Kotlin delegation property.
	 */
	val description: Description
}

/** A standard implementation of the [Describable] interface. */
data class DescribableImpl(override val description: Description = Description()) : Describable {
	constructor(eventBus: EventBus): this(Description(eventBus = eventBus))
	constructor(changeHandler: ((Description, TranslatableText) -> Unit)): this(Description(changeHandler = changeHandler))
	constructor(translation: Translation): this(Description(translation))
	constructor(value: TranslatableText, eventBus: EventBus? = null): this(Description(value, eventBus))
}

class Description(
	translatableText: TranslatableText = TranslatableText(),
	var changeHandler: ((Description, TranslatableText) -> Unit)? = null
) {

	constructor(
		translatableText: TranslatableText = TranslatableText(),
		eventBus: EventBus?
	): this(translatableText, { desc, oldValue -> eventBus?.post(DescriptionChangedEvent(desc, oldValue)) })

	constructor(translation: Translation): this(TranslatableText(translation))

	/** The displayable name in the current system [Language]. */
	var value: String?
		get() = translation.getOptionalTranslation()
		set(newValue) {
			if (newValue != value) {
				translation = if (newValue != null) {
					translation.withTranslation(newValue)
				} else {
					translation.withoutTranslation()
				}
			}
		}

	/** Contains translations of [value] .*/
	var translation: TranslatableText = translatableText
		set(newValue) {
			if (field != newValue) {
				val oldValue = field
				field = newValue
				changeHandler?.invoke(this, oldValue)
			}
		}

	fun read(externalName: String, reader: StoreReader) {
		if (reader.hasElement(externalName)) {
			translation = TranslatableText(reader.readStorables(externalName))
		}
	}

	fun write(externalName: String, writer: StoreWriter) {
		if (!translation.isEmpty) {
			writer.writeStorables(externalName, translation.allTranslations())
		}
	}
}

/** Posted on a [Description]'s [EventBus] when its value or translations have changed.*/
data class DescriptionChangedEvent(val description: Description, val oldValue: TranslatableText)