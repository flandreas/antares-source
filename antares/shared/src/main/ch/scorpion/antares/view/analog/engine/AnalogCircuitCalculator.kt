package ch.scorpion.antares.view.analog.engine

import ch.scorpion.antares.model.analog.AnalogNet
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.model.analog.AnalogVertice
import ch.scorpion.antares.view.analog.AnalogEdgeView
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.jabbah.execution.SignalHandler

class AnalogCircuitCalculator {

	companion object {
		private const val ITERATION_COUNT = 1_000
	}

	private var steps = 0

	private var stopMessage: String? = null

	private var subIterations: Int = 0

	/**
	 * Analyses the structure of an [AnalogGraphView] and returns all information needed for
	 * calculating electrical currents and voltages depending on the actual resistances in the circuit.
	 *
	 * @throws IllegalStateException in case of an invalid circuit
	 * */
	fun analyse(circuitView: AnalogGraphView): AnalogCircuitAnalysis =
		AnalogCircuitAnalyzer(circuitView).analyse()

	/**
	 * Calculates electrical currents and voltages in an [AnalogGraphView]. Prior to calculation,
	 * [analysis] must be called, typically at the start of the simulation.
	 */
	fun calculate(analysis: AnalogCircuitAnalysis, signalHandler: SignalHandler) {
		val elmList = analysis.circuitView.graph!!.elements.filterIsInstance<AnalogVertice>()

		if (elmList.isEmpty()) {
			return
		}

		for (iter in 1..1) {
			elmList.forEach { it.startIteration() }
			steps++

			for (subIter in 0 until ITERATION_COUNT) {
				analysis.converged = true
				subIterations = subIter

				analysis.startSubIteration()
				elmList.forEach { it.doStep(analysis) }

				if (stopMessage != null) {
					stop("Invalid matrix")
					return
				}

				if (analysis.isNonLinear) {
					if (analysis.converged && subIter > 0) {
						break
					}
					if (!analysis.luFactor()) {
						stop("Singular matrix")
						return
					}
				}

				analysis.luSolve()

				for (j in 0 until  analysis.circuitMatrixFullSize) {
					val ri = analysis.rowInfo[j]
					var res = if (ri.type == RowInfo.Type.Constant) {
						ri.value
					} else {
						analysis.circuitRightSide[ri.mapCol]
					}

					if (res.isNaN()) {
						analysis.converged = false
						break
					}

					if (j < analysis.nodeList.size - 1) {
						val cn = analysis.getCircuitNode(j + 1)
						for (k in 0 until cn.links.size) {
							val cnl = cn.links.elementAt(k)
							cnl.elem.setNodeVoltage(cnl.num, res)
						}
					} else {
						val ji = j - (analysis.nodeList.size - 1)
						analysis.voltageSources[ji].setCurrent(ji, res)
					}
				}

				if (!analysis.circuitView.isNonLinear) {
					break
				}
			}

			if (subIterations >= ITERATION_COUNT) {
				stop("Convergence failed")
				break
			} else {
				applySignals(analysis, signalHandler)
			}
		}
	}

	private fun applySignals(analysis: AnalogCircuitAnalysis, signalHandler: SignalHandler) {
		val netMap = mutableMapOf<AnalogNet, AnalogSignal>()
		analysis.circuitView.getEdgeViews()
			.map { it as AnalogEdgeView }
			.forEach { ev ->
				if (!netMap.containsKey(ev.net)) {
					netMap[ev.net!! as AnalogNet] = AnalogSignal(ev.getNodeVoltage(0))
				}
		}
		netMap.keys.forEach {
			it.setSignal(netMap[it], signalHandler)
		}
	}

	private fun stop(message: String) {
		stopMessage = message
	}
}