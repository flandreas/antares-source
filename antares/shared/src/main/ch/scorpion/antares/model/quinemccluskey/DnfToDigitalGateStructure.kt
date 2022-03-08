package ch.scorpion.antares.model.quinemccluskey

import ch.scorpion.antares.model.truthtable.TruthTable
import kotlin.math.abs

class DnfToDigitalGateStructure(
	private val dnf: DNF
) {
	companion object {

		/** Either a constant or a (possibly inverted) input value of the [TruthTable]. */
		data class Factor(
			val inputIndex: Int? = null,
			val inverted: Boolean? = null,
			val constant: Boolean? = null
		)

		/** A [List] of [Factor]s being AND-ed together.*/
		data class AndTerm(
			val factors: List<Factor>
		)
	}

	/** Returns the list of [AndTerm] being OR-ed together. */
	fun build(): List<AndTerm> {
		if (dnf.isEmpty()) {
			return listOf(AndTerm(listOf(Factor(constant = false))))
		}
		return dnf.map { buildAndTerm(it) }
	}

	private fun buildAndTerm(literals: List<Literal>): AndTerm {
		if (literals.isEmpty()) {
			return AndTerm(listOf(Factor(constant = true)))
		}
		return AndTerm(literals.map { buildFactor(it) })
	}

	private fun buildFactor(literal: Literal): Factor =
		Factor(
			inputIndex = abs(literal) - 1,
			inverted = literal > 0
		)
}