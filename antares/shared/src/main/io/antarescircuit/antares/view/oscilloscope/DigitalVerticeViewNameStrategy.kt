package io.antarescircuit.antares.view.oscilloscope

import io.antarescircuit.antares.model.input.Switch
import io.antarescircuit.antares.model.net.Tunnel
import io.antarescircuit.antares.model.output.LED
import io.antarescircuit.antares.model.output.RgbLED
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.view.VerticeViewNameStrategyImpl

/**
 * Overwrites the default strategy to provide the [Vertice] name of certain components
 * instead of its single [Port]'s name, which is usually not set.
 */
class DigitalVerticeViewNameStrategy : VerticeViewNameStrategyImpl() {

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