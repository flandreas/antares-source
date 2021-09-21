package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Stores [Variable] values in nested scopes during execution of an [Interpreter].
 *
 * [Variable]s must be defined using [define] prior to setting or getting values,
 * which is used to decide whether a [Variable] is available in the current scope,
 * or must otherwise be fetched from the parent scope.
 *
 * [ActivationRecord] stacking:
 * - "Context" (optional): Provided from outside to gain access to context variables
 * - "Store": Stores variables that must survive multiple runs of [Interpreter.interpret]. Doesn't apply to [clear].
 * - "Global": Stores variables in the global, outermost context. Applies to [clear].
 * - "<Name1>": Inner scope 1 created by [enterScope]. Applies to [clear].
 * - "<Name2>": Inner scope 2 created by [enterScope]. Applies to [clear].
 * - etc.
 *
 * [Variable] values cannot be `null`.
 */
class Memory(private val context: ActivationRecord? = null) {

	/**
	 * Stores [Variable] values persistently, i.e. they are NOT removed by [clear].
	 * This is used for storing values that must survive multiple runs of [Interpreter.interpret].
	 */
	private val store = BaseModule.storingActivationRecordFactory.create("Store", context)

	/**
	 * The global, outermost scope always present.
	 */
	private val global = BaseModule.storingActivationRecordFactory.create("Global", store)

	private val callStack = Stack<ActivationRecord>()

	init {
		context?.let { callStack.push(it) }
		callStack.push(store)
		callStack.push(global)
	}

	/**
	 * Clears this [Memory] by removing all but the root [ActivationRecord] from the call stack
	 * and clearing the root [ActivationRecord].
	 */
	fun clear() {
		global.clear()
		callStack.clear()
		context?.let { callStack.push(it) }
		callStack.push(global)
	}

	fun enterScope(name: String) {
		callStack.push(BaseModule.storingActivationRecordFactory.create(name, callStack.optionalPeek()))
	}

	fun exitScope(node: Node) {
		if (callStack.peek() === global) {
			throw RuntimeError(node.location, "Cannot exit global scope")
		}
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

	fun define(variable: Variable, inStore: Boolean = false) {
		if (inStore) {
			// Redefinitions will naturally occur when running program multiple times on global store,
			// so just ignore them
			if (!store.isDefined(variable.token.value!!)) {
				store.define(variable)
			}
		} else {
			callStack.peek().define(variable)
		}
	}

	fun isDefined(variable: Variable): Boolean =
		callStack.peek().isDefined(variable.token.value!!)

	fun isLocallyDefined(variable: Variable): Boolean =
		callStack.peek().isLocallyDefined(variable.token.value!!)

	fun setValue(variable: Variable, value: Any) {
		callStack.peek().setValue(variable, value)
	}

	fun getValue(variable: Variable): Any =
		callStack.peek().getValue(variable)

	fun getOptionalValue(variable: Variable): Any? =
		callStack.peek().getOptionalValue(variable)
}