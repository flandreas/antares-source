package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.oscilloscope.Oscilloscope
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.EdgeView

/**
 * Strategy for determining names for [OscilloscopeProbeView] when they are connected to an [EdgeView].
 * Implementations must only produce names that are unique in an [Oscilloscope].
 */
interface OscilloscopeProbeNameStrategy {

	/**
	 * Tries to determine a name for a [OscilloscopeProbeVerticeView] that has been connected
	 * to the specified [EdgeView]
	 *
	 * @return the determined name, or `null` if no suitable name could be determined
	 */
	fun getConnectedName(oscilloscope: Oscilloscope, edgeView: EdgeView<*>): String?
}

/**
 * Default [OscilloscopeProbeNameStrategy] that uses the following priorities when
 * determining a name:
 *
 * 1. Name of destination [Port] of [EdgeView]
 * 2. Name of origin [Port] of [EdgeView]
 * 3. Name of first suitable input [Port] of [Net]
 * 4. Name of first suitable output [Port] of [Net]
 */
open class OscilloscopeProbeNameStrategyImpl : OscilloscopeProbeNameStrategy {

	override fun getConnectedName(oscilloscope: Oscilloscope, edgeView: EdgeView<*>): String? {
		ifFree(oscilloscope, destPortNameOfEdgeView(edgeView))?.let { return it }
		ifFree(oscilloscope, origPortNameOfEdgeView(edgeView))?.let { return it }

		inputPortOfNet(oscilloscope, edgeView)?.let { return it }
		outputPortOfNet(oscilloscope, edgeView)?.let { return it }

		return null
	}

	private fun ifFree(oscilloscope: Oscilloscope, name: String?): String? =
		if (isFreeAndShortEnough(oscilloscope, name)) name else null

	private fun isFreeAndShortEnough(oscilloscope: Oscilloscope, name: String?): Boolean =
		name != null && name.length <= OscilloscopeProbeVerticeView.MAX_PROBE_NAME_LENGTH && !oscilloscope.hasPort(name)

	private fun destPortNameOfEdgeView(edgeView: EdgeView<*>): String? =
		portName(edgeView.destination?.port)

	private fun origPortNameOfEdgeView(edgeView: EdgeView<*>): String? =
		portName(edgeView.origin?.port)

	private fun inputPortOfNet(oscilloscope: Oscilloscope, edgeView: EdgeView<*>): String? =
		portTypeOfNet(oscilloscope, edgeView) { it.portType.isInput }

	private fun outputPortOfNet(oscilloscope: Oscilloscope, edgeView: EdgeView<*>): String? =
		portTypeOfNet(oscilloscope, edgeView) { it.portType.isOutput }

	private fun portTypeOfNet(oscilloscope: Oscilloscope, edgeView: EdgeView<*>, typeCond: (Port<*>) -> Boolean): String? =
		edgeView.model.ports
			.filter { typeCond.invoke(it) }
			.firstOrNull { isFreeAndShortEnough(oscilloscope, portName(it)) }
			?.name

	protected open fun portName(port: Port<*>?): String? =
		when (port?.owner) {
			is GraphPort<*> -> port.owner?.name
			else -> port?.name
		}
}