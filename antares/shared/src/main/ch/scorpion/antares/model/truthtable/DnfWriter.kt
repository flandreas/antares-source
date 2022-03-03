package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.model.quinemccluskey.Literal
import kotlin.math.abs

class DnfWriter(
	private val truthTable: TruthTable,
	private val dnf: List<List<Literal>>
) {

	fun write(outputColumn: Int): String {
		val builder = StringBuilder()

		if (dnf.isEmpty()) {
			builder.append("0")
		} else {
			for (term in dnf) {
				val termBuilder = StringBuilder()
				if (term.isEmpty()) {
					if (builder.isNotEmpty()) {
						termBuilder.append(" + ")
					}
					termBuilder.append("1")
				} else {
					for (literal in term) {
						if (builder.isNotEmpty() && termBuilder.isEmpty()) {
							termBuilder.append(" + ")
						}
						termBuilder.append(truthTable.getColumnName(abs(literal) - 1))
						if (literal > 0) {
							termBuilder.append("'")
						}
					}
				}
				builder.append(termBuilder.toString())
			}
		}

		return "${truthTable.getColumnName(outputColumn)} = $builder"
	}
}