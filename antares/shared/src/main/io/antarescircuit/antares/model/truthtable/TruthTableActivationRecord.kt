package io.antarescircuit.antares.model.truthtable

import io.antarescircuit.antares.model.expression.BooleanExpressionInterpreter
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.jabbah.base.dsl.ActivationRecord
import io.antarescircuit.jabbah.base.dsl.Variable
import io.antarescircuit.jabbah.base.parser.TextLocation

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
		getValue(variable.token.value!!, variable.location)

	override fun getValue(name: String, location: TextLocation): Boolean =
		truthTable.getInputColumn(name)?.let {
			truthTable.getValue(currentRow, it).isSet
		} ?: throw IllegalArgumentException("Unknown input name $name")

	override fun getOptionalValue(variable: Variable): Any = getValue(variable)

	override fun getOptionalValue(name: String, location: TextLocation): Any = getValue(name)

	override fun clear() { throw UnsupportedOperationException("not applicable") }

	override fun preset(name: String, value: Any) { throw UnsupportedOperationException("not applicable") }

	override fun define(variable: Variable) { throw UnsupportedOperationException("not applicable") }

	override fun setValue(variable: Variable, value: Any) { throw UnsupportedOperationException("not applicable") }
}