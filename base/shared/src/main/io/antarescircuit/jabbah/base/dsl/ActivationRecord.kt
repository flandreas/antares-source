package io.antarescircuit.jabbah.base.dsl

import io.antarescircuit.jabbah.base.parser.TextLocation

typealias ActivationRecordFactory = (name: String, parent: ActivationRecord?) -> ActivationRecord

/**
 * Constituent of [Memory] to hold variable values in a stacked way.
 */
interface ActivationRecord {

	fun clear()

	/**
	 * Checks if a variable name is defined specifically in this [ActivationRecord],
	 * i.e. not considering value definitions of its parent [ActivationRecord], if any.
	 */
	fun isLocallyDefined(name: String): Boolean

	/**
	 * Checks if a variable is defined either locally or in its parent [ActivationRecord],
	 * potentially recursively.
	 */
	fun isDefined(name: String): Boolean

	/**
	 * Predefines a variable and sets a value.
	 *
	 * Called by the execution environment (and not by script programs) to preset global context variables,
	 * therefore no [CodeLocation] is defined. Does not check for redefining variables.
	 */
	fun preset(name: String, value: Any)

	/**
	 * Defines a [Variable] in this particular [ActivationRecord] prior to setting or getting values
	 * of this [Variable]. This is used to decide whether a [Variable] is available in the current scope,
	 * or must otherwise be fetched from the parent scope.
	 */
	fun define(variable: Variable)

	fun setValue(variable: Variable, value: Any)

	fun getValue(variable: Variable): Any

	fun getValue(name: String, location: TextLocation = TextLocation.UNDEFINED): Any

	fun getOptionalValue(variable: Variable): Any?

	fun getOptionalValue(name: String, location: TextLocation = TextLocation.UNDEFINED): Any?
}