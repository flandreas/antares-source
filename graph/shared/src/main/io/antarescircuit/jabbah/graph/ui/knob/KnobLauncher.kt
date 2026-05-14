package io.antarescircuit.jabbah.graph.ui.knob

import io.antarescircuit.jabbah.base.*
import io.antarescircuit.jabbah.base.event.*
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.time.Timer
import io.antarescircuit.jabbah.draw.*
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.draw.view.TooltipManager
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.execution.scheduler.SchedulerActivationStateEvent

interface KnobLauncher {
	/**
	 * Launches a new [KnobView] after the mouse pointer has stayed within the client component for the
	 * configured delay time.
	 *
	 * @param initialValue the initial value to be displayed by the [KnobView]
	 * @param location the location in global model space where the center of the [KnobView] should be located
	 * @param mouseMovedCondition the condition that must be `true` during the entire delay time. This is
	 * typically implemented as [Drawable.contains] regarding the client that requests the [KnobView].
	 * @param displayHandler the additional code to be executed when the [KnobView] is displayed.
	 * Allows the client to reset any of its state, e.g. hover highlighting over a button that initiates launching
	 * @param valueChangeHandler called by this [KnobLauncherImpl] whenever the [KnobView]'s value has changed
	 * @param signalHandler the [SignalHandler] in whose context the [KnobView] is launched. If set, [KnobLauncher]
	 * listens for deactivations of the [Scheduler], which result in hiding the [KnobView]
	 */
	fun launchAfterDelay(
		initialValue: MagnitudeValue,
		location: Point2D,
		mouseMovedCondition: (ActorInteractionContext) -> Boolean,
		displayHandler: () -> Unit = {},
		valueChangeHandler: (MagnitudeValue) -> Unit,
		signalHandler: SignalHandler? = null
	): ActorInteractionHandler

	fun launchImmediately(
		view: DrawingView<*,*>,
		initialValue: MagnitudeValue,
		location: Point2D,
		mouseMovedCondition: (ActorInteractionContext) -> Boolean,
		displayHandler: () -> Unit = {},
		valueChangeHandler: (MagnitudeValue) -> Unit,
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

	private var drawingView: DrawingView<*,*>? = null
	private lateinit var initialValue: MagnitudeValue
	private var location: Point2D = Point2D.ZERO
	private var mouseMovedCondition: ((ActorInteractionContext) -> Boolean)? = null
	private var displayHandler: (() -> Unit)? = null
	private var valueChangeHandler: ((MagnitudeValue) -> Unit)? = null
	private var signalHandler: SignalHandler? = null
	private var oldStatus: String? = null

	private val activationStateHandler: EventHandler<SchedulerActivationStateEvent> = {
		if (it.scheduler === signalHandler && !it.scheduler.isActive && drawingView	!= null) {
			hide()
		}
	}

	override fun launchAfterDelay(
		initialValue: MagnitudeValue,
		location: Point2D,
		mouseMovedCondition: (ActorInteractionContext) -> Boolean,
		displayHandler: () -> Unit,
		valueChangeHandler: (MagnitudeValue) -> Unit,
		signalHandler: SignalHandler?
	): ActorInteractionHandler {
		this.initialValue = initialValue
		this.location = location
		this.mouseMovedCondition = mouseMovedCondition
		this.displayHandler = displayHandler
		this.valueChangeHandler = valueChangeHandler
		this.signalHandler = signalHandler

		return handler
	}

	override fun launchImmediately(
		view: DrawingView<*,*>,
		initialValue: MagnitudeValue,
		location: Point2D,
		mouseMovedCondition: (ActorInteractionContext) -> Boolean,
		displayHandler: () -> Unit,
		valueChangeHandler: (MagnitudeValue) -> Unit,
		signalHandler: SignalHandler?
	): ActorInteractionHandler {
		this.initialValue = initialValue
		this.location = location
		this.mouseMovedCondition = mouseMovedCondition
		this.displayHandler = displayHandler
		this.valueChangeHandler = valueChangeHandler
		this.signalHandler = signalHandler

		display(view)

		return handler
	}

	private fun startTimerIfNeeded(view: DrawingView<*,*>) {
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

	private fun display(view: DrawingView<*,*>) {
		stopTimer()

		knobView.valueChangeHandler = { valueChangeHandler!!.invoke(it) }
		knobView.location = location
		knobView.value = initialValue
		knobView.defaultValue = initialValue

		signalHandler?.eventBus?.register(SchedulerActivationStateEvent::class, activationStateHandler)
		drawingView = view

		LOG.trace("show KnobView")
		knobView.zoomPan = view.zoomPan
		view.content.ghostContainer.add(knobView)
		view.content.ghostContainer.validate()
		view.setCursor(Cursor.CLICK)

		oldStatus = Status.replace(StatusType.Tool, Translations.getString("graph.knob.toolStatus"))

		displayHandler?.invoke()
	}

	override fun hide() {
		drawingView?.apply {
			content.ghostContainer.remove(knobView)
			content.ghostContainer.validate()
		}
		signalHandler?.eventBus?.unregister(activationStateHandler)

		Status.set(StatusType.Tool, oldStatus)
	}

	private class Handler : InputEventHandlerAdapter<ActorInteractionContext>() {
		override fun mouseMoved(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
			val view = context.view as DrawingView<*,*>

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


