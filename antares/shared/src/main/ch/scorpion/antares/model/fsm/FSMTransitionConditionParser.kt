package ch.scorpion.antares.model.fsm

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.base.dsl.BaseTokenType.*
import ch.scorpion.jabbah.base.dsl.DslTokenType.*
import ch.scorpion.jabbah.base.parser.AbstractParser
import ch.scorpion.jabbah.base.parser.Token

/**
 * Parses [FSMTransition.condition]s according to the following grammar.
 *
 * <pre>
 *     condition : expr | literal.
 *	   expr: expr1 (orOp expr1)*
 *	   expr1 : expr2 (andOp" expr2)*
 *	   expr2 : term (comparisonOperator term)*
 *     term : literal
 *            | "(" expr ")"
 *            | variable
 *     comparisonOperator : "==" | "="
 *     orOp : "||" | "|" | "OR"
 *     andOp : "&&" | "&" | "AND"
 *     literal: "0" | "1"
 * </pre>
 */
class FSMTransitionConditionParser(lexer: FSMTransitionConditionLexer) : AbstractParser(lexer) {

    constructor(text: String) : this(FSMTransitionConditionLexer(text))

    companion object {
        private val COMPARISON_OPERATORS = setOf(EQUAL)
    }

    override fun parse(): Node = condition()

    private fun condition(): Node {
        return if (currentToken!!.type == LITERAL) {
            number()
        } else {
            expr()
        }
    }

    private fun expr(): Node {
        var node = expr1()
        while (currentToken!!.type == OR) {
            lexer.location.let { location ->
                val token = currentToken!!
                if (token.type == OR) {
                    eat(token.type)
                } else {
                    throw SyntaxError(location, Translations.getString("base.dsl.unexpectedToken.msg", token.type.id))
                }

                node = BinaryOperation(location, left = node, op = token, right = expr1())
            }
        }
        return node
    }

    private fun expr1(): Node {
        var node = expr2()
        while (currentToken!!.type == AND) {
            lexer.location.let { location ->
                val token = currentToken!!
                if (token.type == AND) {
                    eat(token.type)
                } else {
                    throw SyntaxError(location, Translations.getString("base.dsl.unexpectedToken.msg", token.type.id))
                }

                node = BinaryOperation(location, left = node, op = token, right = expr2())
            }
        }
        return node
    }

    private fun expr2(): Node {
        var node = factor()
        while (currentToken!!.type in COMPARISON_OPERATORS) {
            lexer.location.let { location ->
                val token = currentToken!!
                if (COMPARISON_OPERATORS.contains(token.type)) {
                    eat(token.type)
                } else {
                    throw SyntaxError(location, Translations.getString("base.dsl.unexpectedToken.msg", token.type.id))
                }
                node = BinaryOperation(location, left = node, op = token, right = factor())
            }
        }
        return node
    }

    private fun factor(): Node {
        lexer.location.let { location ->
            val token = currentToken!!
            return when (token.type) {
                LITERAL -> number()
                LPAREN -> {
                    eat(LPAREN)
                    val node = expr()
                    eat(RPAREN)
                    node
                }
                ID -> variable()
                else -> throw SyntaxError(location, Translations.getString("base.dsl.unexpectedToken.msg", token.type.id))
            }
        }
    }

    private fun number(): Literal {
        val literal = Literal(lexer.location, currentToken!!)
        eat(LITERAL)
        return literal
    }

    private fun variable(): Variable = Variable(lexer.location, identifier())

    private fun identifier(): Token<String> {
        @Suppress("UNCHECKED_CAST")
        val identifier = currentToken as Token<String>
        eat(ID)
        return identifier
    }
}