package ch.scorpion.antares

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.AbstractGraphViewEditingTest
import ch.scorpion.antares.view.gate.LogicGateView
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.app.GraphViewAppServiceImpl
import ch.scorpion.jabbah.graph.view.connect.ConnectDestinationCommand
import ch.scorpion.jabbah.graph.view.connect.ConnectOriginCommand
import ch.scorpion.jabbah.graph.view.connect.JoinEdgeViewEndpointsCommand
import ch.scorpion.jabbah.graph.view.graph.GraphViewCopyPasteService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType.DESTINATION
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType.ORIGIN
import junit.framework.TestCase.assertEquals
import kotlin.test.BeforeTest
import kotlin.test.Test

class Bug984 : AbstractGraphViewEditingTest() {

    @BeforeTest
    fun setUp() {
        AntaresTestRule.configure()
        EditModule.drawingAppService = GraphViewAppServiceImpl(GraphViewCopyPasteService())
    }

    override fun setupCircuit() {
        editor.selectionTool.rubberBandHandler.delaySelectTimer = null
    }

    @Test
    fun test() {
        val y1 = 50.0
        val y2 = 10.0
        var notGate1 = LogicGateView.notGateView().apply { location = Point2D(-400.0, y1) }
        var notGate2 = LogicGateView.notGateView().apply { location = Point2D(0.0, y2) }

        notGate1 = EditModule.drawingAppService.add(notGate1, editor.view) as LogicGateView
        notGate2 = EditModule.drawingAppService.add(notGate2, editor.view) as LogicGateView

        // Connect notGate1 open-ended
        var edgeView1 = GraphViewModule.getEdgeViewFactory()
            .createEdgeView<DigitalSignal>(editor.drawing as GraphView)
            .addSegmentPoint(Point2D(-400.0, y1))
            .addSegmentPoint(Point2D(-250.05568600010918,63.43638699808889))
        edgeView1 = EditModule.drawingAppService.add(edgeView1, editor.view) as EdgeView<DigitalSignal>
        editor.commandManager.beginTransaction("graph.command.connect", editor.view)
        editor.commandManager.execute(ConnectOriginCommand(
            editor,
            GraphViewModule.graphViewConnectService,
            edgeView1.id,
            notGate1.id,
            2
        ))
        editor.commandManager.commitTransaction()

        // Connect notGate2 open-ended
        var edgeView2 = GraphViewModule.getEdgeViewFactory()
            .createEdgeView<DigitalSignal>(editor.drawing as GraphView)
            .addSegmentPoint(Point2D(-252.05568600010918,68.43638699808889))
            .addSegmentPoint(Point2D(-69.0, y2))
        edgeView2 = EditModule.drawingAppService.add(edgeView2, editor.view) as EdgeView<DigitalSignal>
        editor.commandManager.beginTransaction("graph.command.connect", editor.view)
        editor.commandManager.execute(ConnectDestinationCommand(
            editor,
            GraphViewModule.graphViewConnectService,
            edgeView2.id,
            notGate2.id,
            1
        ))
        editor.commandManager.commitTransaction()

        // Move endpoint
        DESTINATION.moveTo(edgeView1, edgeView2.originEndpointView.location)
        DESTINATION.layout(edgeView1, null)

        // Join EdgeViews
        editor.commandManager.execute(JoinEdgeViewEndpointsCommand<Any>(
            editor,
            GraphViewModule.graphViewConnectService,
            edgeView1.id,
            DESTINATION,
            edgeView2.id,
            ORIGIN
        ))

        assertEquals(1, (editor.drawing as GraphView).getEdgeViews().size)

        editor.commandManager.undo()
        editor.commandManager.redo()

        assertEquals(1, (editor.drawing as GraphView).getEdgeViews().size)
    }
}