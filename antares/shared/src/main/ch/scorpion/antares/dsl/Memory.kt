package ch.scorpion.antares.dsl

import ch.scorpion.jabbah.base.collection.Stack

/**
 * Stores [Variable] values in nested scopes during execution of an [Interpreter].
 *
 * [Variable]s must be defined using [define] prior to setting or getting values,
 * which is used to decide whether a [Variable] is available in the current scope,
 * or must otherwise be fetched from the parent scope.
 *
 * [Variable] values cannot be `null`.
 */
class Memory {

	private val callStack = Stack<ActivationRecord>()

	init {
		enterScope("global")
	}

	fun enterScope(name: String) {
		callStack.push(ActivationRecord(name, callStack.optionalPeek()))
	}

	fun exitScope() {
		callStack.pop()
	}

	/**
	 * Predefines a variable and sets a value.
	 *
	 * Called by the execution environment (and not by script programs) to preset global context variables,
	 * therefore no [CodeLocation] is defined. Does not check for redefining variables.
	 */
	fun preset(name: String, value: Any) {
		callStack.peek().preset(name, value)
	}

	fun define(variable: Variable) {
		ensureStackNotEmpty(variable.location)
		callStack.peek().define(variable)
	}

	fun isDefined(variable: Variable): Boolean {
		ensureStackNotEmpty(variable.location)
		return callStack.peek().isDefined(variable.token.value!!)
	}

	fun isLocallyDefined(variable: Variable): Boolean {
		ensureStackNotEmpty(variable.location)
		return callStack.peek().isLocallyDefined(variable.token.value!!)
	}

	fun setValue(variable: Variable, value: Any) {
		ensureStackNotEmpty(variable.location)
		callStack.peek().setValue(variable, value)
	}

	fun getValue(variable: Variable): Any {
		ensureStackNotEmpty(variable.location)
		return callStack.peek().getValue(variable)
	}

	private fun ensureStackNotEmpty(location: CodeLocation) {
		if (callStack.empty) {
			throw RuntimeError(location, "No activation record")
		}
	}

	/**
	 * Holds [Variable] definitions and values of a particular scope.
	 */
	private class ActivationRecord(val name: String, val parent: ActivationRecord?) {

		/** Defined variables have at least a key with value `null`. */
		private val values = mutableMapOf<String, Any?>()

		fun isLocallyDefined(name: String) = values.containsKey(name)

		fun isDefined(name: String): Boolean =
			isLocallyDefined(name) || parent?.isDefined(name) == true

		fun preset(name: String, value: Any) {
			values[name] = value
		}

		fun define(variable: Variable) {
			if (isLocallyDefined(variable.token.value!!)) {
				throw RuntimeError(variable.location, "Variable '${variable.token.value}' already defined in '$name'")
			}
			values[variable.token.value] = null
		}

		fun setValue(variable: Variable, value: Any) {
			when {
				isLocallyDefined(variable.token.value!!) ->
					values[variable.token.value] = value
				parent != null ->
					parent.setValue(variable, value)
				else ->
					throw RuntimeError(variable.location, "Variable '${variable.token.value}' not defined in '$name'")
			}
		}

		fun getValue(variable: Variable): Any =
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
	}
}