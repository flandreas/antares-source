package ch.scorpion.antares.model.gate

import ch.scorpion.jabbah.base.PreferencesChangedEvent
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule

enum class OpenGateInputBehavior(val customName: String) {

	Accept("accept"),
	Random("random"),
	Error("error");

	companion object {

		/** The name of the [String] property in [Properties] containing the custom name of the current [OpenGateInputBehavior].*/
		const val PROP_OPEN_GATE_INPUT_BEHAVIOR = "antares.model.openGateInputBehavior"

		fun withName(customName: String): OpenGateInputBehavior =
			values().firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("unknown OpenGateInputBehaviour '$customName'")
	}

	override fun toString(): String {
		return when (this) {
			Accept -> Translations.getString("antares.preference.openGateInputBehavior.accept.name")
			Random -> Translations.getString("antares.preference.openGateInputBehavior.random.name")
			Error -> Translations.getString("antares.preference.openGateInputBehavior.error.name")
		}
	}
}

object CurrentOpenGateInputBehaviour {

	/** Holds the current value as stored in [Properties].*/
	var value: OpenGateInputBehavior = fromProperties

	private val fromProperties: OpenGateInputBehavior get() =
		OpenGateInputBehavior.withName(BaseModule.properties.getString(OpenGateInputBehavior.PROP_OPEN_GATE_INPUT_BEHAVIOR))

	init {
		BaseModule.eventBus.register(PreferencesChangedEvent::class) { value = fromProperties }
	}
}