package io.antarescircuit.antares.view

import io.antarescircuit.antares.AntaresJvmTestRule
import io.antarescircuit.antares.model.AntaresGraphTypes.Digital
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_16
import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_8
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rotation
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.TempFileLibraryTestRule
import io.antarescircuit.jabbah.graph.container.ContainerPanelSwing
import io.antarescircuit.jabbah.graph.container.PortViewComponent
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.LibraryTreeViewSwing
import io.antarescircuit.jabbah.graph.library.OpenContainerLibraryElementRequest
import io.antarescircuit.jabbah.graph.module.GraphModuleJvm
import io.antarescircuit.jabbah.graph.project.Project
import io.antarescircuit.jabbah.graph.ui.GraphDataViewController
import io.antarescircuit.jabbah.graph.ui.GraphFrameMockBuilder
import io.antarescircuit.jabbah.graph.ui.TestGraphApplication
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AutoAdjustBitWidthTest {

    private lateinit var graphDataViewController: GraphDataViewController

    private lateinit var application: TestGraphApplication<AntaresFrame>

    private lateinit var graphFrameBuilder: GraphFrameMockBuilder<AntaresFrame>

    private lateinit var containerPanel: ContainerPanelSwing

    private lateinit var libraryTreeView: LibraryTreeViewSwing

    private val graphView: GraphView get() = (application.controller.data!!.content as MetaGraph).graph.graphView

    @BeforeTest
    fun beforeTest() {
        AntaresJvmTestRule.configure()
        GraphModuleJvm.projectAkrabClientService = { mock() }

        BaseModule.eventBus.clear()

        TempFileLibraryTestRule.configure()

        graphDataViewController = GraphDataViewController()

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

    @Test
    fun shouldNotAdjustBitWidthToSubCircuitPortUponOpen() {
        // Create subcircuit with BitWidth 8 input
        val subCircuitElem = createAndOpenEmptyNewMetaGraph("SubCircuit")
        GraphViewModule.graphViewAppService.add(
            DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "addr", bitWidth = BW_8)),
            application.editor.view
        ) as DigitalCircuitInOutView
        val subCircuitUUID = save()

        // Create circuit and add SubCircuitRefView
        val mainCircuitElem = createAndOpenEmptyNewMetaGraph("MainCircuit")
        useContainerLibraryElement(subCircuitUUID)

        // Create circuit input and connect to subcircuit
        val mainInputView = DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(name = "addr", bitWidth = BW_8))
        GraphViewModule.graphViewAppService.add(mainInputView, application.editor.view) as DigitalCircuitInOutView
        GraphViewModule.graphViewConnectService.addConnection<DigitalSignal>(
            graphView,
            graphView.drawables.filterIsInstance<DigitalCircuitInOutView>().first(),
            graphView.drawables.filterIsInstance<SubGraphVerticeView<*>>().first()
        )
        save()

        // Change BitWidth in subcircuit to 16
        BaseModule.eventBus.post(OpenContainerLibraryElementRequest(subCircuitElem))
        val inOutView = graphView.drawables
            .filterIsInstance<DigitalCircuitInOutView>()
            .first()

        inOutView.bitWidth = BW_16
        assertEquals(BW_16, (getPortViewInContainerDrawing().port as DigitalPort).bitWidth)
        save()

        // Open main circuit
        BaseModule.eventBus.post(OpenContainerLibraryElementRequest(mainCircuitElem))

        // Ensure that BitWidth of CircuitInOut has NOT been adjusted
        assertEquals(
            BW_16,
            (graphView.drawables
                .filterIsInstance<SubGraphVerticeView<*>>()
                .first()
                .model
                .getInput<DigitalSignal>() as DigitalPort
            ).bitWidth
        )
        // This failed with bug #986
        assertEquals(
            BW_8,
            graphView.drawables
                .filterIsInstance<DigitalCircuitInOutView>()
                .first()
                .bitWidth
        )
    }

    private fun createAndOpenEmptyNewMetaGraph(circuitName: String): ContainerLibraryElement {
        val project = LibraryModule.libraryHolder.library as Project
        val metaGraph = MetaGraph.create(TranslatableText(circuitName), Digital)
        val element = project.libraryService.addContainerLibraryElement(project, metaGraph, project)
        BaseModule.eventBus.post(OpenContainerLibraryElementRequest(element))
        return element
    }

    private fun save(): UUID {
        application.controller.save()
        return (application.controller.data!!.content as MetaGraph).uuid
    }

    private fun useContainerLibraryElement(componentUuid: UUID): SubGraphVerticeView<*> {
        val service = GraphViewModule.graphViewAppService
        val project = LibraryModule.libraryHolder.library as Project
        val element = project.getContainerLibraryElement(componentUuid)!!
        val editor = application.editor

        return service.addGraphElementViewFromLibrary(element, Point2D.ZERO, Rotation.R0, editor) as SubGraphVerticeView<*>
    }

    private fun getPortViewInContainerDrawing(): DigitalPortView =
        containerPanel.controller.containerDrawing!!.drawables
            .filterIsInstance<PortViewComponent<*>>()
            .first()
            .portView as DigitalPortView
}