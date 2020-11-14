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

/** An object with a [Description] that can be determined and translated by the user. */
interface Describable {
	var description: Description
}

/** Posted on the system's [EventBus] when the [Description] of a [Describable] has changed.*/
data class DescriptionChangedEvent(
	val owner: Describable,
	val description: Description,
	val oldValue: Description)

/** Creates a delegate property that posts a [NameChangedEvent] on the system's [EventBus]. */
fun observableDescription(initialValue: Description = Description(""), changeHandler: (value: Description) -> Unit = {}): ReadWriteProperty<Any?, Description> =
	object : ObservableProperty<Description>(initialValue) {
		override fun setValue(thisRef: Any?, property: KProperty<*>, value: Description) {
			val oldValue = getValue(thisRef, property)
			super.setValue(thisRef, property, value)
			BaseModule.eventBus.post(DescriptionChangedEvent(thisRef as Describable, value, oldValue))
			changeHandler.invoke(value)
		}
	}

class Description(text: TranslatableText = TranslatableText()) : Bean {

	companion object {

		/** Reads a [Description] with the specified [externalName], or return [orElse] if not found.*/
		fun read(externalName: String, reader: StoreReader, orElse: Description = Description("")): Description {
			if (reader.hasElement(externalName)) {
				return Description(TranslatableText(reader.readStorables(externalName)))
			}
			return orElse
		}
	}

	constructor(value: String = ""): this(TranslatableText(value))

	/** The displayable description in the current system [Language]. */
	val value: String? get() = translation.getOptionalTranslation()

	/** Contains translations of [value] .*/
	val translation: TranslatableText = text

	fun write(externalName: String, writer: StoreWriter) {
		if (!translation.isEmpty) {
			writer.writeStorables(externalName, translation.allTranslations())
		}
	}
}
