package io.antarescircuit.jabbah.graph.ui.knob

import io.antarescircuit.jabbah.base.*
import io.antarescircuit.jabbah.base.event.Button
import io.antarescircuit.jabbah.base.geom.Geometry
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.draw.InputEventHandlerAdapter
import io.antarescircuit.jabbah.draw.drawable.AbstractRectangle
import io.antarescircuit.jabbah.draw.drawable.AbstractRectangularUnzoomable
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.execution.actor.ActorView
import io.antarescircuit.jabbah.graph.ui.knob.KnobView.Companion.TRIANGLE_PATH
import io.antarescircuit.jabbah.graph.view.style.GraphTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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

        // Points to the east, origin at arrow tip at the east
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

    /**
     * The angle at which the [TRIANGLE_PATH] has to be rotated in terms of [Graphics2D to point to the current value.
     */
    private val triangleAngle: Double get() = -(ONE_ANGLE - model.asAngle)

    /** Used as a stamp to draw the scale numbers.*/
    private val scaleLabel = Label(font = Themes.get<GraphTheme>().explanation.font, text = "")

    /** Used to draw the current value in the center of the knob.*/
    private val valueLabel = Label(font = Themes.get<GraphTheme>().explanation.font, text = "", color = Color.WHITE)

    init {
        model.addPropertyChangeListener {
            invalidate()
            validate()
            valueChangeHandler.invoke(value)
        }
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

        context.rotatedAndTranslated(INNER_SIZE / 2 + TRIANGLE_SIZE, 0.0, triangleAngle) {
            it.g.fill(TRIANGLE_PATH)
        }

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
                -SCALE_SIZE / 2 * sin(angle)
            )
            scaleLabel.text = number.toString()
            scaleLabel.draw(context)
        }
    }

    override fun contains(x: Double, y: Double): Boolean =
        boundingBox.center.distance(x, y) <= OUTER_SIZE / 2 / zoomPan!!.zoomFactor * zoomPan!!.devicePixelRatio()

    private fun isWithinScale(p: Point2D): Boolean =
        boundingBox.center.distance(p) >= INNER_SIZE / 2 / zoomPan!!.zoomFactor * zoomPan!!.devicePixelRatio()

    /** ---- [ActorView] */

    override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler = handler

    override fun <T: InputEventContext> getExecutionTooltip(context: T): Tooltip? = null

    override fun executionStarted(signalHandler: SignalHandler) { }

    override fun executionStopped(signalHandler: SignalHandler) { }

    /** Controls popup and rotation of [KnobView]. */
    private inner class Handler : InputEventHandlerAdapter<ActorInteractionContext>() {

        private var pressedModelAngle: Double = 0.0
        private var pressedAngle: Double = 0.0
        private var oldAngle: Double = 0.0

        override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? {
            if (!this@KnobView.contains(context.x, context.y)) {
                KnobLauncherImpl.hide()
                return null
            }
            return this
        }

        override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler {
            if (context.mouseEvent?.button != Button.BUTTON1) {
                return this
            }

            pressedModelAngle = model.asAngle
            oldAngle = Geometry.angle(boundingBox.center, Point2D(context.x, context.y))
            pressedAngle = Geometry.angle(boundingBox.center, context.location)

            return this
        }

        override fun mouseDragged(context: ActorInteractionContext): ActorInteractionHandler {
            val newAngle = Geometry.angle(boundingBox.center, context.location)
            if (newAngle != oldAngle) {
                val oldValue = value
                val newValue = model.dragToAngle(
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
                KnobLauncherImpl.hide()
                return null
            }
            return this
        }

        override fun mouseClicked(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
            if (context.mouseEvent?.button != Button.BUTTON1) {
                return null
            }

            // Double-click to reset to default (on center knob)
            if (context.mouseEvent?.clickCount == 2 && boundingBox.center.distance(context.location) < INNER_SIZE / 2) {
                value = defaultValue
                return null
            }

            // Single-click to set angle (on outer scale)
            if (context.mouseEvent?.clickCount == 1 && isWithinScale(context.location)) {
                // Origin east, counter-clockwise (like in math)
                val newAngle = Geometry.angle(boundingBox.center, context.location)

                // Origin north, clockwise (like the KnobView scale)
                val wrappedNewAngle = Geometry.wrapAngle(-newAngle + PI / 2)

                model.clickToAngle(wrappedNewAngle)
            }

            return null
        }
    }
}
