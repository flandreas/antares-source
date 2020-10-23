package ch.scorpion.antares.view.oscilloscope

import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.model.net.Tunnel
import ch.scorpion.antares.model.output.LED
import ch.scorpion.antares.model.output.RgbLED
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeProbeNameStrategyImpl

/**
 * Overwrites the default strategy in order to provide the [Vertice] name of certain components
 * instead of its single [Port]'s name, which is usually not set.
 */
class DigitalOscilloscopeProbeNameStrategy : OscilloscopeProbeNameStrategyImpl() {

	override fun portName(port: Port<*>?): String? {
		return when (port?.owner) {
			is LED -> port.owner?.name
			is RgbLED -> port.owner?.name
			is Switch -> port.owner?.name
			is Tunnel -> port.owner?.name
			else -> super.portName(port)
		}
	}
}