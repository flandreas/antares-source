package ch.scorpion.antares.model

import ch.scorpion.antares.model.net.DigitalNet
import ch.scorpion.antares.model.net.Tunnel
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.graph.GraphImpl

/**
 * Extends [GraphImpl] in order to create temporary [Net]s for [Tunnel]s during execution.
 */
class DigitalGraph(
	name: String = Translations.getString("graph.name.unknown"),
	eventBus: EventBus = BaseModule.eventBus
) : GraphImpl(name = name, eventBus = eventBus) {

	override fun formNet(signalHandler: SignalHandler) {
		createTunnelNets()
		super.formNet(signalHandler)
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		destroyTunnelNets()
	}

	private fun createTunnelNets() {
		val tunnelNets = mutableMapOf<String, Net<DigitalSignal>>()
		elements
			.filterIsInstance<Tunnel>()
			.filter { StringUtils.isNotEmpty(it.name) }
			.forEach { tunnel ->
				tunnelNets
					.getOrPut(tunnel.name!!) { DigitalNet().apply { add(this) } }
					.also {
						it.connect(tunnel.getPort(2))
					}
		}
	}

	private fun destroyTunnelNets() {
		elements.filterIsInstance<Tunnel>().forEach { tunnel ->
			val port = tunnel.getPort<DigitalSignal>(2)
			port.net?.unconnect(port)
		}
	}
}