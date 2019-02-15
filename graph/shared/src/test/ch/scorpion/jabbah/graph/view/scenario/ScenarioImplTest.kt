package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [ScenarioImpl]. */
class ScenarioImplTest {

    companion object {
	    init {
		    GraphViewTestRule.configure()
	    }
    }

    @BeforeTest
    fun setup() {
        TestTranslationsBuilder().withAnyKey()
    }

    @Test
    fun shouldMoveStepToBegin() {
        val scenario = ScenarioImpl()
        val step1 = ScenarioStepImpl()
        val step2 = ScenarioStepImpl()
        val step3 = ScenarioStepImpl()
        scenario.addStep(step1)
        scenario.addStep(step2)
        scenario.addStep(step3)

        scenario.moveStep(step3, 0)

        assertEquals(0, scenario.indexOf(step3))
        assertEquals(1, scenario.indexOf(step1))
        assertEquals(2, scenario.indexOf(step2))
    }

    @Test
    fun shouldMoveStepToEnd() {
        val scenario = ScenarioImpl()
        val step1 = ScenarioStepImpl()
        val step2 = ScenarioStepImpl()
        val step3 = ScenarioStepImpl()
        scenario.addStep(step1)
        scenario.addStep(step2)
        scenario.addStep(step3)

        scenario.moveStep(step1, 2)

        assertEquals(0, scenario.indexOf(step2))
        assertEquals(1, scenario.indexOf(step3))
        assertEquals(2, scenario.indexOf(step1))
    }

    @Test
    fun shouldMoveStepUp() {
        val scenario = ScenarioImpl()
        val step1 = ScenarioStepImpl()
        val step2 = ScenarioStepImpl()
        val step3 = ScenarioStepImpl()
        scenario.addStep(step1)
        scenario.addStep(step2)
        scenario.addStep(step3)

        scenario.moveStep(step1, 1)

        assertEquals(0, scenario.indexOf(step2))
        assertEquals(1, scenario.indexOf(step1))
        assertEquals(2, scenario.indexOf(step3))
    }

    @Test
    fun shouldMoveStepDown() {
        val scenario = ScenarioImpl()
        val step1 = ScenarioStepImpl()
        val step2 = ScenarioStepImpl()
        val step3 = ScenarioStepImpl()
        scenario.addStep(step1)
        scenario.addStep(step2)
        scenario.addStep(step3)

        scenario.moveStep(step3, 1)

        assertEquals(0, scenario.indexOf(step1))
        assertEquals(1, scenario.indexOf(step3))
        assertEquals(2, scenario.indexOf(step2))
    }
}