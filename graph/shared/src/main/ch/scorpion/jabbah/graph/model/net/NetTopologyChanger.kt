package ch.scorpion.jabbah.graph.model.net

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.OutputPort

/**
 * A [GraphElement] that can change the topology of [Net]s at execution time.
 * A typical example of such a [NetTopologyChanger] is a bi-directional switch connected
 * to two individual [Net]s.
 *
 * [NetTopologyChanger]s are added to [CombinedNet]s during net formation. An [OutputPort]
 * owing the formed [CombinedNet] will typically register itself as [NetTopologyChangeListener]
 * on these [NetTopologyChanger]. Upon change, the [OutputPort] will redo its net formation
 * to generated [CombinedNet]s that incorporate the changed topology.
 *
 * When a [NetTopologyChanger] changes the topology of a [Net], it sends a [NetTopologyChangeEvent]
 * to all registered [NetTopologyChangeListener]s. It must then make sure that [OutputPort]s that contain
 * that [NetTopologyChanger] in their [CombinedNet] will re-send their outgoing signal to reach possibly
 * new areas of the [Net] whose topology has changed. A [NetTopologyChanger] can achieve this most easily
 * by un-defining the values of its [OutputPort]s and request recalculation, which will in turn
 * result in re-establishing the [Net]'s value using the outgoing value of the dominant [OutputPort],
 * which is typically the one listing for topology changes.
 */
interface NetTopologyChanger {
	fun addNetTopologyChangeListener(listener: NetTopologyChangeListener)
	fun removeNetTopologyChangeListener(listener: NetTopologyChangeListener)
}

/** Provided by objects interested in being informed about [Net] topology changes.*/
typealias NetTopologyChangeListener = (NetTopologyChangeEvent) -> Unit

/**
 * Sent by [NetTopologyChanger] to registered [NetTopologyChangeListener]s after a [Net] topology has
 * changed during execution.
 */
data class NetTopologyChangeEvent(val source: NetTopologyChanger, val signalHandler: SignalHandler)