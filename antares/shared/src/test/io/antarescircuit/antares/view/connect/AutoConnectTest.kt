package io.antarescircuit.antares.view.connect

import dev.mokkery.mock
import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.view.AbstractGraphViewEditingTest
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.app.GraphViewAppServiceImpl
import io.antarescircuit.jabbah.graph.view.editor.AutoConnector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AutoConnectTest : AbstractGraphViewEditingTest(10) {

    private val graphView: GraphView get() = editor.view.drawing as GraphView

    init {
        editor.dragManager.registerPlugin(AutoConnector)
    }

    override fun setupCircuit() {
        AntaresTestRule.configure()
        EditModule.drawingAppService = GraphViewAppServiceImpl(mock())
    }

    /** Regression test for bug #1231. */
    @Test
    fun shouldNotConnectPortMoreThanOnce() {
        val input = EditModule.drawingAppService.add(
            DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.INPUT)).apply {
                location = Point2D(0, 0)
            },
            editor.view
        ) as DigitalCircuitInOutView

        val and = EditModule.drawingAppService.add(
            LogicGateView.andGateView().apply { location = Point2D(200, 0) },
            editor.view
        ) as LogicGateView

        // Connect port 1 of the AndGate open-ended
        and.getPortView(and.model.getPort(1))!!.connectionPoint.let {
            driver.mouseMoveTo(and.location.xInt + it.xInt, and.location.yInt + it.yInt)
            driver.pressMouseAt(and.location.xInt + it.xInt, and.location.yInt + it.yInt)
            driver.dragMouseAndReleaseAt(input.location.xInt + 3, input.location.yInt)
        }
        assertEquals(1, graphView.getEdgeViews().size)
        // Make sure it is NOT connected to the nearby port of the InOutView
        assertFalse(input.model.getPort<DigitalPort>().isConnected)

        // Drag the AndGate so that its port 2 coincides with the port of the InOutView AS WELL AS the open-ended EdgeView
        val start = and.boundingBox.center
        val offset = and.getPortView(and.model.getPort(2))!!.connectionPoint
            .add(and.location)
            .subtract(input.location)
            .negate
        and.boundingBox.center.let {
            driver.mouseMoveTo(start.xInt, start.yInt)
            driver.pressMouseAt(start.xInt, start.yInt)
            driver.dragMouseAndReleaseAt(start.xInt + offset.xInt, start.yInt + offset.yInt)
        }
    }
}