package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.base.PreferencesChangedEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Represents the current [DigitalSignalNotation] in the system.
 */
object CurrentDigitalSignalNotation {

	private val eventBus: EventBus = BaseModule.eventBus

	var notation: DigitalSignalNotation = notationFromProperties
		private set(value) {
			if (field != value) {
				field =value
				BaseModule.properties.customize(DigitalSignalNotation.PROP_DIGITAL_SIGNAL_NOTATION, field.customName)
				eventBus.post(CurrentDigitalSignalNotationEvent(field))
			}
		}

	init {
		eventBus.register(PreferencesChangedEvent::class) {
			notation = notationFromProperties
		}
	}

	private val notationFromProperties: DigitalSignalNotation get() =
		DigitalSignalNotation.withName(BaseModule.properties.getString(DigitalSignalNotation.PROP_DIGITAL_SIGNAL_NOTATION))
}

data class CurrentDigitalSignalNotationEvent(val notation: DigitalSignalNotation)