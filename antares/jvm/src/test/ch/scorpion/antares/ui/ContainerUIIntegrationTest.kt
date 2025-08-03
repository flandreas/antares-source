package ch.scorpion.antares.ui

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.view.AntaresFrame
import ch.scorpion.antares.view.AntaresFrameController
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.graph.TempFileLibraryTestRule
import ch.scorpion.jabbah.graph.container.ContainerPanelSwing
import ch.scorpion.jabbah.graph.library.LibraryTreeViewSwing
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.ui.GraphDataViewController
import ch.scorpion.jabbah.graph.ui.GraphFrameMockBuilder
import ch.scorpion.jabbah.graph.ui.TestGraphApplication
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test

class ContainerUIIntegrationTest {

    private lateinit var graphDataViewController: GraphDataViewController

    private lateinit var application: TestGraphApplication<AntaresFrame>

    private lateinit var graphFrameBuilder: GraphFrameMockBuilder<AntaresFrame>

    private lateinit var containerPanel: ContainerPanelSwing

    private lateinit var libraryTreeView: LibraryTreeViewSwing

    @BeforeTest
    fun beforeTest() {
        BaseModuleJvm.require()
        AntaresTestRule.configure()
        TempFileLibraryTestRule.configure()
        GraphModuleJvm.projectAkrabClientService = { mock() }

        graphDataViewController = GraphDataViewController()
    }

    @Test
    fun test() {
        TempFileLibraryTestRule.createAndEstablishCurrentLibrary("Lib1")
        application = TestGraphApplication(
            graphDataViewController,
            AntaresFrameController(graphDataViewController)
        )
        graphFrameBuilder = GraphFrameMockBuilder(application.graphFrameController)

        // Use the real ContainerPanel Swing UI because it contains the ContainerTreeView
        // and displays the ContainerDrawing whose content is to be tested in various scenarios
        containerPanel = ContainerPanelSwing(application.graphFrameController.containerPanelController, application)
        graphFrameBuilder.withContainerPanelView(containerPanel)

        // Use the real LibraryTreeView Swing UI because it contains content to be tested in various scenarios
        libraryTreeView = LibraryTreeViewSwing(
            application.graphFrameController.graphPanelViewController.libraryPanelController.libraryTreeViewController,
            application)
        graphFrameBuilder.graphPanelViewBuilder.libraryPanelBuilder.withLibraryTreeView(libraryTreeView)

        application.start()
    }
}