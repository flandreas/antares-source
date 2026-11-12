package io.antarescircuit.jabbah.graph.model.vertice

import io.antarescircuit.jabbah.graph.model.Vertice

/**
 * A [Vertice], such as a tunnel, that is implicitly connected with all other [NamedConnector]s
 * of the same [Vertice.name] and scope.
 */
interface NamedConnector : Vertice {

	/** `true` if connected with [NamedConnector]s in all graphs of the library, `false` if only within its own graph. */
	val isGlobal: Boolean
}
