package ch.scorpion.antares.view.analog.falstad

import ch.scorpion.antares.model.analog.AnalogNet
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.model.analog.AnalogVertice
import ch.scorpion.antares.view.analog.AnalogCircuitCalculator
import ch.scorpion.antares.view.analog.AnalogEdgeView
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.jabbah.execution.SignalHandler

class FalstadAnalogCircuitCalculator : AnalogCircuitCalculator<FalstadAnalogCircuitAnalysis> {

	companion object {
		private const val ITERATION_COUNT = 1_000
	}

	private var steps = 0

	private var converged = true

	private var stopMessage: String? = null

	private var subIterations: Int = 0

	override fun analyse(circuitView: AnalogGraphView): FalstadAnalogCircuitAnalysis =
		FalstadAnalogCircuitAnalyzer(circuitView).analyse()

	override fun calculate(analysis: FalstadAnalogCircuitAnalysis, signalHandler: SignalHandler) {
		val elmList = analysis.circuitView.graph!!.elements.filterIsInstance<AnalogVertice>()

		if (elmList.isEmpty()) {
			return
		}

		for (iter in 1..1) {
			elmList.forEach { it.startIteration() }
			steps++

			for (subIter in 0 until ITERATION_COUNT) {
				converged = true
				subIterations = subIter

				analysis.startSubIteration()
				elmList.forEach { it.doStep() }

				if (stopMessage != null) {
					stop("Invalid matrix")
					return
				}

				if (analysis.isNonLinear) {
					if (converged && subIter > 0) {
						break
					}
					if (!analysis.luFactor()) {
						stop("Singular matrix")
						return
					}
				}

				analysis.luSolve()

				for (j in 0 until  analysis.matrixSize) {
					val ri = analysis.rowInfo[j]
					var res = if (ri.type == RowInfo.Type.Constant) {
						ri.value
					} else {
						analysis.circuitRightSide[ri.mapCol]
					}

					if (res.isNaN()) {
						converged = false
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

	private fun applySignals(analysis: FalstadAnalogCircuitAnalysis, signalHandler: SignalHandler) {
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