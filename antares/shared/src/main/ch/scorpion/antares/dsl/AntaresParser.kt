package ch.scorpion.antares.dsl

import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.dsl.SemanticAnalyser
import ch.scorpion.jabbah.base.dsl.DslTokenType.*
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
 *            | lengthCast
 *     bitAccess : identifier "@" factor
 *     lengthCast : identifier "$" factor
 *     literal : super.literal
 *             | hexLiteral
 *             | binaryLiteral
 *     hexLiteral : definedHexLiteral | undefinedHexLiteral
 *     binaryLiteral : "0b" binaryNumber
 *     definedHexLiteral : "0x" hexNumber
 *     undefinedHexLiteral : "0x?" LONG
 * </pre>
 */
class AntaresParser(
	lexer: AntaresLexer,
	semanticAnalyser: SemanticAnalyser? = BaseModule.semanticAnalyserFactory(null)
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
			DOLLAR -> lengthCast()
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

	private fun lengthCast(): Variable {
		lexer.location.let { location ->
			val variable = identifier()
			eat(DOLLAR)
			return LengthCast(location, variable, factor())
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