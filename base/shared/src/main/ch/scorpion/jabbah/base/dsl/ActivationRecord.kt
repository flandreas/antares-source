package ch.scorpion.jabbah.base.dsl

fun interface ActivationRecordFactory {
	fun create(name: String, parent: ActivationRecord?) : ActivationRecord
}

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

	fun getOptionalValue(variable: Variable): Any?
}

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
			throw RuntimeError(variable.location, "Variable '${variable.token.value}' already defined in '$name'")
		}
		values[variable.token.value] = null
	}

	override fun setValue(variable: Variable, value: Any) {
		when {
			isLocallyDefined(variable.token.value!!) -> store(variable, value)
			parent != null -> parent.setValue(variable, value)
			else ->
				throw RuntimeError(variable.location, "Variable '${variable.token.value}' not defined in '$name'")
		}
	}

	protected open fun store(variable: Variable, value: Any) {
		values[variable.token.value!!] = value
	}

	override fun getValue(variable: Variable): Any =
		when {
			isLocallyDefined(variable.token.value!!) -> {
				values[variable.token.value]
					?: throw RuntimeError(variable.location, "No value for variable '${variable.token.value}' available")
			}
			parent != null ->
				parent.getValue(variable)
			else ->
				throw RuntimeError(variable.location, "Variable '${variable.token.value}' not defined in '$name'")
		}

	override fun getOptionalValue(variable: Variable): Any? {
		return if (isLocallyDefined(variable.token.value!!)) {
			values[variable.token.value]
		} else {
			parent?.getOptionalValue(variable)
		}
	}
}