package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogVertice
import ch.scorpion.antares.view.analog.falstad.FalstadAnalogCircuitAnalysis
import ch.scorpion.jabbah.graph.view.Connection
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.VerticeView

interface AnalogElement {

	val isNonLinear: Boolean

	/** The number of [AnalogPortView]s acting as a voltage source. */
	val voltageSourceCount: Int

	val postCount: Int

	/**
	 * Allocates [postCount] storage places for holding the values provided in [setNode].
	 * Cannot be done on instantiation because [AnalogPortView] are added during creation.
	 */
	fun allocateNodes()

	/** Associates a [postId] (smaller than [postCount]) with the ID of the node voltage variable in the matrix. */
	fun setNode(postId: Int, nodeId: Int)

	fun setVoltageSource(index: Int, sourceId: Int)

	fun getPost(elem: GraphElementView<*>, postId: Int): Connection<*>?

	fun setNodeVoltage(postId: Int, voltage: Double)

	fun getNodeVoltage(postId: Int): Double

	fun setCurrent(index: Int, current: Double)

	fun calculateCurrent() {}

	fun startIteration() {}

	fun doStep() {}

	fun stamp(analysis: FalstadAnalogCircuitAnalysis)
}

/**
 * Mixed-in by [AnalogVertice]s as a delegate to implement [AnalogElement].
 */
class AnalogElementMixin(
	override val isNonLinear: Boolean = false,
	override val postCount: Int = 2
) : AnalogElement {

	private lateinit var vertice: AnalogVertice

	var voltageSource: Int = 0
		private set

	fun bindAnalogElement(vertice: AnalogVertice) {
		this.vertice = vertice
	}

	lateinit var nodes: Array<Int>
		private set

	private lateinit var voltages: Array<Double>

	override val voltageSourceCount: Int get() = vertice.voltageSourceCount

	override fun allocateNodes() {
		nodes = Array(postCount) { 0 }
		voltages = Array(postCount) { 0.0 }
	}

	override fun setNode(postId: Int, nodeId: Int) {
		nodes[postId] = nodeId
	}

	override fun setVoltageSource(index: Int, sourceId: Int) {
		// Currently only 1 supported
		voltageSource = sourceId
	}

	override fun setNodeVoltage(postId: Int, voltage: Double) {
		voltages[postId] = voltage
		calculateCurrent()
	}

	override fun getNodeVoltage(postId: Int): Double = voltages[postId]

	override fun setCurrent(index: Int, current: Double) {
		// empty so far
	}

	override fun getPost(elem: GraphElementView<*>, postId: Int): Connection<*>? =
		Connection(elem as VerticeView<*>, vertice.getPort(postId + 1))

	override fun calculateCurrent() {
		vertice.calculateCurrent()
	}

	override fun stamp(analysis: FalstadAnalogCircuitAnalysis) {
		vertice.stamp(analysis)
	}
}