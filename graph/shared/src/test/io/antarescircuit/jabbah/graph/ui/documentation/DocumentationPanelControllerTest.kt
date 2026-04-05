package io.antarescircuit.jabbah.graph.ui.documentation

import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.app.DefaultSavable
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.model.Document
import io.antarescircuit.jabbah.graph.ui.DocumentationPanelViewMockBuilder
import io.antarescircuit.jabbah.graph.ui.GraphDataViewController
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocumentationPanelControllerTest {

    private val graphDataViewController = GraphDataViewController()
    private lateinit var controller: DocumentationPanelController
    private lateinit var view: DocumentationPanelViewMockBuilder

    private val metaGraph: MetaGraph get() = graphDataViewController.data!!.content as MetaGraph

    @BeforeTest
    fun setup() {
        GraphViewTestRule.configure()
        controller = DocumentationPanelController(graphDataViewController)
        view = DocumentationPanelViewMockBuilder(controller)
    }

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