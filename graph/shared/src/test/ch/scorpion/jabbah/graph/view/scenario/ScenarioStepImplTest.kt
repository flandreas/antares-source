package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import kotlin.test.*

/** Unit tests for [ScenarioStepImpl].*/
class ScenarioStepImplTest {

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
    fun shouldParseHighlightIds() {
        val step = ScenarioStepImpl()
        step.highlightIds = "1,2,3"
        assertEquals(3, step.highlightIdsAsInt.size)
        assertTrue(step.highlightIdsAsInt.contains(1))
	    assertTrue(step.highlightIdsAsInt.contains(2))
	    assertTrue(step.highlightIdsAsInt.contains(3))
	    assertFalse(step.highlightIdsAsInt.contains(4))
    }

    @Test
    fun shouldClearHighlightIdsCache() {
        val step = ScenarioStepImpl()
        step.highlightIds = "1,2,3"
        step.highlightIds = "4"
        assertEquals(1, step.highlightIdsAsInt.size)
        assertTrue(step.highlightIdsAsInt.contains(4))
	    assertFalse(step.highlightIdsAsInt.contains(1))
    }
}