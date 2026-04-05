package io.antarescircuit.antares.standardlibrary

import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_8
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals

class MultiplexerNBitTest : AbstractStandardLibraryBasedCircuitTest() {

	private lateinit var multiplexerView: SubGraphVerticeView<*>

	override fun createCircuit(): GraphView {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)
		multiplexerView = builder.add(LibraryModule.libraryHolder
			.getContainerLibraryElement(UUID("ae0652c3-7ad1-4664-9758-c4d2050e76a5"))!!
			.getNewInstance()) as SubGraphVerticeView<*>
		return builder.build()
	}

	@Test
	fun shouldOutputFalseWithUndefinedInput() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.falseValue(BW_8), multiplexerView.model.getOutput<DigitalSignal>("O").getOutgoingSignal())
	}
}