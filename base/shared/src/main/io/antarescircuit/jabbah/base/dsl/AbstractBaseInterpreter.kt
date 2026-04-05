package io.antarescircuit.jabbah.base.dsl

import io.antarescircuit.jabbah.base.Issue
import io.antarescircuit.jabbah.base.IssueImpl
import io.antarescircuit.jabbah.base.IssueSeverity
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule

abstract class AbstractBaseInterpreter(
	protected val rootNode: Node,
	val memory: Memory = Memory()
) {
	/** Set by "return" statement to the expression to be returned and immediately quit interpretation.*/
	protected var returnValue: Any? = null

	protected var params: Any? = null
		private set

	open fun interpret(node: Node): Any =
		when (node) {
			is Compound<*> -> compound(node)
			else -> throw SyntaxError(node.location, Translations.getString("base.dsl.unknownASTNode.msg", "${node::class.simpleName}"))
		}

	/**
	 * Runs the program defined by the AST in [rootNode].
	 *
	 * @param params the optional parameters on which execution logic might depend on. The
	 * values of these parameters might be different for every call of [interpret].
	 */
	fun interpret(params: Any? = null, keepMemory: Boolean = false): Any {
		returnValue = null
		this.params = params
		try {
			return interpret(rootNode)
		} finally {
			// Don't clear memory BEFORE interpretation in order not to break Memory.preset()
			if (!keepMemory) {
				memory.clear()
			}
		}
	}

	/**
	 * Calls [interpret] and catches [DslError] by posting an [Issue] on the system's [EventBus].
	 *
	 * @param metaData used to describe [Issue]
	 * @param params the optional parameters on which execution logic might depend on. The
	 * values of these parameters might be different for every call of [interpret].
	 */
	fun interpretCatching(metaData: ScriptMetaData, params: Any? = null, rethrow: Boolean = false): Any {
		return try {
			interpret(params)
		} catch (e: DslError) {
			postError(metaData, "base.dsl.scriptError.msg", e)
			if (rethrow) {
				throw e
			}
			Unit

		} catch (e: Exception) {
			postError(metaData, "base.dsl.systemError.msg", e)
			if (rethrow) {
				throw e
			}
			Unit
		}
	}

	private fun postError(metaData: ScriptMetaData, msgKey: String, e: Throwable) {
		BaseModule.eventBus.post(
			IssueImpl(
				severity = IssueSeverity.Error,
				name = Translations.getString("base.dsl.scriptError.msg"),
				description = e.toString(),
				origin = metaData.origin,
				context = metaData.context
			)
		)
	}

	protected fun compound(node: Compound<*>): Any {
		var result: Any = 0L
		// Tuning: Faster than with streams
		for (i in 0 until node.children.size) {
			result = interpret(node.children[i])
			if (returnValue != null) {
				return returnValue!!
			}
		}
		return result
	}
}