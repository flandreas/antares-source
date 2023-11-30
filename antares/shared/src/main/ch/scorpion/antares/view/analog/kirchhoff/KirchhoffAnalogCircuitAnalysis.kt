package ch.scorpion.antares.view.analog.kirchhoff

import ch.scorpion.antares.view.analog.AnalogCircuitAnalysis
import ch.scorpion.antares.view.analog.AnalogCircuitBranch
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem

data class KirchhoffAnalogCircuitAnalysis(
	override val circuitView: AnalogGraphView,
	val voltageNodeNetIds: List<Int>,
	val branches: List<AnalogCircuitBranch>,
	val groundNodeNetId: Int,
	val equationSystem: DynamicLinearEquationSystem
) : AnalogCircuitAnalysis