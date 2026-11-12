package io.antarescircuit.antares.view.synthesis

import io.antarescircuit.antares.model.PortCount
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType.And
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType.Nand
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType.Nor
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType.Or
import io.antarescircuit.antares.model.quinemccluskey.Literal
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.antares.model.truthtable.TruthTableService
import io.antarescircuit.jabbah.base.Translations
import kotlin.math.abs

/** A signal of a synthesized gate network (a tree per output). */
sealed interface GateSignal

/** The value of input column [inputIndex], taken from the inverter of the input if [inverted]. */
data class LiteralSignal(val inputIndex: Int, val inverted: Boolean) : GateSignal

data class ConstantSignal(val value: Boolean) : GateSignal

/**
 * A logic gate. A gate with a single input is an inverter, built as a two-input gate with tied inputs.
 * Uses identity equality, so every instance becomes a gate of its own.
 */
class GateNode(val type: NonUnaryLogicGateType, val inputs: List<GateSignal>) : GateSignal

/**
 * Creates the gate network of each output column of a [TruthTable] from its minimized
 * sum of products, or its minimized product of sums if [productOfSums].
 * @param universalGate `null` for AND/OR gates, [Nand] or [Nor] to build every gate (including inverters) from it
 * @param twoInputGatesOnly splits wider gates into balanced trees of two-input gates
 */
class GateNetworkFactory(
	private val productOfSums: Boolean,
	val universalGate: NonUnaryLogicGateType?,
	private val twoInputGatesOnly: Boolean,
	private val truthTableService: TruthTableService = TruthTableService(),
) {

	init {
		require(universalGate == null || universalGate == Nand || universalGate == Nor)
	}

	/**
	 * Returns the root [GateSignal] of each output column.
	 * @throws CircuitFromTruthTableBuilderError if a gate would exceed the max. input count
	 */
	fun create(truthTable: TruthTable): List<GateSignal> =
		if (productOfSums) {
			truthTableService.generateCnfs(truthTable).map { cnf ->
				when {
					cnf.isEmpty() -> ConstantSignal(true)
					cnf.any { it.isEmpty() } -> ConstantSignal(false)
					else -> and(cnf.map { clause -> or(clause.map(::literal)) })
				}
			}
		} else {
			truthTableService.generateDnfs(truthTable).map { dnf ->
				when {
					dnf.isEmpty() -> ConstantSignal(false)
					dnf.any { it.isEmpty() } -> ConstantSignal(true)
					else -> or(dnf.map { term -> and(term.map(::literal)) })
				}
			}
		}

	// Positive literals denote inverted inputs (see TruthTable.getMinTerm)
	private fun literal(literal: Literal): GateSignal = LiteralSignal(abs(literal) - 1, literal > 0)

	private fun and(inputs: List<GateSignal>): GateSignal =
		combine(inputs, "antares.synthesis.maxAndInputCountExceeded.error", ::and) {
			when (universalGate) {
				null -> GateNode(And, it)
				Nand -> invert(GateNode(Nand, it))
				else -> GateNode(Nor, it.map(::invert))
			}
		}

	private fun or(inputs: List<GateSignal>): GateSignal =
		combine(inputs, "antares.synthesis.maxOrInputCountExceeded.error", ::or) {
			when (universalGate) {
				null -> GateNode(Or, it)
				Nand -> GateNode(Nand, it.map(::invert))
				else -> invert(GateNode(Nor, it))
			}
		}

	/** Builds a single gate with [build], or a balanced tree of [self] if two-input gates are required. */
	private fun combine(
		inputs: List<GateSignal>,
		errorKey: String,
		self: (List<GateSignal>) -> GateSignal,
		build: (List<GateSignal>) -> GateSignal,
	): GateSignal = when {
		inputs.size == 1 -> inputs.first()
		twoInputGatesOnly && inputs.size > 2 -> {
			val mid = (inputs.size + 1) / 2
			self(listOf(self(inputs.subList(0, mid)), self(inputs.subList(mid, inputs.size))))
		}
		inputs.size > PortCount.entries.last().count -> throw CircuitFromTruthTableBuilderError(Translations.getString(errorKey))
		else -> build(inputs)
	}

	/** Inverts without adding gates where possible: literals flip, double inversions cancel. */
	private fun invert(signal: GateSignal): GateSignal = when (signal) {
		is LiteralSignal -> signal.copy(inverted = !signal.inverted)
		is ConstantSignal -> ConstantSignal(!signal.value)
		is GateNode -> if (signal.type == universalGate && signal.inputs.size == 1) {
			signal.inputs.first()
		} else {
			GateNode(universalGate!!, listOf(signal))
		}
	}
}
