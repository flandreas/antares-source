package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.*
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem.Companion.MINUS_ONE
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem.Companion.ONE
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem.Companion.ZERO
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import kotlin.math.abs

/**
 * Calculates voltages/currents in an [AnalogGraphView] by applying Kirchhoff's laws.
 * See https://ultimateelectronicsbook.com/solving-circuit-systems/
 *
 * Most methods are public for unit testing purposes.
 */
object KirchhoffAnalogCircuitCalculator : AnalogCircuitCalculator {

	private val LOG by logger(KirchhoffAnalogCircuitCalculator::class)

	override fun analyse(circuitView: AnalogGraphView): AnalogCircuitAnalysis {
		LOG.debug("Analysing analog circuit")
		val groundNodeNetId: Int = identifyGroundNode(circuitView)
		val voltageNodeNetIds: List<Int> = labelVoltageNodes(circuitView, groundNodeNetId)
		val branches: List<AnalogCircuitBranch> = labelBranchCurrents(circuitView)
		val equationSystem = DynamicLinearEquationSystem(branches.size + voltageNodeNetIds.size)

		composeEquations(circuitView, voltageNodeNetIds, branches, groundNodeNetId, equationSystem)

		LOG.debug("Linear system: #current vars=${branches.size}, #voltages vars=${voltageNodeNetIds.size}, #equations=${equationSystem.equationCount}")

		return AnalogCircuitAnalysis(circuitView, voltageNodeNetIds, branches, groundNodeNetId, equationSystem)
	}

	override fun calculate(analysis: AnalogCircuitAnalysis, signalHandler: SignalHandler) {
		LOG.trace("Calculating analog circuit")
		with(analysis) {
			val result = BaseModule.linearEquationSystemSolver.solve(equationSystem.toLinearEquationSystem())
			applyResult(circuitView, voltageNodeNetIds, branches, groundNodeNetId, result)
		}
	}

	private fun applyResult(
		circuitView: AnalogGraphView,
		voltageNodes: List<Int>,
		branches: List<AnalogCircuitBranch>,
		groundNodeNetId: Int,
		result: DoubleArray,
	) {
		voltageNodes.forEachIndexed { index, netId ->
			val signal = AnalogSignal(result[branches.size + index])
			(circuitView.graph!!.withId(netId) as AnalogNet).setSignal(signal)
		}

		branches.forEachIndexed { branchId, branch ->
			branch.edgeViewIds.forEach {
				val edgeViewId = abs(it)
				val edgeView = circuitView.getWithId(abs(edgeViewId)) as AnalogEdgeView
				if (branch.isPositive(edgeViewId)) {
					edgeView.current = result[branchId]
				} else {
					edgeView.current = -result[branchId]
				}
			}
		}

		// Ground voltage
		(circuitView.graph!!.withId(groundNodeNetId) as AnalogNet).setSignal(AnalogSignal(0.0))
	}

	fun composeEquations(
		circuitView: AnalogGraphView,
		voltageNodes: List<Int>,
		branches: List<AnalogCircuitBranch>,
		groundNodeNetId: Int,
		equationSystem: DynamicLinearEquationSystem
	) {
		composeCurrentLawEquations(circuitView, branches, equationSystem)
		val currentLawEquationsCount = equationSystem.equationCount

		composeComponentConstituentEquations(circuitView, voltageNodes, branches, groundNodeNetId, equationSystem)
		val constituentEquationsCount = equationSystem.equationCount - currentLawEquationsCount

		removeLinearDependentKCL(equationSystem, currentLawEquationsCount)

		LOG.debug("KCL equations: $currentLawEquationsCount, Constituent Equations: $constituentEquationsCount")
	}

	private fun removeLinearDependentKCL(equationSystem: DynamicLinearEquationSystem, currentLawEquationsCount: Int) {
		if (equationSystem.equationCount > equationSystem.variableCount) {
			equationSystem.removeLinearlyDependentEquation(0 until currentLawEquationsCount)
		}
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
			val incomingEdgeView = if (it.portViewCount == 2) {
				circuitView.getEdgeView(it.getPort(2)!!)
			} else {
				circuitView.getEdgeView(it.getPort(1)!!)
			}
			identifyBranches(it, incomingEdgeView, circuitView, branches)
		}
		return branches
	}

	private fun getBranchStartVerticeViews(circuitView: AnalogGraphView): List<VerticeView<*>> {
		val batteryViews = circuitView.getDrawables { it is BatteryView || it is AnalogCircuitInOutView || it is AnalogPowerView }.map {it as VerticeView<*> }
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
		incomingEdgeView: EdgeView<*>?,
		graphView: GraphView,
		branches: MutableList<AnalogCircuitBranch>,
		branch: AnalogCircuitBranch? = null
	) {
		if (connectableView is AnalogBranchVerticeView<*> && incomingEdgeView != null) {
			val incomingConnection = incomingEdgeView.getConnection(connectableView)
			val oppositePortView = connectableView.getOppositeBranchPortView(incomingConnection!!.portView as AnalogPortView)
			if (oppositePortView != null) {
				val outgoingEdgeView = graphView.getEdgeView(oppositePortView.port)!!

				AnalogCircuitBranch.getBranchId(outgoingEdgeView, branches)?.let { existingBranchId ->
					if (branch != null) {
						val existingBranch = branches[existingBranchId]
						existingBranch.merge(branch)
						if (existingBranch !== branch) {
							branches.remove(branch)
						}
					}
				} ?: identifyBranchesRecursivelyImpl(connectableView, outgoingEdgeView, graphView, branches, branch)
			}
		} else if (connectableView is AnalogCircuitInOutView || connectableView is AnalogPowerView) {
			val outgoingEdgeView = graphView.getEdgeView(connectableView.getPort(1)!!)!!
			identifyBranchesRecursivelyImpl(connectableView, outgoingEdgeView, graphView, branches, branch)
		} else if (connectableView is NodeView<*>) {
			val edgeViews = connectableView.getEdgeViews().filter { it !== incomingEdgeView && !isConnectedToGroundView(it) }
			if (edgeViews.size == 1) {
				// Continue with the same branch by ignoring direct junctions to a GroundView
				if (AnalogCircuitBranch.getBranchId(edgeViews.first(), branches) == null) {
					identifyBranchesRecursivelyImpl(connectableView, edgeViews.first(), graphView, branches, branch)
				}
			} else {
				edgeViews.forEach {
					if (AnalogCircuitBranch.getBranchId(it, branches) == null) {
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

	private fun isConnectedToGroundView(edgeView: EdgeView<*>): Boolean =
		edgeView.origin?.connectableView is AnalogGroundView || edgeView.destination?.connectableView is AnalogGroundView

	/**
	 * Composes the Kirchhoff's Current Law (KCL) equations for every [AnalogNodeView].
	 *
	 * @param branches the value at index i contains the [AnalogEdgeView] IDs for branch current variable I(i)
	 * @param equationSystem the equation matrix to which the Current Law's equations are added
	 */
	fun composeCurrentLawEquations(
		circuitView: AnalogGraphView,
		branches: List<AnalogCircuitBranch>,
		equationSystem: DynamicLinearEquationSystem
	) {
		circuitView
			.getNodeViews()
			.filterNot { it.getEdgeViews().any { ev -> isConnectedToGroundView(ev) } }
			.forEach { nodeView ->
				val row = Array(equationSystem.variableCount) { ZERO }
				nodeView.getEdgeViews().forEach { edgeView ->
					val index = AnalogCircuitBranch.getBranchId(edgeView, branches)
						?: throw IllegalStateException("Every EdgeView must be part of a branch")
					val branch = branches[index]
					val isIncoming = if (branch.isPositive(edgeView.id)) {
						edgeView.destination?.connectableView?.id == nodeView.id
					} else {
						edgeView.origin?.connectableView?.id == nodeView.id
					}
					val factor = if (isIncoming) ONE else MINUS_ONE
					row[index] = factor
				}

				equationSystem.addEquation(row, ZERO)
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
		groundNodeNetId: Int,
		equationSystem: DynamicLinearEquationSystem
	) {
		circuitView
			.getDrawables { it is VerticeView<*> && it.model is AnalogVertice }
			.map { it.model as AnalogVertice }
			.forEach { vertice ->
				vertice.composeComponentConstituentEquation(circuitView, voltageNodes, branches, groundNodeNetId, equationSystem)
			}
	}
}