package ch.scorpion.antares.dsl

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestLibraryBuilder
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import io.mockk.mockk
import java.io.File
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.io.path.name
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AntaresDslGraphExecutionTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val signalHandler = mockk<SignalHandler>(relaxed = true)

	@BeforeTest
	fun setup() {
		val dir = Files.createTempDirectory(null)
		File.createTempFile("library", ".lib", dir.toFile())
		LibraryModule.userLibraryPersistenceService = FileLibraryPersistenceService({ dir.parent.absolutePathString() }, dir.name)
		LibraryModule.libraryService = LibraryService()
		LibraryModule.libraryHolder.l = LibraryImpl("test", libraryService = LibraryModule.libraryService)
	}

	@Test
	fun shouldInterpretRaisedInput() {
		val libraryElement = createScriptedMetaGraph("I1", "I2", "O", """
			if (^I2) {
				O = I1
			}
		""".trimIndent())

		val vv = createAndStart(libraryElement)

		vv.model.getInput<DigitalSignal>("I1").setIncomingSignal(Word.Companion.of(BitWidth.BW_1, 1UL), signalHandler)
		vv.model.act(signalHandler, vv.model.createActorData(vv.model.getInput<Boolean>("I1")))
		assertEquals(Word.of(BitWidth.BW_1, 0UL), vv.model.getOutput<DigitalSignal>("O").getOutgoingSignal())

		vv.model.getInput<DigitalSignal>("I2").setIncomingSignal(Word.of(BitWidth.BW_1, 1UL), signalHandler)
		vv.model.act(signalHandler, vv.model.createActorData(vv.model.getInput<Boolean>("I2")))
		assertEquals(Word.of(BitWidth.BW_1, 1UL), vv.model.getOutput<DigitalSignal>("O").getOutgoingSignal())
	}

	private fun createAndStart(libraryElement: ContainerLibraryElement): SubGraphVerticeView<SubGraphVerticeRef> {
		val vv = libraryElement.getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView
		vv.model.bind(true, LibraryModule.libraryHolder.library)
		vv.model.executionInitialize(signalHandler)
		vv.model.executionStart(signalHandler)
		return vv
	}

	private fun createScriptedMetaGraph(i1: String, i2: String, o: String, script: String): ContainerLibraryElement {
		val library = LibraryModule.libraryHolder.library
		val metaGraph = TestLibraryBuilder().addScriptedBinaryFunction(library, i1, i2, o, script)
		val libraryElement = library.getContainerLibraryElement(metaGraph.uuid)!!
		LibraryModule.libraryService.updateContainerLibraryElement(library, metaGraph, libraryElement)
		return libraryElement
	}
}