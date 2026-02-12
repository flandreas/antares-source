package ch.scorpion.antares.ui

import ch.scorpion.antares.AntaresJvmTestRule
import ch.scorpion.antares.model.AntaresGraphTypes.Digital
import ch.scorpion.antares.model.inout.DigitalCircuitInOutImpl
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_1
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_16
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_4
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_8
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation.BINARY
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation.HEXADECIMAL
import ch.scorpion.antares.view.AntaresFrame
import ch.scorpion.antares.view.AntaresFrameController
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.TempFileLibraryTestRule
import ch.scorpion.jabbah.graph.container.ContainerPanelSwing
import ch.scorpion.jabbah.graph.container.PortViewComponent
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibraryTreeViewSwing
import ch.scorpion.jabbah.graph.library.OpenContainerLibraryElementRequest
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.ui.GraphDataViewController
import ch.scorpion.jabbah.graph.ui.GraphFrameMockBuilder
import ch.scorpion.jabbah.graph.ui.TestGraphApplication
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class ContainerUIIntegrationTest {

    private lateinit var graphDataViewController: GraphDataViewController

    private lateinit var application: TestGraphApplication<AntaresFrame>

    private lateinit var graphFrameBuilder: GraphFrameMockBuilder<AntaresFrame>

    private lateinit var containerPanel: ContainerPanelSwing

    private lateinit var libraryTreeView: LibraryTreeViewSwing

    @BeforeTest
    fun beforeTest() {
        AntaresJvmTestRule.configure()
        GraphModuleJvm.projectAkrabClientService = { mock() }

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
    fun shouldAddCircuitInOutView() {
        createAndOpenEmptyNewMetaGraph()
        addCircuitInOutView("addr")
        assertEquals("addr", getPortViewInContainerDrawing().port.name)
    }

    @Test
    fun shouldChangeNameIn() {
        createAndOpenEmptyNewMetaGraph()
        val inOutView = addCircuitInOutView("addr")
        inOutView.name = "changed"
        assertEquals("changed", getPortViewInContainerDrawing().port.name)
    }

    @Test
    fun shouldChangeBitWidth() {
        createAndOpenEmptyNewMetaGraph()
        val inOutView = addCircuitInOutView("addr", BW_4)
        inOutView.bitWidth = BW_8
        assertEquals(BW_8, (getPortViewInContainerDrawing().port as DigitalPort).bitWidth)
    }

    @Test
    fun shouldSignalRepresentation() {
        createAndOpenEmptyNewMetaGraph()
        val inOutView = addCircuitInOutView("addr", signalRepresentation = BINARY)
        inOutView.signalRepresentation = HEXADECIMAL
        assertEquals(HEXADECIMAL, (getPortViewInContainerDrawing().port as DigitalPort).signalRepresentation)
    }

    @Ignore // BUG, currently broken
    @Test
    fun shouldChangeUnconnectedStartValue() {
        createAndOpenEmptyNewMetaGraph()
        val oldStartValue = DigitalSignalFactory.of(Bit.True)
        val newStartValue = DigitalSignalFactory.of(Bit.False)
        val inOutView = addCircuitInOutView("addr", unconnectedStartValue = oldStartValue)
        (inOutView.model.getOutput<DigitalSignal>() as DigitalPort).unconnectedStartValue = newStartValue
        assertEquals(newStartValue, (getPortViewInContainerDrawing().port as DigitalPort).unconnectedStartValue)
    }

    /**
     * Regression test #986.
     */
    @Test
    fun shouldNotChangeBitWidthWhenOpeningOtherCircuit() {
        val circuit1 = createAndOpenEmptyNewMetaGraph("Circuit 1")
        addCircuitInOutView("addr1", BW_8)
        save()

        createAndOpenEmptyNewMetaGraph("Circuit 2")
        val inOutView = addCircuitInOutView("addr2", BW_8)
        inOutView.bitWidth = BW_16
        save()

        BaseModule.eventBus.post(OpenContainerLibraryElementRequest(circuit1))

        val port = getPortViewInContainerDrawing().port as DigitalPort
        assertEquals("addr1", port.name)
        assertEquals(BW_8, port.bitWidth)
    }

    private fun createAndOpenEmptyNewMetaGraph(circuitName: String = "Test"): ContainerLibraryElement {
        val project = LibraryModule.libraryHolder.library as Project
        val metaGraph = MetaGraph.create(TranslatableText(circuitName), Digital)
        val element = project.libraryService.addContainerLibraryElement(project, metaGraph, project)
        BaseModule.eventBus.post(OpenContainerLibraryElementRequest(element))
        return element
    }

    private fun addCircuitInOutView(
        name: String,
        bitWidth: BitWidth = BW_1,
        signalRepresentation: DigitalSignalRepresentation = BINARY,
        unconnectedStartValue: DigitalSignal? = null
    ): DigitalCircuitInOutView {
        val model = DigitalCircuitInOutImpl(name = name, bitWidth = bitWidth)
        val view = DigitalCircuitInOutView(model = model)
        model.signalRepresentation = signalRepresentation
        (model.getOutput<DigitalSignal>() as DigitalPort).unconnectedStartValue = unconnectedStartValue
        return GraphViewModule.graphViewAppService.add(view, application.editor.view) as DigitalCircuitInOutView
    }

    private fun getPortViewInContainerDrawing(): DigitalPortView =
        containerPanel.controller.containerDrawing!!.drawables
        .filterIsInstance<PortViewComponent<*>>()
        .first()
        .portView as DigitalPortView

    private fun save(): UUID {
        application.controller.save()
        return (application.controller.data!!.content as MetaGraph).uuid
    }
}