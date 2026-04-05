package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.app.CurrentSavableEvent
import io.antarescircuit.jabbah.app.Savable
import io.antarescircuit.jabbah.base.event.EventBusImpl
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.draw.CanvasMockBuilder
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.view.DrawingViewImpl
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.execution.scheduler.SchedulerActivationStateEvent
import io.antarescircuit.jabbah.execution.scheduler.SchedulerSingleStepModeEvent
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.TestLibraryBuilder
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.app.ApplicationModeEvent
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolder
import io.antarescircuit.jabbah.graph.library.LibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryImpl
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.MemoryLibraryPersistenceService
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.*
import io.antarescircuit.jabbah.graph.view.vertice.OpenSubGraphRequest
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.eq
import dev.mokkery.mock
import dev.mokkery.verify
import kotlin.test.*

class GraphNavigationViewControllerTest {

	private val scheduler = mock<Scheduler>(MockMode.autofill)
	private val eventBus = EventBusImpl()
	private val systemSpeed = SystemSpeed(eventBus = eventBus)
	private val currentSystemSpeedCategory = CurrentSystemSpeedCategory(systemSpeed, eventBus)
	private val graphViewBuilder: GraphViewBuilder<Boolean>
	private val applicationContextHolder: GraphApplicationContextHolder
	private val applicationModeHolder = mock<ApplicationModeHolder>()
	private val drawingView: DrawingViewImpl<Drawing<Component>>
	private val vv: SubGraphVerticeView<*>
	private val controller: GraphNavigationViewController

	init {
		GraphViewTestRule.configure()
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
		graphViewBuilder = GraphViewBuilder<Boolean>()
		applicationContextHolder = GraphApplicationContextHolder(scheduler, eventBus, systemSpeed, currentSystemSpeedCategory)
		applicationContextHolder.applicationModeHolder = applicationModeHolder
		every { applicationModeHolder.currentMode } returns ApplicationMode.EDIT
		drawingView = DrawingViewImpl(
			drawing = graphViewBuilder.graphView as Drawing<Component>,
			applicationContextHolder = applicationContextHolder,
			eventBus = eventBus)
		vv = createSubGraphVerticeView()
		controller = GraphNavigationViewController(isRoot = true, drawingView as DrawingView<GraphView>, eventBus = eventBus)
		drawingView.canvas = CanvasMockBuilder().withView(drawingView).build()

		graphViewBuilder.addVerticeView(vv)
		GraphNavigationViewMockBuilder(controller)
	}

	@Test
	fun shouldSetRootGraphView() {
		controller.setRootGraphView(graphViewBuilder.build(), editable = true)
		assertEquals(1, controller.navigationStack.size)
		assertSame(graphViewBuilder.graphView, controller.navigationStack.rootEntry!!.content.drawing)
	}

	@Test
	fun shouldDescendIntoSubGraphWithoutAnimation() {
		controller.setRootGraphView(graphViewBuilder.build(), editable = true)
		eventBus.post(OpenSubGraphRequest(vv, newView = false, quickMode = true))
		assertEquals(2, controller.navigationStack.size)
		assertSame(controller.navigationStack.peek().content.drawing, drawingView.drawing as Drawing<GraphElementView<*>>)
	}

	@Test
	fun shouldAscendFromSubGraphWithoutAnimation() {
		controller.setRootGraphView(graphViewBuilder.build(), editable = true)
		eventBus.post(OpenSubGraphRequest(vv, newView = false, quickMode = true))

		controller.navigationStack.navigateBackTo(controller.navigationStack.rootEntry!!, quickMode = true)

		assertEquals(1, controller.navigationStack.size)
		assertSame(controller.navigationStack.rootEntry!!.content.drawing, drawingView.drawing as Drawing<GraphElementView<*>>)
	}

	@Test
	fun shouldPropagateContextWithSystemSpeedCategory() {
		controller.setRootGraphView(graphViewBuilder.build(), editable = true)
		systemSpeed.speed = SystemSpeed.MAX_SPEED

		assertEquals(currentSystemSpeedCategory, (drawingView.applicationContext as GraphApplicationContext).systemSpeedCategory)
	}

	@Test
	fun shouldPropagateContextWithApplicationMode() {
		every { scheduler.isActive } returns true
		every { applicationModeHolder.currentMode } returns ApplicationMode.EXECUTE
		controller.setRootGraphView(graphViewBuilder.build(), editable = true)
		eventBus.post(ApplicationModeEvent((drawingView.applicationContextHolder as GraphApplicationContextHolder).applicationModeHolder, ApplicationMode.EXECUTE))

		assertTrue((drawingView.applicationContext as GraphApplicationContext).isExecute)
	}

	@Test
	fun shouldPropagateContextWithSchedulerRunningState() {
		every { scheduler.isActive } returns true
		every { scheduler.isSingleStepMode } returns true
		controller.setRootGraphView(graphViewBuilder.build(), editable = true)

		eventBus.post(SchedulerSingleStepModeEvent(scheduler))

		assertTrue((drawingView.applicationContext as GraphApplicationContext).isPausing)
	}

	@Test
	fun shouldDisableViewWithNonEditableSavable() {
		controller.setRootGraphView(graphViewBuilder.build(), editable = true)
		val savable = mock<Savable>()
		every { savable.editable } returns false
		eventBus.post(CurrentSavableEvent(savable))

		assertFalse(drawingView.editable)
	}

	@Test
	fun shouldBindOnExecutionStart() {
		val testVertice = mock<Vertice>(MockMode.autofill)
		val testVerticeView = mock<VerticeView<Vertice>>(MockMode.autofill)
		every { testVerticeView.model } returns testVertice
		every { scheduler.isActive } returns true
		every { scheduler.isDeepExecution } returns true
		every { applicationModeHolder.currentMode } returns ApplicationMode.EXECUTE

		graphViewBuilder.addVerticeView(testVerticeView)
		controller.setRootGraphView(graphViewBuilder.build(), editable = true)

		eventBus.post(SchedulerActivationStateEvent(scheduler))

		verify { testVerticeView.bind(graphViewBuilder.graphView, true) }
		verify { testVertice.executionInitializeNonVolatile(any(), any()) }
		verify { testVertice.executionStart(any()) }
	}

	@Test
	fun shouldDeselectAllOnExecutionStart() {
		controller.setRootGraphView(graphViewBuilder.build(), editable = true)
		drawingView.content.selectionManager.select(vv)

		eventBus.post(ApplicationModeEvent((drawingView.applicationContextHolder as GraphApplicationContextHolder).applicationModeHolder, ApplicationMode.EXECUTE))

		assertFalse(drawingView.content.selectionManager.isSelected(vv))
	}

	private fun createSubGraphVerticeView(): SubGraphVerticeView<*> {
		val library = LibraryModule.libraryHolder.library
		TestLibraryBuilder().addInnerCustomComponent(library)
		return (library.get(TestLibraryBuilder.INNER_CUSTOM_COMP) as LibraryElement)
			.getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView<*>
	}
}