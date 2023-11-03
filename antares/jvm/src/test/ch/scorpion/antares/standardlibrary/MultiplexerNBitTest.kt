package ch.scorpion.antares.standardlibrary

import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_8
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
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