package ch.scorpion.jabbah.graph.ui.documentation

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.DefaultSavable
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.Document
import ch.scorpion.jabbah.graph.ui.DocumentationPanelViewMockBuilder
import ch.scorpion.jabbah.graph.ui.GraphDataViewController
import ch.scorpion.jabbah.graph.ui.GraphDataViewMockBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocumentationPanelControllerTest {

    companion object {
        init {
            GraphViewTestRule.configure()
        }
    }

    private val graphDataViewController = GraphDataViewController()
    private val graphDataView = GraphDataViewMockBuilder(graphDataViewController)
    private val controller = DocumentationPanelController(graphDataViewController)
    private val view = DocumentationPanelViewMockBuilder(controller)

    private val metaGraph: MetaGraph get() = graphDataViewController.data!!.content as MetaGraph

    @Test
    fun shouldEnableSaveUponFirstChange() {
        setupDocumentation("Test")
        view.withText("Test!")
        controller.documentChangeBegin()

        assertTrue(EditModule.commandManager.canUndo())
    }

    @Test
    fun shouldUpdateMetaGraphOnDocumentChange() {
        setupDocumentation("Test")

        view.withText("C")
        controller.documentChangeBegin()
        view.withText("Changed text")
        controller.documentChangeEnd()

        assertEquals("Changed text", metaGraph.documentation!!.text)
    }

    @Test
    fun shouldUndo() {
        setupDocumentation("Test")
        view.withText("C")
        controller.documentChangeBegin()
        view.withText("Changed text")
        controller.documentChangeEnd()

        EditModule.commandManager.undo()

        assertEquals("Test", metaGraph.documentation!!.text)
    }

    @Test
    fun shouldRedo() {
        setupDocumentation("Test")
        view.withText("C")
        controller.documentChangeBegin()
        view.withText("Changed text")
        controller.documentChangeEnd()

        EditModule.commandManager.undo()
        EditModule.commandManager.redo()

        assertEquals("Changed text", metaGraph.documentation!!.text)
    }

    private fun setupDocumentation(text: String) {
        val metaGraph = MetaGraph()
        metaGraph.documentation = Document(text = "Test")
        graphDataViewController.data = ApplicationData(metaGraph, DefaultSavable.undefined())
    }
}