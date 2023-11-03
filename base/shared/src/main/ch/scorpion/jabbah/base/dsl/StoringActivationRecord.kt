package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.parser.TextLocation

/**
 * Holds [Variable] definitions and values of a particular scope.
 */
open class StoringActivationRecord(val name: String, val parent: ActivationRecord?) : ActivationRecord {

	/** Defined variables have at least a key with value `null`. */
	private val values = mutableMapOf<String, Any?>()

	override fun clear() {
		values.clear()
	}

	override fun isLocallyDefined(name: String) = values.containsKey(name)

	override fun isDefined(name: String): Boolean =
		isLocallyDefined(name) || parent?.isDefined(name) == true

	override fun preset(name: String, value: Any) {
		values[name] = value
	}

	override fun define(variable: Variable) {
		if (isLocallyDefined(variable.token.value!!)) {
			throw RuntimeError(variable.location, Translations.getString("base.dsl.variableAlreadyDeclared.msg", variable.token.value))
		}
		values[variable.token.value] = null
	}

	override fun setValue(variable: Variable, value: Any) {
		when {
			isLocallyDefined(variable.token.value!!) -> store(variable, value)
			parent != null -> parent.setValue(variable, value)
			else ->
				throw RuntimeError(variable.location, Translations.getString("base.dsl.variableNotDefined.msg", variable.token.value))
		}
	}

	protected open fun store(variable: Variable, value: Any) {
		values[variable.token.value!!] = value
	}

	override fun getValue(variable: Variable): Any =
		getValue(variable.token.value!!, variable.location)

	override fun getValue(name: String, location: TextLocation): Any =
		when {
			isLocallyDefined(name) -> {
				values[name]
					?: throw RuntimeError(TextLocation.UNDEFINED, Translations.getString("base.dsl.noValueForVariable.msg", name))
			}
			parent != null ->
				parent.getValue(name)
			else ->
				throw RuntimeError(TextLocation.UNDEFINED, Translations.getString("base.dsl.variableNotDefined.msg", name))
		}

	override fun getOptionalValue(variable: Variable): Any? =
		getOptionalValue(variable.token.value!!, variable.location)

	override fun getOptionalValue(name: String, location: TextLocation): Any? =
		if (isLocallyDefined(name)) {
			values[name]
		} else {
			parent?.getOptionalValue(name, location)
		}
}