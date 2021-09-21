package ch.scorpion.antares.dsl

import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.dsl.SemanticAnalyser
import ch.scorpion.jabbah.base.dsl.TokenType.*
import ch.scorpion.jabbah.base.dsl.Variable
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.dsl.GraphDslParser

/**
 * Extends the grammar in [GraphDslParser] by the following productions.
 *
 * <pre>
 *     factor : super.factor
 *            | raisedInput
 *     raisedInput : "^" variable
 *     variable : super.variable
 *            | bitAccess
 *     bitAccess : scalarVariable "@" factor
 *     literal : number | hexLiteral
 *     hexLiteral : definedHexLiteral | undefinedHexLiteral
 *     definedHexLiteral : "0x" LONG
 *     undefinedHexLiteral : "0x?" LONG
 * </pre>
 */
class AntaresParser(
	lexer: AntaresLexer,
	semanticAnalyser: SemanticAnalyser? = BaseModule.semanticAnalyserFactory.create(null)
) : GraphDslParser(lexer, semanticAnalyser) {

	constructor(text: String): this(AntaresLexer(text))

	override fun lookAheadFromId(): Node =
		when (lexer.peekNextToken().type) {
			AT -> lookAheadFromAt()
			else -> super.lookAheadFromId()
		}

	private fun lookAheadFromAt(): Node {
		val bitAccess = bitAccess()
		return if (currentToken!!.type == ASSIGN) {
			assignment(bitAccess)
		} else {
			bitAccess
		}
	}

	override fun variable(): Variable {
		return when (lexer.peekNextToken().type) {
			AT -> bitAccess()
			else -> super.variable()
		}
	}

	private fun bitAccess(): Variable {
		lexer.location.let { location ->
			val variable = identifier()
			eat(AT)
			return BitAccess(location, variable, factor())
		}
	}

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