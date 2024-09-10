package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.geom.Geometry
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.math.TWO_PI
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.container.UnzoomableContainer
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.drawable.AbstractRectangularUnzoomable
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.graphics.Graphics2D
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.draw.view.TooltipManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import kotlin.math.*

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
	 */
	fun launchAfterDelay(
		initialValue: Long,
		location: Point2D,
		unit: String,
		mouseMovedCondition: (ActorInteractionContext) -> Boolean,
		displayHandler: () -> Unit = {},
		valueChangeHandler: (Long) -> Unit
	): ActorInteractionHandler

	fun launchImmediately(
		view: DrawingView<*>,
		initialValue: Long,
		location: Point2D,
		unit: String,
		mouseMovedCondition: (ActorInteractionContext) -> Boolean,
		displayHandler: () -> Unit = {},
		valueChangeHandler: (Long) -> Unit
	): ActorInteractionHandler
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

	private var initialValue: Long = 0
	private var location: Point2D = Point2D.ZERO
	private var unit: String = ""
	private var mouseMovedCondition: ((ActorInteractionContext) -> Boolean)? = null
	private var displayHandler: (() -> Unit)? = null
	private var valueChangeHandler: ((Long) -> Unit)? = null

	override fun launchAfterDelay(
		initialValue: Long,
		location: Point2D,
		unit: String,
		mouseMovedCondition: (ActorInteractionContext) -> Boolean,
		displayHandler: () -> Unit,
		valueChangeHandler: (Long) -> Unit
	): ActorInteractionHandler {
		this.initialValue = initialValue
		this.location = location
		this.unit = unit
		this.mouseMovedCondition = mouseMovedCondition
		this.displayHandler = displayHandler
		this.valueChangeHandler = valueChangeHandler

		return handler
	}

	override fun launchImmediately(
		view: DrawingView<*>,
		initialValue: Long,
		location: Point2D,
		unit: String,
		mouseMovedCondition: (ActorInteractionContext) -> Boolean,
		displayHandler: () -> Unit,
		valueChangeHandler: (Long) -> Unit
	): ActorInteractionHandler {
		this.initialValue = initialValue
		this.location = location
		this.unit = unit
		this.mouseMovedCondition = mouseMovedCondition
		this.displayHandler = displayHandler
		this.valueChangeHandler = valueChangeHandler

		display(view)

		return handler
	}

	private fun startTimerIfNeeded(view: DrawingView<*>) {
		if (timer == null) {
			LOG.trace("starting KnobView timer")
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

		LOG.trace("show KnobView")
		knobView.zoomPan = view.zoomPan
		view.content.ghostContainer.add(knobView)
		view.content.ghostContainer.validate()
		view.setCursor(Cursor.CLICK)

		displayHandler?.invoke()
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

/**
 * A circular knob used for interactively changing a value while execution.
 *
 * @property model the [KnobModel] displayed by this [KnobModel]
 * @property unit the description of the unit to be displayed after the value
 * @property valueChangeHandler the logic to be executed when the value of the [KnobModel] has changed
 * @param location the location of the center of this [KnobView] in absolute view coordinates
 */
class KnobView(
	private val model: KnobModel = KnobModel(initialValue = 0),
	var unit: String = "",
	location: Point2D = Point2D.ZERO,
	var valueChangeHandler: (Long) -> Unit = {}
): AbstractRectangularUnzoomable(OUTER_SIZE / 2, location), ActorView {

	companion object {

		const val OUTER_SIZE = 120.0
		private val OUTER_COLOR = Color(196, 196, 196, 228)

		private const val SCALE_SIZE = OUTER_SIZE - 20

		private const val INNER_SIZE = OUTER_SIZE - 50
		private val INNER_COLOR = Color(32, 32, 32, 128)

		private const val TRIANGLE_SIZE = 10
		private val TRIANGLE_PATH = System.createPath()
			.moveTo(0, 0)
			.lineTo(-TRIANGLE_SIZE, TRIANGLE_SIZE / 2)
			.lineTo(-TRIANGLE_SIZE, -TRIANGLE_SIZE / 2)
			.close()

		/** The angle (in radians and in terms of Math, i.e. anti-clockwise) at which the 1 digit is drawn in the scale.*/
		private const val ONE_ANGLE = PI / 2

		/** The angle (in radians) between two subsequent scale digits.*/
		private const val ANGLE_PER_DIGIT = 2 * PI / 9
	}

	var value: Long
		get() = model.value
		set(value) {
			model.value = value
		}

	/**
	 * The value to be set in [value] when the user double-clicks this [KnobView].
	 * Initialized with the [KnobModel]'s initial value upon construction.
	 */
	var defaultValue: Long = model.value

	private val handler = Handler()

	/** The current angle (angle) in radians, expressed in terms of [Graphics2D], i.e. clockwise.*/
	private val angle: Double get() = -(ONE_ANGLE - model.asAngle)

	/** Used as a stamp to draw the scale numbers.*/
	private val scaleLabel = Label(font = Themes.get<GraphTheme>().explanation.font, text = "")

	/** Used to draw the current value in the center of the knob.*/
	private val valueLabel = Label(font = Themes.get<GraphTheme>().explanation.font, text = "", color = Color.WHITE)

	init {
		model.addPropertyChangeListener(object : PropertyChangeListener<Long> {
			override fun propertyChanged(e: PropertyChangeEvent<Long>) {
				invalidate()
				validate()
				valueChangeHandler.invoke(value)
			}
		})
	}

	/** ---- [AbstractRectangle] */

	override val lineWidth: Double get() = 1.0

	override fun draw(context: DrawContext) {
		val viewRectangle = getViewRectangle()

		context.g.translate(viewRectangle.center)

		context.g.color = OUTER_COLOR
		context.g.fillOval(-width / 2, -height / 2, width, height)

		context.g.color = INNER_COLOR
		drawScale(context)

		context.g.fillOval(-INNER_SIZE / 2, -INNER_SIZE / 2, INNER_SIZE, INNER_SIZE)

		val currAngle = angle

		context.g.rotate(currAngle)
		context.translated(INNER_SIZE / 2 + TRIANGLE_SIZE, 0.0) { it.g.fill(TRIANGLE_PATH) }
		context.g.rotate(-currAngle)

		var text = Thousands.convert(model.value)
		if (StringUtils.isNotEmpty(unit)) {
			text = "$text $unit"
		}
		valueLabel.text = text
		valueLabel.draw(context)

		context.g.translate(viewRectangle.center.negate)
	}

	private fun drawScale(context: DrawContext) {
		for (number in 1..9) {
			val angle = ONE_ANGLE - (number - 1) * ANGLE_PER_DIGIT
			scaleLabel.location = Point2D(
				SCALE_SIZE / 2 * cos(angle),
				-SCALE_SIZE / 2 * sin(angle))
			scaleLabel.text = number.toString()
			scaleLabel.draw(context)
		}
	}

	override fun contains(x: Double, y: Double): Boolean =
		boundingBox.center.distance(x, y) <= OUTER_SIZE / 2 / zoomPan!!.zoomFactor

	/** ---- [ActorView] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler = handler

	override fun getExecutionTooltip(x: Double, y: Double): Tooltip? = null

	override fun executionStarted(signalHandler: SignalHandler) { }

	override fun executionStopped(signalHandler: SignalHandler) { }

	private fun removeFromParent(container: UnzoomableContainer<Unzoomable>) {
		container.remove(this)
		container.validate()
	}

	/** Controls popup and rotation of [KnobView]. */
	private inner class Handler : InputEventHandlerAdapter<ActorInteractionContext>() {

		private var pressedModelAngle: Double = 0.0
		private var pressedAngle: Double = 0.0
		private var oldAngle: Double = 0.0

		override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? {
			if (!this@KnobView.contains(context.x, context.y)) {
				removeFromParent((context.view as DrawingView<*>).content.ghostContainer)
				return null
			}
			return this
		}

		override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? {
			if (context.mouseEvent?.button != Button.BUTTON1) {
				return this
			}
			if (context.mouseEvent?.clickCount == 2) {
				value = defaultValue
				return this
			}

			pressedModelAngle = model.asAngle
			oldAngle = Geometry.angle(boundingBox.center, Point2D(context.x, context.y))
			pressedAngle = Geometry.angle(boundingBox.center, Point2D(context.x, context.y))

			return this
		}

		override fun mouseDragged(context: ActorInteractionContext): ActorInteractionHandler? {
			val newAngle = Geometry.angle(boundingBox.center, Point2D(context.x, context.y))
			if (newAngle != oldAngle) {
				val oldValue = value
				val newValue = model.changeToAngle(
					newAngle = Geometry.wrapAngle(pressedModelAngle - (newAngle - pressedAngle)),
					increment = Geometry.isClockwiseAngleChange(oldAngle, newAngle))
				if (newValue != oldValue) {
					oldAngle = newAngle
				}
			}

			return this
		}

		override fun mouseReleased(context: ActorInteractionContext): ActorInteractionHandler? {
			if (!this@KnobView.contains(context.x, context.y)) {
				removeFromParent((context.view as DrawingView<*>).content.ghostContainer)
				return null
			}
			return this
		}
	}
}

/**
 * A model of a 'knob' that can be turned by a particular angle in order to increase or decrease the
 * [KnobModel]'s [Long] value.
 *
 * @param initialValue the initial value of this [KnobModel], defaults to zero.
 */
class KnobModel(
	initialValue: Long = 0,
	private val propertyOwner: PropertyOwner<Long> = PropertyOwnerImpl()
) : PropertyOwner<Long> by propertyOwner {

	companion object {
		const val PROP_VALUE = "value"
	}

	init {
		propertyOwner.source = this
	}

	/** Returns the current value of this [KnobModel] as an angle (in radians, zero east, anti-clockwise).*/
	val asAngle: Double get() = asAngle(value)

	/**
	 * Contains the current value of ths [KnobModel]. Changing this value results in sending a
	 * [PropertyChangeEvent] to all registered [PropertyChangeListener]s.
	 */
	var value: Long = initialValue
		set(value) {
			val effectiveNewValue = max(1L, value)
			if (field != effectiveNewValue) {
				val oldValue = field
				field = effectiveNewValue
				propertyOwner.fire(PROP_VALUE, oldValue, field)
			}
		}

	/**
	 * The current value with all digits except the most significant digits set to zero.
	 * Example: The base value of 12_345 is 10_000.
	 */
	private val baseValue: Double get() = 10.0.pow(log10(value.toDouble()).toLong().toDouble())

	fun incrementAngleTo(newAngle: Double): Long {
		var currentBaseValue = baseValue
		val factor = newAngle / TWO_PI
		var newValue = (currentBaseValue + 9 * currentBaseValue * factor).toLong()
		if (newValue != value) {
			if (newValue < value) {
				currentBaseValue *= 10
				newValue = (currentBaseValue + 9 * currentBaseValue * factor).toLong()
			}
			value = newValue
		}
		return value
	}

	private fun decrementAngleTo(newAngle: Double): Long {
		var currentBaseValue = baseValue
		val factor = newAngle / TWO_PI
		var newValue = (currentBaseValue + 9 * currentBaseValue * factor).toLong()
		if (newValue != value) {
			if (newValue > value) {
				currentBaseValue /= 10
				newValue = (currentBaseValue + 9 * currentBaseValue * factor).toLong()
			}
			value = newValue
		}
		return value
	}

	/**
	 * @return the new value of this [KnobModel] for convenience
	 */
	fun changeToAngle(newAngle: Double, increment: Boolean): Long =
		if (increment) {
			incrementAngleTo(newAngle)
		} else decrementAngleTo(newAngle)

	/** Returns the specified value of this [KnobModel] as an angle (in radians, zero east, anti-clockwise).*/
	private fun asAngle(a: Long): Double {
		val diffValue = a - baseValue
		return if (diffValue == 0.0) 0.0 else TWO_PI * diffValue / (9 * baseValue)
	}
}