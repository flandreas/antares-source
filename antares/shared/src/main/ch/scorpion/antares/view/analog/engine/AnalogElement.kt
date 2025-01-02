package ch.scorpion.antares.view.analog.engine

import ch.scorpion.antares.model.analog.AnalogVertice
import ch.scorpion.jabbah.graph.view.Connection
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.antares.view.analog.AbstractAnalogVerticeView
import ch.scorpion.antares.view.analog.AnalogPortView
import ch.scorpion.jabbah.execution.SignalHandler

interface AnalogElement {

	val isNonLinear: Boolean

	/** The number of [AnalogPortView]s acting as a voltage source. */
	val voltageSourceCount: Int

	val postCount: Int

	/** Resets the state of this [AnalogElement] at simulation start.*/
	fun reset()

	/**
	 * Allocates [postCount] storage places for holding the values provided in [setNode].
	 * Cannot be done on instantiation because [AnalogPortView] are added during creation.
	 */
	fun allocateNodes()

	/** Associates a [postId] (smaller than [postCount]) with the ID of the node voltage variable in the matrix. */
	fun setNode(postId: Int, nodeId: Int)

	fun getNode(postId: Int): Int

	fun setVoltageSource(index: Int, sourceId: Int)

	fun getVoltageSource(index: Int): Int

	fun getPost(elem: GraphElementView<*>, postId: Int): Connection<*>?

	fun setNodeVoltage(postId: Int, voltage: Double)

	fun getNodeVoltage(postId: Int): Double

	fun setInternalCurrent(index: Int, current: Double)

	fun getInternalCurrent(): Double

	fun calculateCurrent() {}

	fun startIteration() {}

	fun doStep(analysis: AnalogCircuitAnalysis, signalHandler: SignalHandler) {}

	fun stamp(analysis: AnalogCircuitAnalysis)
}

/**
 * Mixed-in by [AnalogVertice]s as a delegate to implement [AnalogElement].
 */
class AnalogElementMixin(
	override val isNonLinear: Boolean = false,
	override var postCount: Int = 2
) : AnalogElement {

	private lateinit var vertice: AnalogVertice

	var voltageSource: Int = 0
		private set

	fun bindAnalogElement(vertice: AnalogVertice) {
		this.vertice = vertice
	}

	lateinit var nodes: Array<Int>
		private set

	var voltages: Array<Double> = emptyArray()
		private set

	private var internalCurrent: Double = 0.0

	override val voltageSourceCount: Int get() = vertice.voltageSourceCount

	override fun reset() {
		internalCurrent = 0.0
		for (i in voltages.indices) {
			voltages[i] = 0.0
		}
	}

	override fun allocateNodes() {
		nodes = Array(postCount) { 0 }
		voltages = Array(postCount) { 0.0 }
	}

	override fun setNode(postId: Int, nodeId: Int) {
		nodes[postId] = nodeId
	}

	override fun getNode(postId: Int): Int = nodes[postId]

	override fun setVoltageSource(index: Int, sourceId: Int) {
		// Currently only 1 supported
		voltageSource = sourceId
	}

	override fun getVoltageSource(index: Int): Int = voltageSource

	override fun setNodeVoltage(postId: Int, voltage: Double) {
		voltages[postId] = voltage
		calculateCurrent()
	}

	override fun getNodeVoltage(postId: Int): Double = voltages[postId]

	override fun setInternalCurrent(index: Int, current: Double) {
		this.internalCurrent = current
	}

	override fun getInternalCurrent(): Double = internalCurrent

	override fun getPost(elem: GraphElementView<*>, postId: Int): Connection<*> =
		Connection(elem as VerticeView<*>, vertice.getPort(postId + 1))

	override fun calculateCurrent() {
		vertice.calculateCurrent()
	}

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		vertice.stamp(analysis)
	}
}

/**
 * Forwards everything to the registered model [AnalogElement].
 * Used by view objects that can't inherit from [AbstractAnalogVerticeView].
 */
class AnalogElementProxy : AnalogElement {

	private lateinit var model: AnalogElement

	fun bind(model: AnalogElement) {
		this.model = model
	}

	override val isNonLinear: Boolean get() = model.isNonLinear

	override val voltageSourceCount: Int get() = model.voltageSourceCount

	override val postCount: Int get() = model.postCount

	override fun reset() {
		model.reset()
	}

	override fun allocateNodes() {
		model.allocateNodes()
	}

	override fun setNode(postId: Int, nodeId: Int) {
		model.setNode(postId, nodeId)
	}

	override fun getNode(postId: Int): Int = model.getNode(postId)

	override fun setVoltageSource(index: Int, sourceId: Int) {
		model.setVoltageSource(index, sourceId)
	}

	override fun getVoltageSource(index: Int): Int = model.getVoltageSource(index)

	override fun getPost(elem: GraphElementView<*>, postId: Int): Connection<*>? = model.getPost(elem, postId)

	override fun setNodeVoltage(postId: Int, voltage: Double) {
		model.setNodeVoltage(postId, voltage)
	}

	override fun getNodeVoltage(postId: Int): Double = model.getNodeVoltage(postId)

	override fun setInternalCurrent(index: Int, current: Double) {
		model.setInternalCurrent(index, current)
	}

	override fun getInternalCurrent(): Double = model.getInternalCurrent()

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		model.stamp(analysis)
	}
}