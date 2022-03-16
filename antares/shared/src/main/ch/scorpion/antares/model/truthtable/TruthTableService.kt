package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.model.expression.BooleanExpression
import ch.scorpion.antares.model.expression.BooleanExpressionNotation
import ch.scorpion.antares.model.expression.StandardBooleanExpressionWriter
import ch.scorpion.antares.model.quinemccluskey.DnfToBooleanExpression
import ch.scorpion.antares.model.quinemccluskey.minimizeToDNF
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule

/** Domain-level service for dealing with [TruthTable]. */
class TruthTableService(
	private val properties: Properties = BaseModule.properties
) {

	companion object {
		const val PROP_TRUTH_TABLE_MAX_INPUTS = "antares.truthTable.maxInputs"
		const val PROP_TRUTH_TABLE_MAX_OUTPUTS = "antares.truthTable.maxOutputs"

		private val OUTPUT_REGEX = "!?[a-zA-Z][0-9a-zA-Z]*|!\\([a-zA-Z][0-9a-zA-Z]*\\)".toRegex()
	}

	/**
	 * Creates a new [TruthTable] based on the user-provided input arguments.
	 * Performs all the necessary validations and throws an exception when any
	 * domain requirement is violated by the given arguments
	 *
	 * @param name the name of the [TruthTable] in the current language
	 * @param inputNames a comma-separated list of the input names
	 * @param outputNames a comma-separated list of the output names
	 * @throws [IllegalArgumentException] if a domain requirement is violated. The exception message is translated
	 * into the user's language and can be displayed in the UI.
	 */
	fun createWithUserInput(
		name: String,
		inputNames: String,
		outputNames: String
	): TruthTable {
		val maxInputs = properties.getInt(PROP_TRUTH_TABLE_MAX_INPUTS)
		val maxOutputs = properties.getInt(PROP_TRUTH_TABLE_MAX_OUTPUTS)

		check(name.trim().isNotEmpty()) { Translations.getString("library.newTruthTable.error.nameEmpty") }

		val inputs = inputNames.split(',').map { it.trim() }.filter { it.isNotBlank() }
		if (inputs.isEmpty()) {
			throw IllegalArgumentException(Translations.getString("library.newTruthTable.error.inputsEmpty"))
		}
		if (inputs.size > maxInputs) {
			throw IllegalArgumentException(Translations.getString("library.newTruthTable.error.maxInputs", maxInputs))
		}
		if (inputs.distinct().size != inputs.size) {
			throw IllegalArgumentException(Translations.getString("library.newTruthTable.error.duplicateInput", maxInputs))
		}
		inputs.forEach {
			if (!checkInputName(it)) {
				throw IllegalArgumentException(Translations.getString("library.newTruthTable.error.illegalInputName", it))
			}
		}

		val outputs = outputNames.split(',').map { it.trim() }.filter { it.isNotBlank() }
		if (outputs.isEmpty()) {
			throw IllegalArgumentException(Translations.getString("library.newTruthTable.error.outputsEmpty"))
		}
		if (outputs.size > maxOutputs) {
			throw IllegalArgumentException(Translations.getString("library.newTruthTable.error.maxOutputs", maxOutputs))
		}
		if (outputs.distinct().size != outputs.size) {
			throw IllegalArgumentException(Translations.getString("library.newTruthTable.error.duplicateOutput", maxInputs))
		}
		if (inputs.any { outputs.contains(it) }) {
			throw IllegalArgumentException(Translations.getString("library.newTruthTable.error.intersection", maxOutputs))
		}
		outputs.forEach {
			if (!checkOutputName(it)) {
				throw IllegalArgumentException(Translations.getString("library.newTruthTable.error.illegalOutputName", it))
			}
		}

		return TruthTable(name, inputs, outputs)
	}

	private fun checkInputName(name: String): Boolean =
		name.first().isLetter() && name.all { it.isLetterOrDigit() }

	private fun checkOutputName(name: String): Boolean = OUTPUT_REGEX.matches(name)

	/**
	 * Generates the minimized [BooleanExpression]s of a [TruthTable] using the specified
	 * [BooleanExpressionNotation].
	 */
	fun generateExpressions(truthTable: TruthTable, notation: BooleanExpressionNotation): String {
		val builder = StringBuilder()
		with (truthTable) {
			for (col in inputColumnCount until inputColumnCount + outputColumnCount) {
				val dnf = minimizeToDNF(getMinTerms(col), getDontCares(col), inputColumnCount)
				builder.append(
					StandardBooleanExpressionWriter
						.ofNotation(notation)
						.write(this, DnfToBooleanExpression(truthTable, dnf).build(), col)
				)
				builder.append("\n")
			}
		}
		return builder.toString()
	}
}