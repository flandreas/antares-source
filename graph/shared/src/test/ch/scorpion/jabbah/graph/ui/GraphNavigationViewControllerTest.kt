package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.execution.scheduler.SchedulerSingleStepModeEvent
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.TestLibraryBuilder
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeEvent
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.*

class GraphNavigationViewControllerTest {

	companion object {
		init {
			GraphViewTestRule.configure()

			LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
			LibraryModule.libraryService = LibraryService()
			LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
		}
	}

	private val scheduler = mockk<Scheduler>(relaxed = true)
	private val eventBus = EventBusImpl()
	private val systemSpeed = SystemSpeed(eventBus = eventBus)
	private val currentSystemSpeedCategory = CurrentSystemSpeedCategory(systemSpeed, eventBus)
	private val graphViewBuilder = GraphViewBuilder<Boolean>()
	private val applicationContextHolder = GraphApplicationContextHolder(scheduler, eventBus, systemSpeed, currentSystemSpeedCategory)
	private val applicationModeHolder = mockk<ApplicationModeHolder>().also {
		applicationContextHolder.applicationModeHolder = it
		every { it.currentMode } returns ApplicationMode.EDIT
	}
	private val drawingView = DrawingViewImpl(
		drawing = graphViewBuilder.graphView as Drawing<Component>,
		applicationContextHolder = applicationContextHolder,
		eventBus = eventBus)
	private val vv = createSubGraphVerticeView()
	private val controller = GraphNavigationViewController(isRoot = true, drawingView as DrawingView<GraphView>, eventBus = eventBus)

	init {
		drawingView.canvas = mockk(relaxed = true)
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
		val savable = mockk<Savable>()
		every { savable.editable } returns false
		eventBus.post(CurrentSavableEvent(savable))

		assertFalse(drawingView.editable)
	}

	@Test
	fun shouldBindOnExecutionStart() {
		val testVertice = mockk<Vertice>(relaxed = true)
		val testVerticeView = mockk<VerticeView<Vertice>>(relaxed = true)
		every { testVerticeView.model } returns testVertice
		every { scheduler.isActive } returns true
		every { applicationModeHolder.currentMode } returns ApplicationMode.EXECUTE

		graphViewBuilder.addVerticeView(testVerticeView)
		controller.setRootGraphView(graphViewBuilder.build(), editable = true)

		eventBus.post(SchedulerActivationStateEvent(scheduler))

		verify { testVerticeView.bind(eq(graphViewBuilder.graph)) }
		verify { testVertice.executionInitialize(any()) }
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