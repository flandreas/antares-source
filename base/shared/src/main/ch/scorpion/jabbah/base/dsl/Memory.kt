package ch.scorpion.jabbah.base.dsl

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
class Memory(rootActivationRecord: ActivationRecord = StoringActivationRecord("global", null)) {

	private val callStack = Stack<ActivationRecord>()

	init {
		callStack.push(rootActivationRecord)
	}

	fun enterScope(name: String) {
		callStack.push(StoringActivationRecord(name, callStack.optionalPeek()))
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
}