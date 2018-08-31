package ch.scorpion.antares.view.input

import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.event.PropertyOwner
import ch.scorpion.jabbah.base.geom.Geometry
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Graphics2D
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandlerAdapter
import ch.scorpion.jabbah.execution.actor.ActorView

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
	private val unit: String = "",
	private val valueChangeHandler: (Long) -> Unit = {},
	location: Point2D = Point2D()
) : AbstractRectangle(location.x - OUTER_SIZE / 2, location.y - OUTER_SIZE / 2, OUTER_SIZE, OUTER_SIZE), ActorView {

	companion object {

		const val OUTER_SIZE = 120.0
		private val OUTER_COLOR = Color(196, 196, 196, 228)

		private const val SCALE_SIZE = OUTER_SIZE - 20

		private const val INNER_SIZE = OUTER_SIZE - 50
		private val INNER_COLOR = Color(32, 32, 32, 128)

		private const val TRIANGLE_SIZE = 10
		private val TRIANGLE_PATH = System.get().createPath()
			.moveTo(0, 0)
			.lineTo(-TRIANGLE_SIZE, TRIANGLE_SIZE / 2)
			.lineTo(-TRIANGLE_SIZE, -TRIANGLE_SIZE / 2)
			.close()

		/** The angle (in radians and in terms of Math, i.e. anti-clockwise) at which the 1 digit is drawn in the scale.*/
		private const val ONE_ANGLE = MathClass.PI / 2

		/** The angle (in radians) between two subsequent scale digits.*/
		private const val ANGLE_PER_DIGIT = 2 * MathClass.PI / 9
	}

	var value: Long
		get() = model.value
		set(value) { model.value = value }

	private val handler = Handler()

	/** The current angle (angle) in radians, expressed in terms of [Graphics2D], i.e. clockwise.*/
	private val angle: Double get() = -(ONE_ANGLE - model.asAngle)

	/** Used as a stamp to draw the scale numbers.*/
	private val scaleLabel = Label(font = Themes.get<AntaresTheme>().explanation.font, text= "")

	/** Used to draw the current value in the center of the knob.*/
	private val valueLabel = Label(font = Themes.get<AntaresTheme>().explanation.font, text= "", color = Color.WHITE)

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
		context.g.translate(x + width / 2, y + height / 2)

		context.g.color = OUTER_COLOR
		context.g.fillOval(-width / 2, -height / 2, width, height)

		context.g.color = INNER_COLOR
		drawScale(context)

		context.g.fillOval(-INNER_SIZE / 2, -INNER_SIZE / 2, INNER_SIZE, INNER_SIZE)

		val currAngle = angle

		context.g.rotate(currAngle)
		context.g.translate(INNER_SIZE / 2 + TRIANGLE_SIZE, 0.0)
		context.g.fill(TRIANGLE_PATH)
		context.g.translate(-(INNER_SIZE / 2 + TRIANGLE_SIZE), 0.0)
		context.g.rotate(-currAngle)

		var text = Thousands.convert(model.value)
		if (StringUtils.isNotEmpty(unit)) {
			text = "$text $unit"
		}
		valueLabel.text = text
		valueLabel.draw(context)

		context.g.translate(-(x + width / 2), -(y + height / 2))
	}

	private fun drawScale(context: DrawContext) {
		for (number in 1..9) {
			val angle = ONE_ANGLE - (number - 1) * ANGLE_PER_DIGIT
			scaleLabel.location = Point2D(
				SCALE_SIZE / 2 * Math.cos(angle),
				-SCALE_SIZE / 2 * Math.sin(angle))
			scaleLabel.text = number.toString()
			scaleLabel.draw(context)
		}
	}

	override fun contains(x: Double, y: Double): Boolean {
		return boundingBox.center.distance(x, y) <= OUTER_SIZE / 2
	}

	/** ---- [ActorView] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler? {
		return handler
	}

	override fun getExecutionTooltip(x: Double, y: Double): Tooltip? {
		return null
	}

	private fun removeFromParent(container: DrawableContainer<Drawable>) {
		container.remove(this)
		container.validate()
	}

	/** Controls popup and rotation of [KnobView]. */
	private inner class Handler : ActorInteractionHandlerAdapter() {

		private var pressedModelAngle: Double = 0.0
		private var pressedAngle: Double = 0.0
		private var oldAngle: Double = 0.0

		override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? {
			if (!this@KnobView.contains(context.x, context.y)) {
				removeFromParent((context.view as DrawingView<*>).content.animationContainer)
				return null
			}
			return this
		}

		override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? {
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
				removeFromParent((context.view as DrawingView<*>).content.animationContainer)
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
class KnobModel(initialValue: Long = 0) : PropertyOwner<Long>() {

	companion object {
		const val PROP_VALUE = "value"
	}

	/** Returns the current value of this [KnobModel] as an angle (in radians, zero east, anti-clockwise).*/
	val asAngle: Double get() = asAngle(value)

	/**
	 * Contains the current value of ths [KnobModel]. Changing this value results in sending a
	 * [PropertyChangeEvent] to all registered [PropertyChangeListener]s.
	 */
	var value: Long = initialValue
		set(value) {
			val effectiveNewValue = Math.max(1L, value)
			if (field != effectiveNewValue) {
				val oldValue = field
				field = effectiveNewValue
				pcSupport.fire(PROP_VALUE, oldValue, field)
			}
		}

	/**
	 * The current value with all digits except the most significant digits set to zero.
	 * Example: The base value of 12_345 is 10_000.
	 */
	private val baseValue: Double get() = Math.power(10.0, Math.log10(value.toDouble()).toLong().toDouble())

	fun incrementAngleTo(newAngle: Double): Long {
		var currentBaseValue = baseValue
		val factor = newAngle / MathClass.TWO_PI
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
		val factor = newAngle / MathClass.TWO_PI
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
	fun changeToAngle(newAngle: Double, increment: Boolean): Long {
		return if (increment) {
			incrementAngleTo(newAngle)
		} else decrementAngleTo(newAngle)
	}

	/** Returns the specified value of this [KnobModel] as an angle (in radians, zero east, anti-clockwise).*/
	private fun asAngle(a: Long): Double {
		val diffValue = a - baseValue
		return if (diffValue == 0.0) 0.0 else MathClass.TWO_PI * diffValue / (9 * baseValue)
	}
}