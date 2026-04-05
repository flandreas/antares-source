package io.antarescircuit.antares.view.analog.engine

import io.antarescircuit.jabbah.graph.view.Connection

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