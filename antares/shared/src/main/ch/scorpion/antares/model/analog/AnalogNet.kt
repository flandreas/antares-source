package ch.scorpion.antares.model.analog

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.net.NetImpl
import ch.scorpion.antares.view.analog.AnalogEdgeView

class AnalogNet : NetImpl<AnalogSignal>() {

	companion object {
		private val LOG by logger(AnalogNet::class)
	}

	/** Maps IDs of [AnalogEdgeView]s to the corresponding [Posts]*/
	private val postMap = mutableMapOf<Int, Posts>()

	override val signal: AnalogSignal get() = super.signal ?: AnalogSignal.ZERO

	override fun cloneEmpty(): Net<AnalogSignal> = AnalogNet()

	override fun setSignal(
		signal: AnalogSignal?,
		origin: OutputPort<AnalogSignal>,
		immediatePort: OutputPort<AnalogSignal>,
		signalHandler: SignalHandler,
		force: Boolean
	) {
		// Don't call super.setSignal() to avoid requestActingAfter()
		setSignal(signal, signalHandler)
	}

	fun setSignal(signal: AnalogSignal?, signalHandler: SignalHandler) {
		signal?.let {
			LOG.trace("Set AnalogSignal ${it.voltage} on AnalogNet $id")
			updateSignal(it)
			ports.map { port -> port as AnalogPort }.forEach { port -> port.handleAnalogSignalChanged(it, signalHandler) }
		}
	}

	fun setNode(edgeViewId: Int, postId: Int, nodeId: Int) {
		postMap.getOrPut(edgeViewId) { Posts() }.apply {
			nodes[postId] = nodeId
		}
	}

	fun getNode(edgeViewId: Int, postId: Int): Int =
		postMap[edgeViewId]?.let { it.nodes[postId] } ?: 0

	fun setNodeVoltage(edgeViewId: Int, postId: Int, voltage: Double) {
		postMap.getOrPut(edgeViewId) { Posts() }.apply {
			voltages[postId] = voltage
		}
	}

	fun getNodeVoltage(edgeViewId: Int, postId: Int): Double =
		postMap[edgeViewId]?.let { it.voltages[postId] } ?: 0.0

	fun setCurrent(edgeViewId: Int, current: Double) {
		postMap[edgeViewId]!!.current = current
	}

	fun getCurrent(edgeViewId: Int): Double =
		postMap[edgeViewId]?.current ?: 0.0

	private data class Posts(
		val nodes: Array<Int> = Array(2) { 0 },
		var voltages: Array<Double> = Array(2) { 0.0 },
		var current: Double = 0.0
	) {
		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (other == null || this::class != other::class) return false

			other as Posts

			if (!nodes.contentEquals(other.nodes)) return false
			if (!voltages.contentEquals(other.voltages)) return false

			return true
		}

		override fun hashCode(): Int {
			var result = nodes.contentHashCode()
			result = 31 * result + voltages.contentHashCode()
			return result
		}
	}
}