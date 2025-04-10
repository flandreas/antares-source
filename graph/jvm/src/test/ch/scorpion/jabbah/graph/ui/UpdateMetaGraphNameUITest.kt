package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.TempFileLibraryTestRule
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibraryProperties
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.test.*

/**
 * Tests the various ways how the name of a [MetaGraph] is changed in the UI, and how the UI is updated
 * at various places to show the new name.
 */
class UpdateMetaGraphNameUITest {

    companion object {
        /** This is the default name given to new [MetaGraphs][MetaGraph] in the graph module.*/
        private const val OLD_NAME = "Graph"

        private const val NEW_NAME = "NewName"
    }

    private lateinit var application: TestGraphApplication
    private lateinit var graphFrame: GraphFrameMockBuilder

    @BeforeTest
    fun setup() {
        TempFileLibraryTestRule.configure()
        application = TestGraphApplication()
        graphFrame = GraphFrameMockBuilder(application.graphFrameController)
        application.start()
    }

    @Test
    fun updateNameInLibraryTreeNode() {
        val metaGraphElement = createAndOpenNewProjectWithMetaGraph()

        // In the UI, this would be called from popup menu on ContainerLibraryElement explorer tree node
        application.graphFrameController.graphPanelViewController.libraryPanelController.libraryTreeViewController.renameContainerLibraryElement(
            metaGraphElement,
            NEW_NAME
        )

        // Element in Library must be updated
        assertEquals(NEW_NAME, LibraryModule.libraryHolder.library.getContainerLibraryElement(metaGraphElement.uuid)!!.name.value)

        with(application.graphFrameController.graphPanelViewController.editViewController) {
            assertSame(
                view.drawingView!!.content,
                graphNavigationViewController.navigationStack.rootEntry!!.content
            )

            assertNull(graphNavigationViewController.navigationStack.rootEntry!!.subGraphVerticeView)

            // If the changed MetaGraph is currently open, its name must be updated
            assertEquals(NEW_NAME, view.drawingView!!.content.drawing.name.value)

            // Head in NavigationStackView must be updated
            assertEquals(NEW_NAME, graphNavigationViewController.navigationStack.rootEntry!!.name)
        }
    }

    @Test
    fun shouldUpdateInPropertiesPanel() {
        val metaGraphElement = createAndOpenNewProjectWithMetaGraph()

        // In the UI, this would be called from a Command issued by the ComponentPropertyPanel with GraphViewImplBeanInfo
        (application.graphFrameController.graphPanelViewController.editViewController.editor.drawing as GraphView).name = Name(NEW_NAME)

        // Head in NavigationStackView must be updated
        assertEquals(NEW_NAME, application.graphFrameController.graphPanelViewController.editViewController.graphNavigationViewController.navigationStack.rootEntry!!.name)

        // Element in Library must NOT be updated before "Save"
        assertEquals(OLD_NAME, LibraryModule.libraryHolder.library.getContainerLibraryElement(metaGraphElement.uuid)!!.name.value)

        application.controller.save()

        // Element in Library not be updated after "Save"
        assertEquals(NEW_NAME, LibraryModule.libraryHolder.library.getContainerLibraryElement(metaGraphElement.uuid)!!.name.value)
    }

    @Test
    fun shouldUpdateInNavigationStackView() {
        val metaGraphElement = createAndOpenNewProjectWithMetaGraph()

        with (application.graphFrameController.graphPanelViewController.editViewController) {
            graphNavigationViewController.navigationStackViewController
                .changeName(NEW_NAME)

            // Currently open MetaGraph must be updated
            assertEquals(NEW_NAME, (editor.drawing as GraphView).name.value)

            // Head in NavigationStackView must be updated
            assertEquals(NEW_NAME, graphNavigationViewController.navigationStack.rootEntry!!.name)

            // Element in Library must NOT be updated before "Save"
            assertEquals(OLD_NAME, LibraryModule.libraryHolder.library.getContainerLibraryElement(metaGraphElement.uuid)!!.name.value)
        }
    }

    private fun createAndOpenNewProjectWithMetaGraph(): ContainerLibraryElement {
        val service = ProjectModule.projectManagementService
        var project = service.create(LibraryProperties(name = TranslatableText("MyProject")))
        project = service.open(project.identification)
        return project.getItems().first() as ContainerLibraryElement
    }
}