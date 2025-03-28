package ch.scorpion.antares.model.fsm

import ch.scorpion.jabbah.base.dsl.BaseLexer
import ch.scorpion.jabbah.base.dsl.DslTokenType.*
import ch.scorpion.jabbah.base.parser.Token

class FSMTransitionConditionLexer(text: String) : BaseLexer(text) {

    companion object {
        private val LPAREN_TOKEN = Token<Unit>(LPAREN)
        private val RPAREN_TOKEN = Token<Unit>(RPAREN)
        private val EQUAL_TOKEN = Token<Unit>(EQUAL)
        private val AND_TOKEN = Token<String>(AND)
        private val OR_TOKEN = Token<String>(OR)

        private val RESERVED_KEYWORDS = mapOf(
            "AND" to AND_TOKEN,
            "OR" to OR_TOKEN,
            "and" to AND_TOKEN,
            "or" to OR_TOKEN
        )
    }

    override fun getReservedKeyword(name: String): Token<String>? =
        RESERVED_KEYWORDS[name]

    override fun nextTokenImpl(state: State): Token<Any> {
        if (isEqual(state)) {
            return equal(state)
        }
        if (isProgrammingAnd(state)) {
            return programmingAnd(state)
        }
        if (isProgrammingOr(state)) {
            return programmingOr(state)
        }

        when (state.currentChar!!) {
            LPAREN.id.first() -> return advanceWith(state, LPAREN_TOKEN)
            RPAREN.id.first() -> return advanceWith(state, RPAREN_TOKEN)
            AMPERSAND.id.first() -> return advanceWith(state, AND_TOKEN)
            VERTICAL_BAR.id.first() -> return advanceWith(state, OR_TOKEN)
            ASSIGN.id.first() -> return advanceWith(state, EQUAL_TOKEN)
        }

        return super.nextTokenImpl(state)
    }

    private fun isEqual(state: State): Boolean = state.currentChar == '=' && peek(state) == '='

    private fun isProgrammingAnd(state: State): Boolean = state.currentChar == '&' && peek(state) == '&'
    private fun isProgrammingOr(state: State): Boolean = state.currentChar == '|' && peek(state) == '|'

    private fun equal(state: State): Token<Unit> {
        advance(state)
        advance(state)
        return EQUAL_TOKEN
    }

    private fun programmingAnd(state: State): Token<String> {
        advance(state)
        advance(state)
        return AND_TOKEN
    }

    private fun programmingOr(state: State): Token<String> {
        advance(state)
        advance(state)
        return OR_TOKEN
    }
}