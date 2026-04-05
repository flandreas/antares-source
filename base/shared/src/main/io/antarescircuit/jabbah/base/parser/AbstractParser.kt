package io.antarescircuit.jabbah.base.parser

import io.antarescircuit.jabbah.base.IssueImpl
import io.antarescircuit.jabbah.base.IssueSeverity
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.DslError
import io.antarescircuit.jabbah.base.dsl.Node
import io.antarescircuit.jabbah.base.dsl.ScriptMetaData
import io.antarescircuit.jabbah.base.dsl.SyntaxError
import io.antarescircuit.jabbah.base.module.BaseModule

abstract class AbstractParser(
	protected val lexer: AbstractLexer
) : Parser {

	override fun parseCatching(metaData: ScriptMetaData): Node? {
		return try {
			parse()
		} catch (e: DslError) {
			BaseModule.eventBus.post(
				IssueImpl(
				severity = IssueSeverity.Error,
				name = Translations.getString("base.dsl.scriptError.msg"),
				description = e.message,
				origin = metaData.origin,
				context = metaData.context)
			)
			null
		}
	}

	/** Contains the current [Token] as determined by [AbstractLexer.nextToken].*/
	protected var currentToken: Token<Any>? = lexer.nextToken()
		private set

	/**
	 * Compares the current [TokenType] with the passed [TokenType] and, if they match,
	 * then "eats" the current [Token] and assigns the next [Token] to [currentToken],
	 * otherwise throws [SyntaxError].
	 */
	protected fun eat(type: TokenType) {
		if (currentToken!!.type == type) {
			currentToken = lexer.nextToken()
		} else {
			throw SyntaxError(lexer.location, Translations.getString("base.dsl.expectedToken.msg", type.id))
		}
	}
}