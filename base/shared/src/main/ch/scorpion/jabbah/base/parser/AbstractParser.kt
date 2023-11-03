package ch.scorpion.jabbah.base.parser

import ch.scorpion.jabbah.base.IssueImpl
import ch.scorpion.jabbah.base.IssueSeverity
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.dsl.ScriptMetaData
import ch.scorpion.jabbah.base.dsl.SyntaxError
import ch.scorpion.jabbah.base.module.BaseModule

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