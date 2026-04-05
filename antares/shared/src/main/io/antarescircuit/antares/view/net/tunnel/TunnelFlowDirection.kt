package io.antarescircuit.antares.view.net.tunnel

import io.antarescircuit.jabbah.base.EnumProperty
import io.antarescircuit.jabbah.base.Translations

/**
 * Defines the direction in which signals flow through a [TunnelView].
 * The enum names are in relation to the virtual tunnel between two [TunnelView], e.g. "In" means
 * from the circuit into the virtual tunnel, whereas "Out" means out of the virtual tunnel
 * and into the circuit.
 */
enum class TunnelFlowDirection(override val customName: String) : EnumProperty<TunnelFlowDirection> {
	Undefined("undefined"),
	In("in"),
	Out("out"),
	InOut("inOut");

	companion object {
		fun withName(customName: String): TunnelFlowDirection =
			values().firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("unknown TunnelFlowDirection '$customName'")
	}

	override fun toString(): String =
		when (this) {
			Undefined -> Translations.getString("element.property.tunnelFlowDirection.undefined")
			In -> Translations.getString("element.property.tunnelFlowDirection.in")
			Out -> Translations.getString("element.property.tunnelFlowDirection.out")
			InOut -> Translations.getString("element.property.tunnelFlowDirection.inOut")
		}
}