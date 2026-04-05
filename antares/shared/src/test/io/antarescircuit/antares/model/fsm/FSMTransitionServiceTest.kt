package io.antarescircuit.antares.model.fsm

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.module.AntaresModelModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FSMTransitionServiceTest {

    init {
        AntaresTestRule.configure()
    }

    @Test
    fun shouldParseTransitionCondition() {
        val result = AntaresModelModule.fsmTransitionService.parseTransitionCondition("A=0 & B=1")

        assertEquals(2, result.variableNames.size)
        assertTrue(result.variableNames.contains("A"))
        assertTrue(result.variableNames.contains("B"))
    }
}