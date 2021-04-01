package ch.scorpion.jabbah.graph.model

/**
 * A [NetCombiner] is a [Vertice] that combines [Nets][Net] to which its [OutputPort] are connected
 * to a larger [Net], which is used for negotiation of conflicting signals asserted by these [OutputPorts][OutputPort].
 */
interface NetCombiner : Vertice {

	/**
	 * Returns all [OutputPort]s of this [NetCombiner] whose [Net]s form a combined bigger [Net]
	 * with the specified [OutputPort]. The default is an empty [Collection].
	 */
	fun getCombinedNetOutputPorts(outputPort: OutputPort<*>): Collection<OutputPort<*>> = emptyList()
}