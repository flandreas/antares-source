package ch.scorpion.antares.view.analog.engine

import ch.scorpion.jabbah.graph.view.Connection

class CircuitNode(
	val connection: Connection<*>?,
	val internal: Boolean = false
) {
	val links = mutableListOf<CircuitNodeLink>()
}

data class CircuitNodeLink(
	val num: Int,
	val elem: AnalogElement
)