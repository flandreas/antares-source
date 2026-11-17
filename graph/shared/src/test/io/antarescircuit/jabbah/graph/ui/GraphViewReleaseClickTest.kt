package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.base.event.*
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.draw.InputEventHandlerAdapter
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.DrawingViewMockBuilder
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorViewBag
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.app.ConstantApplicationModeHolder
import io.antarescircuit.jabbah.graph.model.TestVertice
import io.antarescircuit.jabbah.graph.view.*
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeViewTimeline
import io.antarescircuit.jabbah.graph.view.oscilloscope.SignalHistoryTimelineViewImpl
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import kotlin.test.*

class GraphViewReleaseClickTest {

	init { GraphViewTestRule.configure() }

	private class Recorder : InputEventHandlerAdapter<InputEventContext>() {
		val clicks = mutableListOf<InputEventContext>()
		var releases = 0
		var drags = 0
		override fun mousePressed(context: InputEventContext): InputEventHandler<InputEventContext> = this
		override fun mouseDragged(context: InputEventContext): InputEventHandler<InputEventContext> {
			drags++
			return this
		}
		override fun mouseReleased(context: InputEventContext): InputEventHandler<InputEventContext>? {
			releases++
			return null
		}
		override fun mouseClicked(context: InputEventContext): InputEventHandler<InputEventContext>? {
			clicks.add(context)
			return null
		}
	}

	private val graph = mock<GraphView>(MockMode.autofill)
	private val view: DrawingView<GraphElementView<*>, GraphView> = DrawingViewMockBuilder().withDrawing(graph).build()
	private val scheduler = mock<Scheduler>(MockMode.autofill)
	private val eventBus = EventBusImpl()
	private val holder = GraphApplicationContextHolder(scheduler, eventBus).apply {
		applicationModeHolder = ConstantApplicationModeHolder(ApplicationMode.EXECUTE)
	}
	private val actor = mock<VerticeView<TestVertice>>(MockMode.autofill)
	private val recorder = Recorder()
	private val mouse = Capture.slot<MouseListener>()
	private val propertyChange = Capture.slot<PropertyChangeListener<Any>>()

	init {
		every { view.applicationContextHolder } returns holder
		every { scheduler.isActive } returns false
		every { view.addMouseListener(capture(mouse)) } returns Unit
		every { view.addPropertyChangeListener(capture<PropertyChangeListener<Any>>(propertyChange)) } returns Unit
		every { view.getInnerDrawableAt(any(), any(), any()) } returns actor
		every { actor.getActorInteractionHandler(any()) } returns recorder
		every { actor.getInputEventHandler(any<InputEventContext>()) } returns recorder
		every { graph.getDrawableAt(any<Point2D>()) } returns actor
	}

	@AfterTest
	fun disposeContext() {
		holder.dispose()
	}

	private fun event(type: MouseEventType, x: Int = 100, count: Int = 1) = MouseEventImpl(
		type = type, x = x, y = 100, button = Button.BUTTON1, clickCount = count,
	)

	private fun activateExecution(): GraphViewExecutionHandler {
		val handler = GraphViewExecutionHandler(view, holder, eventBus)
		propertyChange.get().propertyChanged(PropertyChangeEvent(view, View.PROP_CANVAS, null, view.canvas))
		return handler
	}

	@Test
	fun shouldMoveNestedTimelineTriggerImmediatelyAndRestoreThresholdForNextActor() {
		val timelineView = SignalHistoryTimelineViewImpl(rightInset = 20)
		timelineView.setBounds(0.0, 0.0, 100.0, 20.0)
		timelineView.bind(null, OscilloscopeViewTimeline(scale = 1_000.0) { 1_000L })
		val bag = ActorViewBag<Drawable>(location = Point2D(60, 90), useLocation = true)
		bag.add(timelineView)
		every { actor.getActorInteractionHandler(any()) } returns
			bag.getActorInteractionHandler(ActorInteractionContext(scheduler, view))
		val handler = activateExecution()
		val listener = mouse.get()

		// The trigger is at local (40, 10), hence (100, 100) in the outer view.
		listener.mousePressed(event(MouseEventType.PRESSED))
		(listener as MouseMotionListener).mouseDragged(event(MouseEventType.DRAGGED, 101))
		assertEquals(1.0, timelineView.scrollX)
		listener.mouseReleased(event(MouseEventType.RELEASED, 101))
		assertEquals(1.0, timelineView.scrollX)

		every { actor.getActorInteractionHandler(any()) } returns recorder
		listener.mousePressed(event(MouseEventType.PRESSED, 200))
		listener.mouseDragged(event(MouseEventType.DRAGGED, 202))
		listener.mouseReleased(event(MouseEventType.RELEASED, 202))
		assertEquals(0, recorder.drags)
		assertEquals(1, recorder.clicks.size)
		handler.dispose()
	}

	@Test
	fun shouldResizeNestedTimelineGridImmediately() {
		val timeline = OscilloscopeViewTimeline(scale = 1_000.0) { 1_000L }
		val timelineView = SignalHistoryTimelineViewImpl(rightInset = 20)
		timelineView.setBounds(0.0, 0.0, 100.0, 20.0)
		timelineView.bind(null, timeline)
		val bag = ActorViewBag<Drawable>(location = Point2D(60, 90), useLocation = true)
		bag.add(timelineView)
		every { actor.getActorInteractionHandler(any()) } returns
			bag.getActorInteractionHandler(ActorInteractionContext(scheduler, view))
		val handler = activateExecution()
		val listener = mouse.get()

		// The next grid line is eight model units to the right of the trigger.
		listener.mousePressed(event(MouseEventType.PRESSED, 108))
		(listener as MouseMotionListener).mouseDragged(event(MouseEventType.DRAGGED, 109))
		assertEquals(9.0, timeline.divisionWidth)
		listener.mouseReleased(event(MouseEventType.RELEASED, 109))
		handler.dispose()
	}

	@Test
	fun shouldClickActorOnReleaseWithMovementAndIgnoreNativeClick() {
		val handler = activateExecution()
		val listener = mouse.get()
		listener.mousePressed(event(MouseEventType.PRESSED))
		(listener as MouseMotionListener).mouseDragged(event(MouseEventType.DRAGGED, 102))
		listener.mouseReleased(event(MouseEventType.RELEASED, 102))
		listener.mouseClicked(event(MouseEventType.CLICKED, 102))
		assertEquals(1, recorder.releases)
		assertEquals(0, recorder.drags)
		assertEquals(Point2D(102, 100), recorder.clicks.single().location)
		handler.dispose()
	}

	@Test
	fun shouldNotClickActorAfterDragReturnsToPressLocation() {
		val handler = activateExecution()
		val listener = mouse.get()
		listener.mousePressed(event(MouseEventType.PRESSED))
		(listener as MouseMotionListener).mouseDragged(event(MouseEventType.DRAGGED, 130))
		listener.mouseReleased(event(MouseEventType.RELEASED))
		assertEquals(1, recorder.releases)
		assertEquals(1, recorder.drags)
		assertTrue(recorder.clicks.isEmpty())
		handler.dispose()
	}

	@Test
	fun shouldNotClickDifferentActorAtRelease() {
		val other = mock<VerticeView<TestVertice>>(MockMode.autofill)
		every { view.getInnerDrawableAt(102.0, 100.0, any()) } returns other
		val handler = activateExecution()
		val listener = mouse.get()
		listener.mousePressed(event(MouseEventType.PRESSED))
		listener.mouseReleased(event(MouseEventType.RELEASED, 102))
		assertTrue(recorder.clicks.isEmpty())
		handler.dispose()
	}

	@Test
	fun shouldDoubleClickReadOnlyDrawableOnRelease() {
		every { view.editable } returns false
		val handler = GraphViewDisplayHandler(view, holder, eventBus)
		propertyChange.get().propertyChanged(PropertyChangeEvent(view, DrawingView.PROP_EDITABLE, true, false))
		val listener = mouse.get()
		listener.mousePressed(event(MouseEventType.PRESSED, count = 2))
		listener.mouseReleased(event(MouseEventType.RELEASED, 102, count = 0))
		assertEquals(2, recorder.clicks.single().mouseEvent!!.clickCount)
		handler.dispose()
	}

	@Test
	fun shouldRejectReadOnlyClickOnDifferentDrawable() {
		every { view.editable } returns false
		every { graph.getDrawableAt(Point2D(102, 100)) } returns null
		val handler = GraphViewDisplayHandler(view, holder, eventBus)
		propertyChange.get().propertyChanged(PropertyChangeEvent(view, DrawingView.PROP_EDITABLE, true, false))
		val listener = mouse.get()
		listener.mousePressed(event(MouseEventType.PRESSED))
		listener.mouseReleased(event(MouseEventType.RELEASED, 102))
		assertTrue(recorder.clicks.isEmpty())
		handler.dispose()
	}

	@Test
	fun shouldUnregisterEventHandlersWhenDisplayHandlerIsDisposed() {
		val registeredBefore = eventBus.size
		GraphViewDisplayHandler(view, holder, eventBus).dispose()
		assertEquals(registeredBefore, eventBus.size)
	}
}
