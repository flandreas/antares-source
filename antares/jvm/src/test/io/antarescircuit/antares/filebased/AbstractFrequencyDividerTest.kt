package io.antarescircuit.antares.filebased

import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractFrequencyDividerTest(
    private val uuid: UUID,
    private val switchId: Int,
    private val outputId: Int
) : AbstractFileBasedTest() {
    private lateinit var switch: SwitchView
    private lateinit var output: DigitalCircuitInOutView

    @BeforeTest
    fun openCircuit() {
        openCircuit(uuid)

        switch = openedCircuitView.getWithId(switchId) as SwitchView
        output = openedCircuitView.getWithId(outputId) as DigitalCircuitInOutView
    }

    @Test
    fun shouldNotCountDuringStartupDeep() {
        shouldNotCountDuringStartup()
    }

    @Test
    fun shouldNotCountDuringStartupScripted() {
        scheduler.isDeepExecution = false
        shouldNotCountDuringStartup()
    }

    private fun shouldNotCountDuringStartup() {
        startSimulation()
        proceedUntilQueueIsEmpty()

        assertEquals(DigitalSignalFactory.of(BitWidth.BW_3, 0UL), output.model.signal)
    }

    @Test
    fun shouldCountDeep() {
        shouldCount()
    }

    @Test
    fun shouldCountScripted() {
        scheduler.isDeepExecution = false
        shouldCount()
    }

    private fun shouldCount() {
        startSimulation()
        proceedUntilQueueIsEmpty()

        clockUpDown()
        assertEquals(DigitalSignalFactory.of(BitWidth.BW_3, 1UL), output.model.signal)

        clockUpDown()
        assertEquals(DigitalSignalFactory.of(BitWidth.BW_3, 2UL), output.model.signal)
    }

    private fun clockUpDown() {
        switch.model.toggle(scheduler)
        proceedUntilQueueIsEmpty()
        switch.model.toggle(scheduler)
        proceedUntilQueueIsEmpty()
    }
}