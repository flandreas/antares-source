package io.antarescircuit.antares.model.fsm

import io.antarescircuit.antares.model.expression.AbstractLexerTest
import io.antarescircuit.jabbah.base.dsl.DslTokenType
import kotlin.test.Test

class FSMTransitionConditionLexerTest : AbstractLexerTest() {

    @Test
    fun shouldScanStandardExpression() {
        assertExpression("A==0 and B==1")
    }

    @Test
    fun shouldScanExpressionWithAssignToken() {
        assertExpression("A=0 and B=1")
    }

    @Test
    fun shouldScanExpressionWithProgrammingAndToken() {
        assertExpression("A==0 && B==1")
    }

    @Test
    fun shouldScanExpressionWithAmpersandAndToken() {
        assertExpression("A==0 & B==1")
    }

    @Test
    fun shouldScanSimplestExpression() {
        assertExpression("A=0 & B=1")
    }

    private fun assertExpression(expr: String) {
        val lexer = FSMTransitionConditionLexer(expr)
        assertId("A", lexer)
        assertToken(DslTokenType.EQUAL, lexer)
        assertLong(0L, lexer)
        assertToken(DslTokenType.AND, lexer)
        assertId("B", lexer)
        assertToken(DslTokenType.EQUAL, lexer)
        assertLong(1L, lexer)
    }

    @Test
    fun shouldScanSingleExpression() {
        val lexer = FSMTransitionConditionLexer("I=0")
        assertId("I", lexer)
        assertToken(DslTokenType.EQUAL, lexer)
        assertLong(0L, lexer)
    }

    @Test
    fun shouldScanLiteral() {
        val lexer = FSMTransitionConditionLexer("1")
        assertLong(1L, lexer)
    }
}