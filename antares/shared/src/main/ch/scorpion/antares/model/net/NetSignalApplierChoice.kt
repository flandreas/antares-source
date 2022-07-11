package ch.scorpion.antares.model.net

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.Translations

enum class NetSignalApplierChoice(override val customName: String) : EnumProperty<NetSignalApplierChoice> {

	Conflict("conflict") {
		override val netSignalApplier: DigitalNetSignalApplier get() = ConflictDigitalNetSignalApplier
	},
	WiredOr("wiredOr") {
		override val netSignalApplier: DigitalNetSignalApplier get() = WiredOrNetSignalApplier
	};

	companion object {
		const val BASE_KEY = "element.property.netSignalApplierChose"

		fun withName(customName: String): NetSignalApplierChoice {
			return values().firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("unknown NetSignalApplierChoice $customName")
		}
	}

	abstract val netSignalApplier: DigitalNetSignalApplier

	override fun toString(): String {
		return when (this) {
			Conflict -> Translations.getString("$BASE_KEY.conflict")
			WiredOr -> Translations.getString("$BASE_KEY.wiredOr")
		}
	}
}