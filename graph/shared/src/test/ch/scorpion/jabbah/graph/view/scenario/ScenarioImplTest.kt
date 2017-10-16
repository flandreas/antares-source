package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

/** Unit tests for [ScenarioImpl]. */
class ScenarioImplTest {

    companion object {
        @ClassRule
        @JvmField
        val rule = GraphViewTestRule()
    }

    @Before
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

        assertThat(scenario.indexOf(step3), `is`(0))
        assertThat(scenario.indexOf(step1), `is`(1))
        assertThat(scenario.indexOf(step2), `is`(2))
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

        assertThat(scenario.indexOf(step2), `is`(0))
        assertThat(scenario.indexOf(step3), `is`(1))
        assertThat(scenario.indexOf(step1), `is`(2))
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

        assertThat(scenario.indexOf(step2), `is`(0))
        assertThat(scenario.indexOf(step1), `is`(1))
        assertThat(scenario.indexOf(step3), `is`(2))
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

        assertThat(scenario.indexOf(step1), `is`(0))
        assertThat(scenario.indexOf(step3), `is`(1))
        assertThat(scenario.indexOf(step2), `is`(2))
    }
}