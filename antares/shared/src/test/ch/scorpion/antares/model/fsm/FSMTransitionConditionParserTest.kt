package ch.scorpion.antares.model.fsm

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.expression.assertAST
import kotlin.test.Test

class FSMTransitionConditionParserTest {

    init {
        AntaresTestRule.configure()
    }

    @Test
    fun shouldParseExpression() {
        val ast = FSMTransitionConditionParser("A=0 & B=1").parse()
        assertAST(ast, """
            and
            - ==
            -- A
            -- 0
            - ==
            -- B
            -- 1
        """.trimIndent())
    }

    @Test
    fun shouldParseLiteral() {
        val ast = FSMTransitionConditionParser("1").parse()
        assertAST(ast, """
            1
        """.trimIndent())
    }
}