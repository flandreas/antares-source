package io.antarescircuit.jabbah.draw.view

import dev.mokkery.answering.calls
import dev.mokkery.every
import dev.mokkery.matcher.any
import io.antarescircuit.jabbah.base.PROP_BEGINNER_HELP_TOOLTIP
import io.antarescircuit.jabbah.base.PreferencesChangedEvent
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.Tooltip
import io.antarescircuit.jabbah.base.event.*
import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.time.Timer
import io.antarescircuit.jabbah.base.time.TimerHandler
import io.antarescircuit.jabbah.draw.*
import io.antarescircuit.jabbah.draw.container.DrawableContainerImpl
import io.antarescircuit.jabbah.draw.drawable.AbstractDrawable
import io.antarescircuit.jabbah.draw.drawable.RectangularDrawable
import io.antarescircuit.jabbah.draw.drawable.RichTextDrawable
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleType
import kotlin.test.*

class TooltipTest {

	private val previousTimerFactory = System.timerFactory
	private val previousEventBus = TooltipManager.eventBus
	private val eventBus = EventBusImpl()
	private val timers = mutableListOf<HoverTimer>()
	private val handlers = mutableListOf<TooltipHandler>()
	private val mouseListeners = mutableListOf<MouseListener>()
	private val motionListeners = mutableListOf<MouseMotionListener>()
	private val wheelListeners = mutableListOf<MouseWheelListener>()
	private val keyListeners = mutableListOf<KeyListener>()
	private lateinit var view: ViewImpl<InputEventContext>
	private lateinit var canvas: Canvas
	private lateinit var container: DrawableContainerImpl<Drawable>
	private lateinit var drawable: TooltipDrawable
	private lateinit var handler: TooltipHandler
	private var retrievals = 0

	private val timer get() = timers.last()

	@BeforeTest
	fun before() {
		DrawTestRule.configure()
		BaseModule.properties.set(TooltipManager.PROP_DELAY, 100)
		BaseModule.properties.set(PROP_BEGINNER_HELP_TOOLTIP, true)
		TooltipManager.eventBus = eventBus
		view = ViewImpl(
			affineTransformFactory = { System.createAffineTransform() },
			viewPainterFactory = { SimpleViewPainter(it) },
			applicationContextHolder = null,
		)
		canvas = CanvasMockBuilder().withDimension(Dimension2D(1000, 1000)).withView(view).build()
		every { canvas.addMouseListener(any()) } calls { mouseListeners.add(it.args[0] as MouseListener); Unit }
		every { canvas.removeMouseListener(any()) } calls { mouseListeners.remove(it.args[0] as MouseListener); Unit }
		every { canvas.addMouseMotionListener(any()) } calls { motionListeners.add(it.args[0] as MouseMotionListener); Unit }
		every { canvas.removeMouseMotionListener(any()) } calls { motionListeners.remove(it.args[0] as MouseMotionListener); Unit }
		every { canvas.addMouseWheelListener(any()) } calls { wheelListeners.add(it.args[0] as MouseWheelListener); Unit }
		every { canvas.removeMouseWheelListener(any()) } calls { wheelListeners.remove(it.args[0] as MouseWheelListener); Unit }
		every { canvas.addKeyListener(any()) } calls { keyListeners.add(it.args[0] as KeyListener); Unit }
		every { canvas.removeKeyListener(any()) } calls { keyListeners.remove(it.args[0] as KeyListener); Unit }
		System.timerFactory = { HoverTimer().also { timers.add(it) } }
		container = DrawableContainerImpl()
		drawable = TooltipDrawable()
		container.add(drawable)
		handler = newHandler()
	}

	@AfterTest
	fun after() {
		handlers.forEach { it.dispose() }
		TooltipManager.eventBus = previousEventBus
		System.timerFactory = previousTimerFactory
		if (::view.isInitialized) view.dispose()
	}

	@Test
	fun shouldResolveContentOnlyOnceAfterTheMouseRests() {
		hover()
		assertEquals(0, retrievals)
		assertEquals(0, drawable.contexts.size)
		assertEquals(0, drawable.explanationCalls)
		timer.advance(99)
		assertEquals(0, drawable.contexts.size)
		timer.advance(1)
		assertEquals(1, retrievals)
		assertEquals(1, drawable.contexts.size)
		assertEquals(1, drawable.explanationCalls)
		assertEquals(1, view.overlayContainer.drawables.size)
		timer.advance(100)
		assertEquals(1, drawable.contexts.size)
	}

	@Test
	fun shouldRestartTheFullDelayWithinTheSameDrawableAndUseTheLatestContext() {
		hover()
		timer.advance(75)
		val latest = InputEventContext(view, MouseEventImpl(modifiers = Modifier.Shift.mask), x = 25.0, y = 20.0, readonly = true)
		handler.handle(view, container, latest)
		timer.advance(99)
		assertEquals(0, drawable.contexts.size)
		timer.advance(1)
		assertSame(latest, drawable.contexts.single())
	}

	@Test
	fun shouldNotCreateContentWhilePassingOverDrawables() {
		val second = TooltipDrawable(Rectangle2D(200, 0, 100, 100))
		container.add(second)
		hover()
		timer.advance(75)
		hover(210.0)
		timer.advance(75)
		hover(500.0)
		timer.advance(100)
		assertTrue(drawable.contexts.isEmpty())
		assertTrue(second.contexts.isEmpty())
		assertTrue(view.overlayContainer.drawables.isEmpty())
	}

	@Test
	fun shouldGiveANewDrawableTheFullDelay() {
		val second = TooltipDrawable(Rectangle2D(200, 0, 100, 100))
		container.add(second)
		hover()
		timer.advance(75)
		hover(210.0)
		timer.advance(99)
		assertTrue(second.contexts.isEmpty())
		timer.advance(1)
		assertEquals(1, second.contexts.size)
		assertTrue(drawable.contexts.isEmpty())
	}

	@Test
	fun shouldRepeatHitTestingAfterTheDelay() {
		hover()
		container.remove(drawable)
		timer.advance(100)
		assertTrue(drawable.contexts.isEmpty())
		assertTrue(view.overlayContainer.drawables.isEmpty())
	}

	@Test
	fun shouldReadCurrentContentAtDisplayTime() {
		var value = "old"
		var resolved: String? = null
		val custom = TooltipHandler(eventBus, tooltipAccessor = { _, _ -> resolved = value; null }).also { handlers.add(it) }
		custom.handle(view, container, InputEventContext(view, x = 10.0, y = 20.0))
		value = "new"
		timer.advance(100)
		assertEquals("new", resolved)
	}

	@Test
	fun shouldCancelAndAllowTheSameTooltipAgain() {
		hover()
		handler.clear(view)
		timer.fire() // A callback queued before cancellation must not resolve content.
		assertTrue(drawable.contexts.isEmpty())
		assertTrue(mouseListeners.isEmpty())
		hover()
		timer.advance(100)
		assertEquals(1, view.overlayContainer.drawables.size)
		handler.clear(view)
		assertTrue(view.overlayContainer.drawables.isEmpty())
		hover()
		timer.advance(100)
		assertEquals(2, drawable.contexts.size)
		assertEquals(1, view.overlayContainer.drawables.size)
	}

	@Test
	fun shouldNotCancelAnotherHandlersPendingOrDisplayedTooltip() {
		val other = newHandler()
		hover()
		handler.clear(view)
		other.handle(view, container, InputEventContext(view, x = 10.0, y = 20.0))
		handler.dispose()
		timer.advance(100)
		assertEquals(1, view.overlayContainer.drawables.size)
		handler.clear(view)
		assertEquals(1, view.overlayContainer.drawables.size)
	}

	@Test
	fun shouldCancelOnDisposalAndIgnoreFurtherMoves() {
		hover()
		handler.dispose()
		timer.fire()
		hover()
		timer.advance(100)
		assertTrue(drawable.contexts.isEmpty())
		assertTrue(mouseListeners.isEmpty())
	}

	@Test
	fun shouldHideDisplayedContentAsSoonAsTheMouseMoves() {
		hover()
		timer.advance(100)
		hover(20.0)
		assertTrue(view.overlayContainer.drawables.isEmpty())
		timer.advance(100)
		assertEquals(2, drawable.contexts.size)
	}

	@Test
	fun shouldCancelPendingAndDisplayedContentOnViewTransformation() {
		hover()
		view.navigator.panBy(1, 0)
		timer.advance(100)
		assertTrue(drawable.contexts.isEmpty())
		hover()
		timer.advance(100)
		view.navigator.panBy(1, 0)
		assertTrue(view.overlayContainer.drawables.isEmpty())
		assertTrue(mouseListeners.isEmpty())
	}

	@Test
	fun shouldCancelOnMouseExitPressDragWheelAndKeyInput() {
		val event = MouseEventImpl()
		val cancellations: List<() -> Unit> = listOf(
			{ mouseListeners.toList().forEach { it.mouseExited(event) } },
			{ mouseListeners.toList().forEach { it.mousePressed(event) } },
			{ motionListeners.toList().forEach { it.mouseDragged(event) } },
			{ wheelListeners.toList().forEach { it.mouseWheelRotated(event) } },
			{ keyListeners.toList().forEach { it.keyPressed(KeyEventImpl(KeyEventType.PRESSED, key = KeyEvent.VK_SHIFT, keyChar = ' ')) } },
		)
		for (cancel in cancellations) {
			hover()
			cancel()
			timer.fire()
			assertTrue(drawable.contexts.isEmpty())
			assertTrue(mouseListeners.isEmpty())
			assertTrue(motionListeners.isEmpty())
			assertTrue(wheelListeners.isEmpty())
			assertTrue(keyListeners.isEmpty())
		}
	}

	@Test
	fun shouldCancelOnPopupOpening() {
		hover()
		eventBus.post(PopupMenuEvent(canvas))
		timer.fire()
		assertTrue(drawable.contexts.isEmpty())
	}

	@Test
	fun shouldDisplayAndDismissAnExplanationWithoutText() {
		drawable.text = null
		drawable.explanation = DrawableExplanation(
			RichTextDrawable.of("Explanation", DrawStyleModule.styleProvider.getStyle(StyleType.TOOLTIP).font),
			Rectangle2D(10, 20, 20, 20),
		)
		hover()
		timer.advance(100)
		assertEquals(1, view.overlayContainer.drawables.size)
		view.navigator.panBy(1, 0)
		assertTrue(view.overlayContainer.drawables.isEmpty())
	}

	@Test
	fun shouldHonorTooltipAndBeginnerHelpPreferencesAtResolutionTime() {
		hover()
		BaseModule.properties.set(PROP_BEGINNER_HELP_TOOLTIP, false)
		timer.advance(100)
		assertEquals(0, drawable.explanationCalls)
		hover()
		BaseModule.properties.set(TooltipHandler.PROP_TOOLTIPS_ENABLED, false)
		timer.advance(100)
		assertEquals(1, drawable.contexts.size)
		hover()
		assertFalse(timer.isRunning())
	}

	@Test
	fun shouldStopAnOldTimerWhenPreferencesChange() {
		hover()
		val oldTimer = timer
		BaseModule.properties.set(TooltipManager.PROP_DELAY, 200)
		eventBus.post(PreferencesChangedEvent(BaseModule.properties))
		assertFalse(oldTimer.isRunning())
		hover()
		oldTimer.fire()
		assertTrue(drawable.contexts.isEmpty())
		timer.advance(199)
		assertTrue(drawable.contexts.isEmpty())
		timer.advance(1)
		assertEquals(1, drawable.contexts.size)
	}

	private fun newHandler(): TooltipHandler = TooltipHandler(eventBus, drawableRetriever = { c, x, y ->
		retrievals++
		c.getDrawableAt(x, y)
	}).also { handlers.add(it) }

	private fun hover(x: Double = 10.0) {
		handler.handle(view, container, InputEventContext(view, x = x, y = 20.0))
	}

	private class TooltipDrawable(override val boundingBox: Rectangle2D = Rectangle2D(0, 0, 100, 100)) : AbstractDrawable() {
		val contexts = mutableListOf<InputEventContext>()
		var explanationCalls = 0
		var text: String? = "Tooltip"
		var explanation: DrawableExplanation<RectangularDrawable>? = null

		override fun contains(x: Double, y: Double): Boolean = boundingBox.contains(x, y)
		override fun draw(context: DrawContext) { }

		override fun <T : InputEventContext> getTooltip(context: T): Tooltip? {
			contexts.add(context)
			return text?.let { Tooltip(it, context.x, context.y) }
		}

		override fun getExplanation(x: Double, y: Double): DrawableExplanation<RectangularDrawable>? {
			explanationCalls++
			return explanation
		}
	}

	/** A one-shot timer whose start, like the production timers, does not restart a running timer. */
	private class HoverTimer : Timer {
		override var interval = 0
		private var running = false
		private var remaining = 0
		private lateinit var handler: TimerHandler

		override fun initialize(interval: Int, repeats: Boolean, handler: TimerHandler): Timer {
			check(!repeats)
			this.interval = interval
			this.handler = handler
			return this
		}

		override fun start() {
			if (running) return
			running = true
			remaining = interval
		}

		override fun stop() { running = false }
		override fun isRunning(): Boolean = running

		fun advance(millis: Int) {
			if (!running) return
			remaining -= millis
			if (remaining <= 0) {
				running = false
				fire()
			}
		}

		fun fire() = handler(ActionEvent(null, this, 0, "timer", 0))
	}
}
