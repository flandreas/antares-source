package ch.scorpion.antares.view.analog.engine

import ch.scorpion.antares.view.analog.*
import ch.scorpion.jabbah.base.collection.indexOfFirstOrNull
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.Connection
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.net.node.NodeView

class AnalogCircuitAnalyzer(private val circuitView: AnalogGraphView) {

	companion object {
		private val LOG by logger(AnalogCircuitAnalyzer::class)
	}

	private val nodeList = mutableListOf<CircuitNode>()

	private lateinit var voltageSources: Array<AnalogElement?>

	private var voltageSourceCount = 0

	fun analyse(): AnalogCircuitAnalysis {
		LOG.trace("Analyzing analog circuit '${circuitView.graph?.name?.value}'")
		createNodeList()

		val analysis = createAnalysis()

		// Stamp linear circuit elements
		for (elem in circuitView.analogElementViews) {
			elem.stamp(analysis)
		}

		analysis.simplify()

		return analysis
	}

	private fun createNodeList() {
		createGroundNode()

		var vsCount = createNodeAndVoltageSources()
		voltageSources = Array(vsCount) { null }

		vsCount = 0
		for (elem in circuitView.analogElementViews) {
			for (j in 0 until elem.voltageSourceCount) {
				voltageSources[vsCount] = elem
				elem.setVoltageSource(j, vsCount++)
			}
		}
		voltageSourceCount = vsCount
	}

	private fun createGroundNode() {
		var gotGround = false
		var batteryView: BatteryView? = null

		// Look for voltage or ground element
		for (elem in circuitView.analogElementViews) {
			if (elem is AnalogGroundView) {
				gotGround = true
				break
			}
			if (batteryView == null && elem is BatteryView) {
				batteryView = elem
			}
		}

		// If no ground, the the voltage element's first port is ground
		if (!gotGround && batteryView != null) {
			nodeList.add(CircuitNode(Connection(batteryView, batteryView.model.negativePort)))
		} else {
			// otherwise allocate extra node for ground
			nodeList.add(CircuitNode(null))
		}
	}

	private fun createNodeAndVoltageSources(): Int {
		var vsCount = 0

		for (elem in circuitView.analogElementViews) {
			elem.allocateNodes()

			if (elem is AnalogEdgeView) {
				elem.origin?.let { matchNode(elem, 0, it.connectableView, it.port) }
				elem.destination?.let { matchNode(elem, 1, it.connectableView, it.port) }
			} else if (elem !is NodeView<*>) {
				for (postId in 0 until elem.postCount) {
					val connection = elem.getPost(elem as GraphElementView<*>, postId)!!
					matchNode(elem, postId, connection.connectableView, connection.port)
				}
			}

			vsCount += elem.voltageSourceCount
		}

		return vsCount
	}

	private fun matchNode(owner: AnalogElement, postId: Int, connectableView: ConnectableView, port: Port<*>?) {
		val nodeIndex = nodeList.indexOfFirstOrNull {
			if (it.connection?.port == null && port == null) {
				// EdgeViews meeting at a NodeView
				it.connection?.connectableView === connectableView
			} else {
				it.connection?.connectableView === connectableView && it. connection.port === port
			}
			//it.connection?.connectableView === connectableView && it. connection.port === port
		}

		if (nodeIndex == null) {
			// No matching post found. Create a new one
			val node = CircuitNode(Connection(connectableView, port))
			node.links.add(CircuitNodeLink(postId, owner))
			owner.setNode(postId, nodeList.size)
			nodeList.add(node)
		} else {
			// Matching post found. Link to it
			nodeList[nodeIndex].links.add(CircuitNodeLink(postId, owner))
			owner.setNode(postId, nodeIndex)
			// If it's the ground node, make sure the node voltage is 0, it may not get set later
			if (nodeIndex == 0) {
				owner.setNodeVoltage(postId, 0.0)
			}
		}
	}

	private fun createAnalysis(): AnalogCircuitAnalysis =
		AnalogCircuitAnalysis(circuitView, nodeList, voltageSources as Array<AnalogElement>)
}