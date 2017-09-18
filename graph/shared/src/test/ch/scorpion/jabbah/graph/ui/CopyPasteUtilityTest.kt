package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.edit.DrawingViewMockBuilder
import ch.scorpion.jabbah.graph.model.TestVertice
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.TestGraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.io.IOModule
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

/** Unit tests for [CopyPasteUtility]. */
class CopyPasteUtilityTest {

    companion object {
        @ClassRule @JvmField
        val rule = GraphViewTestRule()
    }

    @Before
    fun setup() {
        TestTranslationsBuilder().withAnyKey()
        IOModule.typeMap.register("testVertice", TestVertice::class)
        IOModule.typeMap.register("testVerticeView", TestVerticeView::class)
    }

    @Test
    fun shouldCopyPaste() {
        val testGraphView = copyPaste()
        assertThat(testGraphView.graphView.drawablesCount, `is`(4))
    }

    @Test
    fun copyShouldHaveDifferentModel() {
        val testGraphView = copyPaste()
        assertThat(testGraphView.vv2.model, `is`(not(sameInstance(getCopiedVerticeView(testGraphView).model))))
    }

    /** Creates a [TestGraphView] and performs a copy/paste for its [TestVerticeView] vv2.*/
    fun copyPaste(): TestGraphView {
        val testGraphView = TestGraphView()
        CopyPasteUtility.copy(testGraphView.graphView, listOf(testGraphView.vv2))
        CopyPasteUtility.paste(DrawingViewMockBuilder().withDrawing(testGraphView.graphView).build())
        return testGraphView
    }

    fun getCopiedVerticeView(testGraphView: TestGraphView): VerticeView<*> {
        return testGraphView.graphView.get(testGraphView.graphView.drawablesCount - 1) as VerticeView<*>
    }
}