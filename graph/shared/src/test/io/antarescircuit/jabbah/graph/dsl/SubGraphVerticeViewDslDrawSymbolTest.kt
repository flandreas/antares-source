package io.antarescircuit.jabbah.graph.dsl

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.edit.model.text.ScriptProperty
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.issue.IssueCollector
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.TestLibraryBuilder
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryImpl
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.MemoryLibraryPersistenceService
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import io.antarescircuit.jabbah.graphics.Graphics2DMockBuilder
import dev.mokkery.MockMode
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SubGraphVerticeViewDslDrawSymbolTest {

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
		Translations.withAnyKey()
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
	}

	private val signalHandler = mock<SignalHandler>(MockMode.autofill)

	@Test
	fun shouldDrawExecSymbol() {
		val issueCollector = IssueCollector()
		val libraryElement = createMetaGraph(
			inputName = "I",
			outputName = "O",
			drawExecScript = "drawDataFlow(\"I\", \"O\")")

		val vv = createAndStart(libraryElement)

		val g2 = Graphics2DMockBuilder().build()
		val appContext = GraphApplicationContext(
			CurrentSystemSpeedCategory(SystemSpeed(SystemSpeed.DEFAULT_SPEED)),
			ApplicationMode.EXECUTE)
		val drawContext = DrawContext(g2, appContext = appContext)

		vv.draw(drawContext)

		assertEquals(0, issueCollector.size)
		verify { g2.drawLine(any<Double>(), any(), any(), any()) }
	}

	private fun createAndStart(libraryElement: ContainerLibraryElement): SubGraphVerticeView<SubGraphVerticeRef> {
		val vv = libraryElement.getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView
		vv.model.bind(true, LibraryModule.libraryHolder.library)
		vv.model.executionInitialize(signalHandler)
		vv.model.executionStart(signalHandler)
		vv.executionStarted(signalHandler)
		return vv
	}

	private fun createMetaGraph(inputName: String, outputName: String, drawExecScript: String): ContainerLibraryElement {
		val library = LibraryModule.libraryHolder.library
		val metaGraph = TestLibraryBuilder().addInnerCustomComponent(library, inputName = inputName, outputName = outputName)
		metaGraph.containerDrawing.execDrawScript = ScriptProperty(drawExecScript)
		val libraryElement = library.getContainerLibraryElement(metaGraph.uuid)!!
		LibraryModule.libraryService.updateContainerLibraryElement(library, metaGraph, libraryElement)
		return libraryElement
	}
}