package io.antarescircuit.jabbah.graph.dsl

import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.issue.IssueCollector
import io.antarescircuit.jabbah.graph.TestLibraryBuilder
import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for execution of DSL scripts of [SubGraphVerticeRef]s.
 */
class SubGraphVerticeRefDslExecutionTest {

	private val signalHandler = mock<SignalHandler>(MockMode.autofill)
	private lateinit var issueCollector: IssueCollector

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
		issueCollector = IssueCollector()
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
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

	@Test
	fun semanticAnalyserShouldNotAllowWritingInputPort() {
		val libraryElement = createScriptedMetaGraph(
			inputName = "I",
			outputName = "O",
			script = """
				I = 1
			""".trimIndent())

		val vv = createAndStart(libraryElement)
		vv.model.act(signalHandler, vv.model.createActorData(vv.model.getInput<Boolean>()))

		assertEquals(1, issueCollector.size)
		issueCollector.issues[0].description!!.contains("Assigning value to input")
	}

	private fun createAndStart(libraryElement: ContainerLibraryElement): SubGraphVerticeView<SubGraphVerticeRef> {
		val vv = libraryElement.getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView
		vv.model.bind(true, LibraryModule.libraryHolder.library)
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