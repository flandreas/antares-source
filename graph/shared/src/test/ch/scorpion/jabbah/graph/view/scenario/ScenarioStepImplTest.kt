package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.edit.model.text.TextProperty
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

/** Unit tests for [ScenarioStepImpl].*/
class ScenarioStepImplTest {

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
    fun shouldParseHighlightIds() {
        val step = ScenarioStepImpl()
        step.highlightIds = "1,2,3"
        assertThat(step.highlightIdsAsInt.size, `is`(3))
        assertThat(step.highlightIdsAsInt, hasItem(1))
        assertThat(step.highlightIdsAsInt, hasItem(2))
        assertThat(step.highlightIdsAsInt, hasItem(3))
        assertThat(step.highlightIdsAsInt, not(hasItem(4)))
    }

    @Test
    fun shouldClearHighlightIdsCache() {
        val step = ScenarioStepImpl()
        step.highlightIds = "1,2,3"
        step.highlightIds = "4"
        assertThat(step.highlightIdsAsInt.size, `is`(1))
        assertThat(step.highlightIdsAsInt, hasItem(4))
        assertThat(step.highlightIdsAsInt, not(hasItem(1)))
    }
}