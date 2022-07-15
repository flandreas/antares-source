package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.drawable.RotationDirection
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.graph.DrawingViewMockBuilder
import ch.scorpion.jabbah.graph.TestLibraryBuilder
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.container.ControlViewComponent
import ch.scorpion.jabbah.graph.container.OriginIndicator
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.io.StorableCloner
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SubGraphVerticeViewImplTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	@BeforeTest
	fun setup() {
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		LibraryModule.libraryService = LibraryService()
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
	}

	@Test
	fun shouldFindRotatedControlView() {
		val handler = mockk<ActorInteractionHandler>(relaxed = true)
		val controlView = TestControlVerticeView(rectangle = Rectangle2D(0, 0, 100, 100))

		val containerDrawing = ContainerDrawing()
		containerDrawing.add(ControlViewComponent(source = controlView as ControlViewSource<Vertice>))
		containerDrawing.add(OriginIndicator())

		// This clones the ControlViews, so set up handler on clone
		val vv = SubGraphVerticeViewImpl()
		vv.fillFromContainerDrawing(containerDrawing)
		(vv.getControlViewComponents().first().controlView as TestControlVerticeView).actorInteractionHandler = handler
		vv.rotate(RotationDirection.CounterClockwise)

		val context = contextFor(50.0, -50.0)
		vv.getActorInteractionHandler(context).mouseMoved(context)

		verify { handler.mouseMoved(any()) }
	}

	@Test
	fun shouldLoadCustomLabelEvenWithCustomSymbol() {
		val builder = GraphViewBuilder<Boolean>()
		DrawingViewMockBuilder().withDrawing(builder.graphView)

		val libraryElement = createMetaGraph("L")
		val vv = libraryElement.getNewInstance<Vertice>() as SubGraphVerticeView
		builder.addVerticeView(vv)

		// Edit ContainerDrawing
		val containerDrawing = vv.getEditableContainerDrawing()
		containerDrawing.add(TestVerticeView.createEastOutputVerticeView("Hello", 100, 100))
		vv.setEditedContainerDrawing(containerDrawing)

		// Change custom label
		vv.label = TranslatableText("Test")

		// Store and load
		val clone = StorableCloner.clone(builder.graphStorable)
		val vvClone = clone.graphView.getVerticeViews().filterIsInstance<SubGraphVerticeView<*>>().first()

		assertEquals("Test", vvClone.label!!.getTranslation())
	}

	private fun contextFor(x: Double, y: Double, clickCount: Int = 0): ActorInteractionContext {
		val mouseEvent = mockk<MouseEvent>()
		every { mouseEvent.clickCount } returns clickCount
		every { mouseEvent.button} returns Button.BUTTON1
		return ActorInteractionContext(
			signalHandler = mockk(),
			view = mockk(),
			mouseEvent = mouseEvent,
			keyEvent = mockk(),
			x = x,
			y = y
		)
	}

	private fun createMetaGraph(label: String): ContainerLibraryElement {
		val library = LibraryModule.libraryHolder.library
		val metaGraph = TestLibraryBuilder().addInnerCustomComponent(library, label)
		val libraryElement = library.getContainerLibraryElement(metaGraph.uuid)!!
		LibraryModule.libraryService.updateContainerLibraryElement(library, metaGraph, libraryElement)
		return libraryElement
	}
}