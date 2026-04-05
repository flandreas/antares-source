package io.antarescircuit.antares.model.fsm

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.jabbah.base.dsl.Memory
import kotlin.test.Test
import kotlin.test.assertEquals

class FSMTransitionConditionInterpreterTest {

    init {
        AntaresTestRule.configure()
    }

    @Test
    fun shouldInterpretExpression() {
        assertExpressionTrue("A=0 & B=1", mapOf("A" to 0L, "B" to 1L))
        assertExpressionFalse("A=0 & B=1", mapOf("A" to 0L, "B" to 0L))
        assertExpressionTrue("A=1", mapOf("A" to 1L))
    }

    @Test
    fun shouldInterpretLiteral() {
        assertExpressionTrue("1", mapOf())
        assertExpressionFalse("0", mapOf())
    }

    private fun assertExpressionTrue(expression: String, values: Map<String, Long>) {
        testExpression(expression, values, 1L)
    }

    private fun assertExpressionFalse(expression: String, values: Map<String, Long>) {
        testExpression(expression, values, 0L)
    }

    private fun testExpression(expression: String, values: Map<String, Long>, result: Long) {
        val memory = Memory()
        val parser = FSMTransitionConditionParser(expression)
        val interpreter = FSMTransitionConditionInterpreter(parser.parse(), memory)
        values.forEach { memory.preset(it.key, it.value) }

        assertEquals(result, interpreter.interpret())
    }
}