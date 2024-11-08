package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.library.LibraryPreferences
import ch.scorpion.jabbah.graph.library.LibraryPreferencesProperty

enum class UndefinedGateInputBehavior(
	override val customName: String
) : EnumProperty<UndefinedGateInputBehavior> {

	ReadAs0("readAs0") {
		override val definedBit: Bit get() = Bit.False
		override fun definedValue(bitWidth: BitWidth): DigitalSignal =
			DigitalSignalFactory.allOf(bitWidth, definedBit)
	},
	ReadAs1("readAs1") {
		override val definedBit: Bit get() = Bit.True
		override fun definedValue(bitWidth: BitWidth): DigitalSignal =
			DigitalSignalFactory.allOf(bitWidth, definedBit)
	},
	ReadAsRandom("readAsRandom") {
		override val definedBit: Bit get() = Bit.random()
		override fun definedValue(bitWidth: BitWidth): DigitalSignal =
			DigitalSignalFactory.random(bitWidth)
	};

	companion object {
		const val PROP_UNDEFINED_GATE_INPUT_BEHAVIOR = "antares.model.undefinedInputBehaviour"

		fun withName(customName: String): UndefinedGateInputBehavior =
			values().firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("unknown UndefinedGateInputBehavior '$customName'")
	}

	abstract val definedBit: Bit

	abstract fun definedValue(bitWidth: BitWidth): DigitalSignal

	override fun toString(): String {
		return when (this) {
			ReadAs0 -> Translations.getString("antares.preference.undefinedGateInputBehavior.readAs0.name")
			ReadAs1 -> Translations.getString("antares.preference.undefinedGateInputBehavior.readAs1.name")
			ReadAsRandom -> Translations.getString("antares.preference.undefinedGateInputBehavior.readAsRandom.name")
		}
	}
}

object CurrentUndefinedGateInputBehavior : LibraryPreferencesProperty<UndefinedGateInputBehavior>() {

	override val fromProperties: UndefinedGateInputBehavior get() =
		UndefinedGateInputBehavior.withName(BaseModule.properties.getString(UndefinedGateInputBehavior.PROP_UNDEFINED_GATE_INPUT_BEHAVIOR))

	override val fromLibraryPreferences: UndefinedGateInputBehavior get() =
		UndefinedGateInputBehavior.withName(LibraryPreferences.getString(UndefinedGateInputBehavior.PROP_UNDEFINED_GATE_INPUT_BEHAVIOR))
}