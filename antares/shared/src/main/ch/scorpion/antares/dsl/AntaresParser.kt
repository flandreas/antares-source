package ch.scorpion.antares.dsl

import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.dsl.Parser
import ch.scorpion.jabbah.base.dsl.SemanticAnalyser
import ch.scorpion.jabbah.base.dsl.TokenType

/**
 * Extends the grammar in [Parser] by the following productions.
 *
 * <pre>
 *     factor : super.factor
 *            | raisedInput
 *     raisedInput : "^" variable
 *     number : LONG | hexLiteral
 *     hexLiteral : "0x" LONG
 * </pre>
 */
class AntaresParser(
	lexer: AntaresLexer,
	semanticAnalyser: SemanticAnalyser? = SemanticAnalyser()
) : Parser(lexer, semanticAnalyser) {

	constructor(text: String): this(AntaresLexer(text))

	override fun factor(): Node {
		lexer.location.let { location ->
			val token = currentToken!!
			return when (token.type) {
				TokenType.CARET -> {
					eat(TokenType.CARET)
					return RaisedInput(location, variable())
				}
				else -> super.factor()
			}
		}
	}
}