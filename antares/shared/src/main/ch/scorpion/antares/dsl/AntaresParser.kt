package ch.scorpion.antares.dsl

import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.dsl.SemanticAnalyser
import ch.scorpion.jabbah.base.dsl.TokenType.CARET
import ch.scorpion.jabbah.graph.dsl.GraphDslParser

/**
 * Extends the grammar in [GraphDslParser] by the following productions.
 *
 * <pre>
 *     factor : super.factor
 *            | raisedInput
 *     raisedInput : "^" variable
 *     literal : number | hexLiteral
 *     hexLiteral : definedHexLiteral | undefinedHexLiteral
 *     definedHexLiteral : "0x" LONG
 *     undefinedHexLiteral : "0x?" LONG
 * </pre>
 */
class AntaresParser(
	lexer: AntaresLexer,
	semanticAnalyser: SemanticAnalyser? = SemanticAnalyser()
) : GraphDslParser(lexer, semanticAnalyser) {

	constructor(text: String): this(AntaresLexer(text))

	override fun factor(): Node {
		lexer.location.let { location ->
			val token = currentToken!!
			return when (token.type) {
				CARET -> {
					eat(CARET)
					return RaisedInput(location, variable())
				}
				else -> super.factor()
			}
		}
	}

	override fun isFactor(): Boolean {
		return when(currentToken!!.type) {
			CARET -> true
			else -> super.isFactor()
		}
	}
}