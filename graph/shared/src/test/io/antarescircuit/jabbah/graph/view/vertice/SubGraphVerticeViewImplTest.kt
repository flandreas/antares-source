package io.antarescircuit.jabbah.graph.view.vertice

import io.antarescircuit.jabbah.base.event.Button
import io.antarescircuit.jabbah.base.event.MouseEvent
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.drawable.RotationDirection
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.model.text.LabelComponent
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.DrawingViewMockBuilder
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.graph.TestLibraryBuilder
import io.antarescircuit.jabbah.graph.container.ContainerDrawing
import io.antarescircuit.jabbah.graph.container.ControlViewComponent
import io.antarescircuit.jabbah.graph.container.OriginIndicator
import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.view.ControlViewSource
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.io.StorableCloner
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
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
		@Suppress("UNCHECKED_CAST")
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
			view = DrawingViewMockBuilder().withDrawing(GraphViewBuilder<Boolean>().build()).build<GraphElementView<*>, GraphView>(),
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