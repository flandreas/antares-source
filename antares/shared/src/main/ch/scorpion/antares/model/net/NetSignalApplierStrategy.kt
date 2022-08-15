package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.model.NetSignalApplier

enum class NetSignalApplierStrategy(override val customName: String) : EnumProperty<NetSignalApplierStrategy> {

	Conflict("conflict") {
		override val netSignalApplier: NetSignalApplier<DigitalSignal> get() = ConflictDigitalNetSignalApplier
	},
	WiredOr("wiredOr") {
		override val netSignalApplier: NetSignalApplier<DigitalSignal> get() = WiredOrNetSignalApplier
	};

	companion object {
		const val BASE_KEY = "element.property.netSignalApplierStrategy"

		fun withName(customName: String): NetSignalApplierStrategy =
			values().firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("unknown NetSignalApplierStrategy $customName")
	}

	abstract val netSignalApplier: NetSignalApplier<DigitalSignal>

	override fun toString(): String =
		when (this) {
			Conflict -> Translations.getString("$BASE_KEY.conflict")
			WiredOr -> Translations.getString("$BASE_KEY.wiredOr")
		}
}