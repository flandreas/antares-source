package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.model.expression.BooleanExpressionInterpreter
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.jabbah.base.dsl.ActivationRecord
import ch.scorpion.jabbah.base.dsl.Variable

/**
 * An [ActivationRecord] implementation that allows a [BooleanExpressionInterpreter]
 * to access input values of a current row in a [TruthTable] as variables.
 *
 * Converts the [Bit]s in [TruthTable] to [Boolean] as this is the type [BooleanExpressionInterpreter]
 * operates on.
 */
class TruthTableActivationRecord(
	private val truthTable: TruthTable
) : ActivationRecord {

	var currentRow: Int = 0

	/** ---- [ActivationRecord] */

	override fun isLocallyDefined(name: String): Boolean = isDefined(name)

	override fun isDefined(name: String): Boolean = truthTable.hasInputName(name)

	override fun getValue(variable: Variable): Boolean =
		truthTable.getInputColumn(variable.token.value!!)?.let {
			truthTable.getValue(currentRow, it).isSet
		} ?: throw IllegalArgumentException("Unknown input name ${variable.token.value}")

	override fun getOptionalValue(variable: Variable): Any? = getValue(variable)

	override fun clear() { throw UnsupportedOperationException("not applicable") }

	override fun preset(name: String, value: Any) { throw UnsupportedOperationException("not applicable") }

	override fun define(variable: Variable) { throw UnsupportedOperationException("not applicable") }

	override fun setValue(variable: Variable, value: Any) { throw UnsupportedOperationException("not applicable") }
}