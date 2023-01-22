package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogGround
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.model.analog.AnalogTwoPortVertice
import ch.scorpion.antares.model.analog.Battery
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.collection.indexOfFirstOrNull
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.math.LinearEquationSystem
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.port.PortView

/**
 * Calculates voltages/currents in an [AnalogGraphView] by applying Kirchhoff's laws.
 * See https://ultimateelectronicsbook.com/solving-circuit-systems/
 *
 * Most methods are public for unit testing purposes.
 */
object KirchhoffAnalogCircuitCalculator : AnalogCircuitCalculator {

	private val LOG by logger(KirchhoffAnalogCircuitCalculator::class)

	override fun calculate(circuitView: AnalogGraphView, signalHandler: SignalHandler) {
		val groundNodeNetId: Int = identifyGroundNode(circuitView)
		val voltageNodeNetIds: List<Int> = labelVoltageNodes(circuitView, groundNodeNetId)
		val branches: List<AnalogCircuitBranch> = labelBranchCurrents(circuitView)
		val equationSystem = LinearEquationSystem(branches.size + voltageNodeNetIds.size)

		LOG.debug("Current variables: ${branches.size}, voltage node variables: ${voltageNodeNetIds.size}")

		composeEquations(circuitView, voltageNodeNetIds, branches, groundNodeNetId, equationSystem)

		val result = BaseModule.linearEquationSystemSolver.solve(equationSystem)
	}

	fun composeEquations(
		circuitView: AnalogGraphView,
		voltageNodes: List<Int>,
		branches: List<AnalogCircuitBranch>,
		groundNodeNetId: Int,
		equationSystem: LinearEquationSystem
	) {
		composeCurrentLawEquations(circuitView, branches, groundNodeNetId, equationSystem)
		val currentLawEquationsCount = equationSystem.equationCount
		composeComponentConstituentEquations(circuitView, voltageNodes, branches, equationSystem)
		val constituentEquationsCount = equationSystem.equationCount - currentLawEquationsCount

		LOG.debug("KCL equations: $currentLawEquationsCount, Constituent Equations: $constituentEquationsCount")
	}

	/**
	 * Identifies the [Net] designated as the ground node. This can either be defined by a
	 * single explicit [AnalogGround] component or the common negative terminal of [Batteries][Battery]
	 * all connected to the same [Net].
	 *
	 * @return the model ID of the [Net] identified as ground node
	 * @throws [IllegalStateException] if no ground node could be identified. The exception's message
	 * contains a translated description of the cause, such as "More than one signal ground".
	 */
	fun identifyGroundNode(circuitView: AnalogGraphView): Int =
		findUniqueGroundNode(circuitView)
		?: findUniqueBatteryGroundNode(circuitView)
		?: throw IllegalArgumentException("antares.analogCalc.noGroundNode.error.msg")

	private fun findUniqueGroundNode(circuitView: AnalogGraphView): Int? {
		val grounds = circuitView.graph?.elements
			?.filterIsInstance<AnalogGround>()
			?.filter { it.getPort<AnalogSignal>().isConnected }

		if (grounds != null) {
			if (grounds.size > 1) {
				throw IllegalArgumentException(Translations.getString("antares.analogCalc.moreThanOneGround.error.msg"))
			}
			if (grounds.size == 1) {
				return grounds.first().getPort<AnalogSignal>().net!!.id
			}
		}

		return null
	}

	private fun findUniqueBatteryGroundNode(circuitView: AnalogGraphView): Int? {
		val batteries = circuitView.graph!!.elements
			.filterIsInstance<Battery>()
			.filter { it.negativePort.isConnected }

		if (batteries.isEmpty()) {
			return null
		}
		if (batteries.size == 1) {
			return batteries.first().negativePort.net!!.id
		}
		if (batteries.all { it.negativePort.net === batteries.first().negativePort.net }) {
			return batteries.first().negativePort.net!!.id
		}

		throw IllegalArgumentException(Translations.getString("antares.analogCalc.multipleBatteriesAtDifferentNets.error.msg"))
	}

	/**
	 * Labels all non-ground voltages by mapping a [List] index to the ID of a [Net].
	 *
	 * @param groundNetId the ID of the [Net] representing ground, which is excluded from the resulting [List]
	 * @return the resulting mapping, where the [List] index identifies the voltage variable V(i),
	 * and the [List] value identifies the ID of the [Net] having that voltage
	 */
	fun labelVoltageNodes(circuitView: AnalogGraphView, groundNetId: Int): List<Int> =
		circuitView
			.graph!!.elements
			.filterIsInstance<Net<*>>()
			.filter { it.id != groundNetId }
			.map { it.id }

	/**
	 * Labels all branches by mapping a [List] index the the [List] of the ID's of [AnalogEdgeView]s of that branch.
	 * Positive current values represent current flowing from [AnalogEdgeView.origin] to [AnalogEdgeView.destination]
	 * and are represented for a particular [AnalogEdgeView] in the branch by a positive ID, while
	 * negative currents are represented by an artificially negative ID.
	 */
	fun labelBranchCurrents(circuitView: AnalogGraphView): List<AnalogCircuitBranch> {
		val branches = mutableListOf<AnalogCircuitBranch>()
		getBranchStartVerticeViews(circuitView).forEach {
			val incomingEdgeView = circuitView.getEdgeView(it.getPort(2)!!)!!
			identifyBranches(it, incomingEdgeView, circuitView, branches)
		}
		return branches
	}

	private fun getBranchStartVerticeViews(circuitView: AnalogGraphView): List<VerticeView<*>> {
		val batteryViews = circuitView.getDrawables { it is BatteryView }.map {it as VerticeView<*> }
		if (batteryViews.isEmpty()) {
			throw IllegalStateException(Translations.getString("antares.analogCalc.noStartComponentFound.error.msg"))
		}
		return batteryViews
	}

	/**
	 * Identifies all [AnalogCircuitBranch]es of a [GraphView] starting in [connectableView] at its outgoing
	 * [EdgeView] opposite to [incomingEdgeView]. Executed recursively at every [ConnectableView] found
	 * along the way by collecting the identified branches in [branches].
	 *
	 * @param connectableView the [ConnectableView] from which the [AnalogCircuitBranch] is expanded
	 * @param incomingEdgeView the [EdgeView] incoming at [connectableView]. Expansion takes place
	 * at the opposite [EdgeView], which depends on the type of [connectableView]
	 * @param branches collects the resulting [AnalogCircuitBranch]es
	 * @param branch [AnalogCircuitBranch] to be continued at the outgoing [EdgeView]s of [connectableView].
	 * If `null`, a new [AnalogCircuitBranch] is started
	 */
	fun identifyBranches(
		connectableView: ConnectableView,
		incomingEdgeView: EdgeView<*>,
		graphView: GraphView,
		branches: MutableList<AnalogCircuitBranch>,
		branch: AnalogCircuitBranch? = null
	) {
		if (connectableView is VerticeView<*> && connectableView.portViewCount == 2) {
			val incomingConnection = incomingEdgeView.getConnection(connectableView)
			val oppositePortView = getOppositePortViewOfVerticeView(connectableView, incomingConnection!!.portView!!)
			val outgoingEdgeView = graphView.getEdgeView(oppositePortView.port)!!

			if (getBranchId(outgoingEdgeView, branches) == null) {
				identifyBranchesRecursivelyImpl(connectableView, outgoingEdgeView, graphView, branches, branch)
			} else {
				// TODO: Merge branches
			}
		} else if (connectableView is NodeView<*>) {
			val edgeViews = connectableView.getEdgeViews().filter { it !== incomingEdgeView && !isConnectedToGroundView(it) }
			if (edgeViews.size == 1) {
				// Continue with the same branch by ignoring direct junctions to a GroundView
				if (getBranchId(edgeViews.first(), branches) == null) {
					identifyBranchesRecursivelyImpl(connectableView, edgeViews.first(), graphView, branches, branch)
				}
			} else {
				edgeViews.forEach {
					if (getBranchId(it, branches) == null) {
						// Start a new branch
						identifyBranchesRecursivelyImpl(connectableView, it, graphView, branches, null)
					}
				}
			}
		}
	}

	/**
	 * Expands an [AnalogCircuitBranch] by following [outgoingEdgeView] starting at [connectableView].
	 */
	private fun identifyBranchesRecursivelyImpl(
		connectableView: ConnectableView,
		outgoingEdgeView: EdgeView<*>,
		graphView: GraphView,
		branches: MutableList<AnalogCircuitBranch>,
		branch: AnalogCircuitBranch? = null
	) {
		val currentBranch = branch ?: AnalogCircuitBranch().also { branches.add(it) }

		val connection = outgoingEdgeView.getConnection(connectableView)!!
		val nextIncomingConnection = outgoingEdgeView.getOppositeConnection(connection)!!
		currentBranch.add(connectableView, outgoingEdgeView)

		identifyBranches(
			nextIncomingConnection.connectableView,
			outgoingEdgeView,
			graphView,
			branches,
			currentBranch
		)
	}

	private fun getOppositePortViewOfVerticeView(verticeView: VerticeView<*>, portView: PortView<*>): PortView<*> =
		verticeView.getPortViews().first { it !== portView }

	private fun getBranchId(edgeView: EdgeView<*>, branches: List<AnalogCircuitBranch>): Int? =
		branches.indexOfFirstOrNull { it.containsId(edgeView.id) }

	private fun isConnectedToGroundView(edgeView: EdgeView<*>): Boolean =
		edgeView.origin?.connectableView is AnalogGroundView || edgeView.destination?.connectableView is AnalogGroundView

	/**
	 * Composes the Kirchhoff's Current Law equations for every [AnalogNodeView].
	 *
	 * @param branches the value at index i contains the [AnalogEdgeView] IDs for branch current variable I(i)
	 * @param groundNodeNetId the ID of the ground [Net] to be skipped
	 * @param equationSystem the equation matrix to which the Current Law's equations are added
	 */
	fun composeCurrentLawEquations(
		circuitView: AnalogGraphView,
		branches: List<AnalogCircuitBranch>,
		groundNodeNetId: Int,
		equationSystem: LinearEquationSystem
	) {
		circuitView
			.getNodeViews()
			.filter { it.net!!.id != groundNodeNetId }
			.forEach { nodeView ->
				val row = DoubleArray(equationSystem.numberOfVariables) { 0.0 }

				nodeView.getEdgeViews().forEach { edgeView ->
					val factor = if (edgeView.origin?.connectableView?.id == nodeView.id) -1.0 else 1.0
					val index = getBranchId(edgeView, branches)
						?: throw IllegalStateException("Every EdgeView must be part of a branch")
					row[index] = factor
				}

				equationSystem.addEquation(row, 0.0)
			}
	}

	/**
	 * Composes the Component Constituent Equations for each two-terminal component, which
	 * relates is voltage difference and its branch current according to Ohm's Law.
	 *
	 * @param branches the value at index i contains the [AnalogEdgeView] IDs for branch current variable I(i)
	 * @param voltageNodes the [List] index identifies the voltage variable V(i), and the [List] value
	 * identifies the ID of the [Net] having that voltage
	 * @param equationSystem the equation matrix to which the Current Law's equations are added
	 * @return the list of coefficient matrix rows for the voltage variables V(i)
	 */
	fun composeComponentConstituentEquations(
		circuitView: AnalogGraphView,
		voltageNodes: List<Int>,
		branches: List<AnalogCircuitBranch>,
		equationSystem: LinearEquationSystem
	) {
		circuitView
			.getDrawables { it is VerticeView<*> && it.model is AnalogTwoPortVertice }
			.map { it as VerticeView<*> }
			.forEach { vv ->
				val edgeView = circuitView.getEdgeView(vv.model.getPort<AnalogSignal>(1))!!
				val currentVariableIndex = getBranchId(edgeView, branches)!!
				(vv.model as AnalogTwoPortVertice).composeComponentConstituentEquation(
					voltageNodes, branches, currentVariableIndex, equationSystem)
			}
	}
}