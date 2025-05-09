package ch.scorpion.antares.view.input

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.inout.DigitalCircuitInOutImpl
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.antares.view.net.DigitalEdgeView
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests the simulation behaviour of [DoubleThrowSwitchView] with a [DigitalCircuitInOutView] that
 * can go into state "undefined".
 */
class DoubleThrowSwitchViewUndefinedSimulationTest : AbstractCircuitTest() {

    private lateinit var circuitView: GraphView
    private lateinit var switchView: SwitchView
    private lateinit var doubleThrowSwitchView: DoubleThrowSwitchView
    private lateinit var inOutView1: DigitalCircuitInOutView
    private lateinit var inOutView2: DigitalCircuitInOutView
    private lateinit var edgeView: DigitalEdgeView

    override fun getCircuitView(): GraphView = circuitView

    @BeforeTest
    fun setupCircuit() {
        val builder = TestCircuitBuilder("test", styleProvider, eventBus)

        switchView = builder.addVerticeView(SwitchView())
        doubleThrowSwitchView = builder.addVerticeView(DoubleThrowSwitchView())
        inOutView1 = builder.addVerticeView(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.INOUT)))
        inOutView2 = builder.addVerticeView(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.INOUT)))

        edgeView = builder.connect(switchView, doubleThrowSwitchView, toPort = doubleThrowSwitchView.model.getInput(1)) as DigitalEdgeView
        builder.connect(doubleThrowSwitchView, fromPort = doubleThrowSwitchView.model.getOutput(2), inOutView1)
        builder.connect(doubleThrowSwitchView, fromPort = doubleThrowSwitchView.model.getOutput(3), inOutView2)

        circuitView = builder.build()
    }

    @Test
    fun shouldSetInOut() {
        startSimulation()
        proceedUntilQueueIsEmpty()

        switchView.model.toggle(scheduler)
        proceedUntilQueueIsEmpty()

        assertNull(edgeView.model.executionError)
        assertEquals(DigitalSignalFactory.of(true), inOutView2.model.signal)
    }
}