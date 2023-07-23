package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.Issue
import ch.scorpion.jabbah.base.IssueImpl
import ch.scorpion.jabbah.base.IssueSeverity
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule

abstract class AbstractBaseInterpreter(
	protected val rootNode: Node,
	protected val memory: Memory = Memory()
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
	fun interpret(params: Any? = null): Any {
		returnValue = null
		this.params = params
		try {
			return interpret(rootNode)
		} finally {
			// Don't clear memory BEFORE interpretation in order not to break Memory.preset()
			memory.clear()
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
			BaseModule.eventBus.post(
				IssueImpl(
				severity = IssueSeverity.Error,
				name = Translations.getString("base.dsl.scriptError.msg"),
				description = e.toString(),
				origin = metaData.origin,
				context = metaData.context
			)
			)
			if (rethrow) {
				throw e
			}
			Unit
		}
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