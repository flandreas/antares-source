package ch.scorpion.antares.standardlibrary

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_8
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService
import ch.scorpion.jabbah.graph.library.dictionary.ResourceLibraryDictionaryPersistenceService
import ch.scorpion.jabbah.graph.module.GraphModule
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals

class MultiplexerNBitTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var multiplexerView: SubGraphVerticeView<*>

	override fun getCircuitView(): GraphView = circuitView

	override fun setup() {
		super.setup()
		setupLibrary()
		setupCircuit()
	}

	private fun setupLibrary() {
		LibraryModule.systemLibraryPersistenceService = ResourceLibraryPersistenceService()
		LibraryModule.systemLibraryDictionaryService = LibraryDictionaryService(
			ResourceLibraryDictionaryPersistenceService()
		)
		LibraryModule.libraryManagementService = LibraryManagementService()
		LibraryModule.libraryService = LibraryService()

		LibraryModule.libraryHolder.l = LibraryModule.libraryService.loadLibrary(
			LibraryIdentification(LibraryModule.DEF_LIBRARY_UUID, null), isSystem = true)
	}

	private fun setupCircuit() {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)
		multiplexerView = builder.add(GraphModule.metaGraphRepository
			.getContainerLibraryElement(UUID("ae0652c3-7ad1-4664-9758-c4d2050e76a5"))!!
			.getNewInstance()) as SubGraphVerticeView<*>
		circuitView = builder.build()
	}

	@Test
	fun shouldOutputFalseWithUndefinedInput() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.falseValue(BW_8), multiplexerView.model.getOutput<DigitalSignal>("O").getOutgoingSignal())
	}
}