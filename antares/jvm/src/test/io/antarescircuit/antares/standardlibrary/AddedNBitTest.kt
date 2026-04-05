package io.antarescircuit.antares.standardlibrary

import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals

class AddedNBitTest : AbstractStandardLibraryBasedCircuitTest() {

    private lateinit var adderView: SubGraphVerticeView<*>

    override fun createCircuit(): GraphView {
        val builder = TestCircuitBuilder("test", styleProvider, eventBus)
        adderView = builder.add(LibraryModule.libraryHolder
            .getContainerLibraryElement(UUID("632c1059-c657-4521-9c28-f503920bcad7"))!!
            .getNewInstance()) as SubGraphVerticeView<*>
        return builder.build()
    }

    /** Regression test for GitHub #1170. */
    @Test
    fun shouldOutputZeroWithComplementaryInput() {
        startSimulation()

        // A = -1 in two's complement, which is FF
        adderView.model.getInput<DigitalSignal>("A").setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_8, 255UL), scheduler)
        // B = 1
        adderView.model.getInput<DigitalSignal>("B").setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_8, 1UL), scheduler)
        // C = 0
        adderView.model.getInput<DigitalSignal>("CI").setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_1, 0UL), scheduler)
        proceedUntilQueueIsEmpty()

        val sum = adderView.model.getOutput<DigitalSignal>("S").getOutgoingSignal()!!
        assertEquals(BitWidth.BW_8, sum.bitWidth)
        assertEquals(DigitalSignalFactory.of(BitWidth.BW_8, 0UL), sum)
    }
}