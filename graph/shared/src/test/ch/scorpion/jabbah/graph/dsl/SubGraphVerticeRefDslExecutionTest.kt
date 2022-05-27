package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.TestLibraryBuilder
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.IOModule
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for execution of DSL scripts of [SubGraphVerticeRef]s.
 */
class SubGraphVerticeRefDslExecutionTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val signalHandler = mockk<SignalHandler>(relaxed = true)

	@BeforeTest
	fun setup() {
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		LibraryModule.libraryService = LibraryService()
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
	}

	@Test
	fun shouldAddGraphPortsToMemoryContext() {
		val libraryElement = createScriptedMetaGraph(
			inputName = "I",
			outputName = "O",
			script = "O = I")

		val vv = createAndStart(libraryElement)

		vv.model.getInput<Boolean>().setIncomingSignal(true, signalHandler)
		vv.model.act(signalHandler, vv.model.createActorData(vv.model.getInput<Boolean>()))

		assertTrue(vv.model.getOutput<Boolean>().getOutgoingSignal()!!)
	}

	@Test
	fun shouldQuotePortNames() {
		val libraryElement = createScriptedMetaGraph(
			inputName = "In I",
			outputName = "!O",
			script = "\'!O\' = \'In I\'")

		val vv = createAndStart(libraryElement)

		vv.model.getInput<Boolean>().setIncomingSignal(true, signalHandler)
		vv.model.act(signalHandler, vv.model.createActorData(vv.model.getInput<Boolean>()))

		assertTrue(vv.model.getOutput<Boolean>().getOutgoingSignal()!!)
	}

	@Test
	fun shouldExecuteInitBlockOnExecutionStart() {
		val libraryElement = createScriptedMetaGraph(
			inputName = "I",
			outputName = "O",
			script = """
				init {
					O = 1
				}
				O = 666
			""".trimIndent())

		val vv = createAndStart(libraryElement)
		val o = vv.model.getOutput<Long>().getOutgoingSignal()

		assertEquals(1L, o)
	}

	@Test
	fun shouldNotExecuteInitBLockOnActing() {
		val libraryElement = createScriptedMetaGraph(
			inputName = "I",
			outputName = "O",
			script = """
				init {
					O = 1
				}
				O = 42
			""".trimIndent())

		val vv = createAndStart(libraryElement)
		vv.model.act(signalHandler, vv.model.createActorData(vv.model.getInput<Boolean>()))

		val o = vv.model.getOutput<Long>().getOutgoingSignal()
		assertEquals(42L, o)
	}

	private fun createAndStart(libraryElement: ContainerLibraryElement): SubGraphVerticeView<SubGraphVerticeRef> {
		val vv = libraryElement.getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView
		vv.model.bind(true, LibraryModule.libraryHolder.library, IOModule.storableCreator)
		vv.model.executionInitialize(signalHandler)
		vv.model.executionStart(signalHandler)
		return vv
	}

	private fun createScriptedMetaGraph(inputName: String, outputName: String, script: String): ContainerLibraryElement {
		val library = LibraryModule.libraryHolder.library
		val metaGraph = TestLibraryBuilder().addInnerCustomComponent(library, inputName = inputName, outputName = outputName)
		metaGraph.graph.model!!.script = script
		val libraryElement = library.getContainerLibraryElement(metaGraph.uuid)!!
		LibraryModule.libraryService.updateContainerLibraryElement(library, metaGraph, libraryElement)
		return libraryElement
	}
}