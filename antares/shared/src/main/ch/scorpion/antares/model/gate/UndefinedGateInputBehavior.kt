package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.jabbah.base.PreferencesChangedEvent
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule

enum class UndefinedGateInputBehavior(val customName: String) {

	ReadAs0("readAs0") {
		override val definedBit: Bit get() = Bit.False
	},
	ReadAs1("readAs1") {
		override val definedBit: Bit get() = Bit.True
	},
	ReadAsRandom("readAsRandom") {
		override val definedBit: Bit get() = Bit.random()
	};

	companion object {
		const val PROP_UNDEFINED_GATE_INPUT_BEHAVIOR = "antares.model.undefinedInputBehaviour"

		fun withName(customName: String): UndefinedGateInputBehavior =
			values().firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("unknown UndefinedGateInputBehavior '$customName'")
	}

	abstract val definedBit: Bit

	override fun toString(): String {
		return when (this) {
			ReadAs0 -> Translations.getString("antares.preference.undefinedGateInputBehavior.readAs0.name")
			ReadAs1 -> Translations.getString("antares.preference.undefinedGateInputBehavior.readAs1.name")
			ReadAsRandom -> Translations.getString("antares.preference.undefinedGateInputBehavior.readAsRandom.name")
		}
	}
}

object CurrentUndefinedGateInputBehavior {

	var value: UndefinedGateInputBehavior = fromProperties

	private val fromProperties: UndefinedGateInputBehavior get() =
		UndefinedGateInputBehavior.withName(BaseModule.properties.getString(UndefinedGateInputBehavior.PROP_UNDEFINED_GATE_INPUT_BEHAVIOR))

	init {
		BaseModule.eventBus.register(PreferencesChangedEvent::class) { value = fromProperties }
	}
}