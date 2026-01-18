package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.drawable.RotationDirection
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.text.LabelComponent
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.DrawingViewMockBuilder
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
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
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SubGraphVerticeViewImplTest {

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
	}

	@Test
	fun shouldFindRotatedControlView() {
		val handler = mock<ActorInteractionHandler>(MockMode.autofill)
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
		containerDrawing.add(LabelComponent("Hello"))
		vv.setEditedContainerDrawing(containerDrawing)

		// Change custom label
		vv.label = TranslatableText("Test")

		// Store and load
		val clone = StorableCloner.clone(builder.graphStorable)
		val vvClone = clone.graphView.getVerticeViews().filterIsInstance<SubGraphVerticeView<*>>().first()

		assertEquals("Test", vvClone.label!!.getTranslation())
	}

	private fun contextFor(x: Double, y: Double, clickCount: Int = 0): ActorInteractionContext {
		val mouseEvent = mock<MouseEvent>()
		every { mouseEvent.clickCount } returns clickCount
		every { mouseEvent.button} returns Button.BUTTON1
		return ActorInteractionContext(
			signalHandler = mock(),
			view = DrawingViewMockBuilder().withDrawing(GraphViewBuilder<Boolean>().build()).build<Component>(),
			mouseEvent = mouseEvent,
			keyEvent = mock(),
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