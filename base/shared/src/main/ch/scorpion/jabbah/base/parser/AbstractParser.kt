package ch.scorpion.jabbah.base.parser

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.dsl.SyntaxError

abstract class AbstractParser(
	protected val lexer: AbstractLexer
) {

	/** Contains the current [Token] as determined by [AbstractLexer.nextToken].*/
	protected var currentToken: Token<Any>? = lexer.nextToken()

	/**
	 * Parses the program this [AbstractParser] was created with and returns the corresponding AST.
	 * @throws SyntaxError if the sentence is syntactically invalid
	 */
	abstract fun parse(): Node

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