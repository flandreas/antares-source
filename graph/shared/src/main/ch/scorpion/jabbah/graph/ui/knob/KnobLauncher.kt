package ch.scorpion.jabbah.graph.ui.knob

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.view.TooltipManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent

interface KnobLauncher {
	/**
	 * Launches a new [KnobView] after the mouse pointer has stayed within the client component for the
	 * configured delay time.
	 *
	 * @param initialValue the initial value to be displayed by the [KnobView]
	 * @param location the location in global model space where the center of the [KnobView] should be located
	 * @param unit the [String] displayed after the value in [KnobView]. Example: µs
	 * @param mouseMovedCondition the condition that must be `true` during the entire delay time. This is
	 * typically implemented as [Drawable.contains] regarding the client that requests the [KnobView].
	 * @param displayHandler the additional code to be executed when the [KnobView] is displayed.
	 * Allows the client to reset any of its state, e.g. hover highlighting over a button that initiates launching
	 * @param valueChangeHandler called by this [KnobLauncherImpl] whenever the [KnobView]'s value has changed
	 * @param signalHandler the [SignalHandler] in whose context the [KnobView] is launched. If set, [KnobLauncher]
	 * listens for deactivations of the [Scheduler], which result in hiding the [KnobView]
	 */
	fun launchAfterDelay(
		initialValue: Long,
		location: Point2D,
		unit: String,
		mouseMovedCondition: (ActorInteractionContext) -> Boolean,
		displayHandler: () -> Unit = {},
		valueChangeHandler: (Long) -> Unit,
		signalHandler: SignalHandler? = null
	): ActorInteractionHandler

	fun launchImmediately(
		view: DrawingView<*>,
		initialValue: Long,
		location: Point2D,
		unit: String,
		mouseMovedCondition: (ActorInteractionContext) -> Boolean,
		displayHandler: () -> Unit = {},
		valueChangeHandler: (Long) -> Unit,
		signalHandler: SignalHandler? = null
	): ActorInteractionHandler

	fun hide()
}

/**
 * Utility class for launching a [KnobView] for a client component after the mouse pointer has stayed
 * within the client component during a particular delay time, similar to tooltip delays.
 */
object KnobLauncherImpl : KnobLauncher {

	private val LOG by logger(KnobLauncherImpl::class)

	/** The factor of [TooltipManager.PROP_DELAY] for delaying displaying the [KnobView].*/
	private const val DELAY_FACTOR = 0.7

	private val timerDelay: Int get() = (DELAY_FACTOR * BaseModule.properties.getInt(TooltipManager.PROP_DELAY)).toInt()
	private val handler = Handler()
	private var timer: Timer? = null
	private val knobView: KnobView by lazy { KnobView() }

	private var drawingView: DrawingView<*>? = null
	private var initialValue: Long = 0
	private var location: Point2D = Point2D.ZERO
	private var unit: String = ""
	private var mouseMovedCondition: ((ActorInteractionContext) -> Boolean)? = null
	private var displayHandler: (() -> Unit)? = null
	private var valueChangeHandler: ((Long) -> Unit)? = null
	private var signalHandler: SignalHandler? = null

	private val activationStateHandler: EventHandler<SchedulerActivationStateEvent> = {
		if (it.scheduler === signalHandler && !it.scheduler.isActive && drawingView	!= null) {
			hide()
		}
	}

	override fun launchAfterDelay(
		initialValue: Long,
		location: Point2D,
		unit: String,
		mouseMovedCondition: (ActorInteractionContext) -> Boolean,
		displayHandler: () -> Unit,
		valueChangeHandler: (Long) -> Unit,
		signalHandler: SignalHandler?
	): ActorInteractionHandler {
		this.initialValue = initialValue
		this.location = location
		this.unit = unit
		this.mouseMovedCondition = mouseMovedCondition
		this.displayHandler = displayHandler
		this.valueChangeHandler = valueChangeHandler
		this.signalHandler = signalHandler

		return handler
	}

	override fun launchImmediately(
		view: DrawingView<*>,
		initialValue: Long,
		location: Point2D,
		unit: String,
		mouseMovedCondition: (ActorInteractionContext) -> Boolean,
		displayHandler: () -> Unit,
		valueChangeHandler: (Long) -> Unit,
		signalHandler: SignalHandler?
	): ActorInteractionHandler {
		this.initialValue = initialValue
		this.location = location
		this.unit = unit
		this.mouseMovedCondition = mouseMovedCondition
		this.displayHandler = displayHandler
		this.valueChangeHandler = valueChangeHandler
		this.signalHandler = signalHandler

		display(view)

		return handler
	}

	private fun startTimerIfNeeded(view: DrawingView<*>) {
		if (timer == null) {
			LOG.trace("starting KnobView timer")
			drawingView = view
			timer = System.createTimer()
			timer!!.initialize(timerDelay) { display(view) }
			timer!!.start()
		}
	}

	private fun stopTimer() {
		timer?.let {
			it.stop()
			timer = null
		}
	}

	private fun display(view: DrawingView<*>) {
		stopTimer()

		knobView.valueChangeHandler = { valueChangeHandler!!.invoke(it) }
		knobView.location = location
		knobView.value = initialValue
		knobView.defaultValue = initialValue
		knobView.unit = unit

		signalHandler?.eventBus?.register(SchedulerActivationStateEvent::class, activationStateHandler)
		drawingView = view

		LOG.trace("show KnobView")
		knobView.zoomPan = view.zoomPan
		view.content.ghostContainer.add(knobView)
		view.content.ghostContainer.validate()
		view.setCursor(Cursor.CLICK)

		displayHandler?.invoke()
	}

	override fun hide() {
		drawingView?.apply {
			content.ghostContainer.remove(knobView)
			content.ghostContainer.validate()
		}
		signalHandler?.eventBus?.unregister(activationStateHandler)
	}

	private class Handler : InputEventHandlerAdapter<ActorInteractionContext>() {
		override fun mouseMoved(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
			val view = context.view as DrawingView<*>

			if (view.content.ghostContainer.contains(knobView)) {
				return null
			}

			if (mouseMovedCondition!!.invoke(context)) {
				startTimerIfNeeded(view)
				return this
			}

			stopTimer()
			return null
		}
	}
}


