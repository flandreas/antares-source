package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.base.dsl.TokenType.*
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Extends the grammar in [Parser] by the following productions.
 *
 * <pre>
 *     statement : super.statement
 *               | initStatement
 *     initStatement : "init" block
 *     factor : super.factor
 *            | property
 *     property : propertyPortName | propertyPortId
 *     propertyPortName : "#" number "." identifier
 *     propertyPortId : "#" number "." number
 * </pre>
 */
open class GraphDslParser(
	lexer: Lexer,
	semanticAnalyser: SemanticAnalyser? = BaseModule.semanticAnalyserFactory.create(null)
) : Parser(lexer, semanticAnalyser) {

	constructor(program: String): this(Lexer(program))

	override fun statement(): Node =
		when (currentToken!!.type) {
			INIT -> init()
			else -> super.statement()
		}

	override fun isFactor(): Boolean =
		when (currentToken!!.type) {
			HASH -> true
			else -> super.isFactor()
		}

	override fun factor(): Node =
		when (currentToken!!.type) {
			HASH -> property()
			else -> super.factor()
		}

	private fun init(): Node {
		eat(INIT)
		return InitStatement(lexer.location, block())
	}

	private fun property(): Node {
		lexer.location.let { location ->
			eat(HASH)
			val id = literal()
			eat(DOT)
			return when (currentToken!!.type) {
				ID -> propertyPortName(id)
				LITERAL -> propertyPortId(id)
				else -> throw SyntaxError(location, "Expected port ID or port name")
			}
		}
	}

	private fun propertyPortName(elemId : Literal): Node = PropertyPortName(lexer.location, elemId, variable())

	private fun propertyPortId(elemId: Literal): Node = PropertyPortId(lexer.location, elemId, literal())
}