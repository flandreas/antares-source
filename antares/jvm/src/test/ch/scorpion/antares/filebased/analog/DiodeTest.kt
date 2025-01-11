package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.view.analog.AnalogEdgeView
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test

class DiodeTest : AbstractAnalogFileBasedTest() {

    private lateinit var edgeView1: AnalogEdgeView
    private lateinit var edgeView2: AnalogEdgeView

    @BeforeTest
    fun openCircuit() {
        openCircuit(UUID("cbfdff1c-2294-4b3e-8876-b98e77f22e3c"))

        edgeView1 = openedCircuitView.getWithId(17) as AnalogEdgeView
        edgeView2 = openedCircuitView.getWithId(15) as AnalogEdgeView
    }

    @Test
    fun shouldSimulate() {
        startSimulation()
        processUntilQueueIsEmpty()

        assertCurrent(0.212, edgeView1.current)
        assertCurrent(0.0, edgeView2.current)
    }
}