package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Graphics2D
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.issue.IssueCollector
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.TestLibraryBuilder
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.IOModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SubGraphVerticeViewDslDrawSymbolTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	@BeforeTest
	fun setup() {
		Translations.withAnyKey()
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		LibraryModule.libraryService = LibraryService()
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
	}

	private val signalHandler = mockk<SignalHandler>(relaxed = true)

	@Test
	fun shouldDrawExecSymbol() {
		val issueCollector = IssueCollector()
		val libraryElement = createMetaGraph(
			inputName = "I",
			outputName = "O",
			drawExecScript = "drawDataFlow(\"I\", \"O\")")

		val vv = createAndStart(libraryElement)

		val g2 = mockk<Graphics2D>(relaxed = true)
		val appContext = mockk<GraphApplicationContext>()
		every { appContext.isExecute } returns true
		every { appContext.systemSpeedCategory } returns CurrentSystemSpeedCategory(SystemSpeed(SystemSpeed.DEFAULT_SPEED))
		every { appContext.isPausing } returns false
		val drawContext = mockk<DrawContext>(relaxed = true)
		every { drawContext.g } returns g2
		every { drawContext.castedAppContext<GraphApplicationContext>() } returns appContext

		vv.draw(drawContext)

		assertEquals(0, issueCollector.size)
		verify { g2.drawLine(any<Double>(), any(), any(), any()) }
	}

	private fun createAndStart(libraryElement: ContainerLibraryElement): SubGraphVerticeView<SubGraphVerticeRef> {
		val vv = libraryElement.getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView
		vv.model.bind(true, LibraryModule.libraryHolder.library, IOModule.storableCreator)
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
		LibraryModule.libraryService.updateContainerLibraryElement(library, libraryElement)
		return libraryElement
	}
}