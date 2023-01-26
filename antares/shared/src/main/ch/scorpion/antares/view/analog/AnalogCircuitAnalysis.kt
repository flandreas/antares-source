package ch.scorpion.antares.view.analog

/**
 * Represents the result of analysing an [AnalogGraphView] as preparation of simulation.
 */
data class AnalogCircuitAnalysis(
	val circuitView: AnalogGraphView,
	val voltageNodeNetIds: List<Int>,
	val branches: List<AnalogCircuitBranch>,
	val groundNodeNetId: Int,
	val equationSystem : DynamicLinearEquationSystem
)