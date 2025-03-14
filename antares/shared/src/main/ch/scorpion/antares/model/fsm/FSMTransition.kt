package ch.scorpion.antares.model.fsm

import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.math.PI_6
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.polyline.ArrowHead
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.Labeled
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.edit.model.text.description.observableDescription
import ch.scorpion.jabbah.io.Reference
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * An [FSMTransition] is a transition between two [FSMStates][FSMState], leading from the "origin" state
 * to the "destination" state.
 * Uses a quadratic Bézier curve to draw its shape. If the transition is to itself, it uses a cubic Bézier curve.
 *
 * @param cubicAngle the angle of placement in case this [FSMTransition] is a self-transition
 */
class FSMTransition(
    origStateId: Int = 0,
    destinationStateId: Int = 0,
    cubicAngle: Double = DEF_SELF_TRANSITION_ANGLE
) : AbstractComponent(), Labeled, Describable {

    companion object {
        private val LOG by logger(FSMTransition::class)

        private val TYPE: String by lazy { Translations.getString("antares.fsm.transition") }

        private const val STRETCH = 50.0

        private const val SEPARATOR = '/'

        private const val CONTAINS_SENSITIVITY = 2.0

        private const val CUBIC_OPEN_ANGLE = PI_6

        private const val CUBIC_SIZE = 3.0

        private const val CUBIC_ANGLE_STEP = PI / 12

        const val DEF_SELF_TRANSITION_ANGLE = -PI * 3 / 2
    }

    /** The condition in terms of input signals that transitions the system to [destinationState].*/
    var condition: String = ""
        set(value) {
            field = value
            updateLabel()
        }

    /** The output value(s) of the system when this [FSMTransition] is traversed (Mealy machine). */
    var output: String = ""
        set(value) {
            field = value
            updateLabel()
        }

    val isSelfTransition: Boolean get() = origStateId == destinationStateId

    override val label: Label = Label(condition, font, richText = false, fillBackground = true)

    override var description: Description by observableDescription(Description(""))

    /** The arrow head drawn at the [destinationPoint]. */
    private val arrowHead = ArrowHead()

    /** The ID of the [FSMState] where this [FSMTransition] originates.*/
    var origStateId: Int = origStateId
        private set

    /** The ID of the [FSMState] where this [FSMTransition] leads to. */
    var destinationStateId: Int = destinationStateId
        private set

    /** The [FSMState] where this [FSMTransition] originates. Set when added to [Drawing]. */
    private var originState: FSMState? = null

    /** The [FSMState] where this [FSMTransition] leads to. Set when added to [Drawing]. */
    private var destinationState: FSMState? = null

    /** The point on the origin's [FSMState] circle where the Bézier curve starts.*/
    var originPoint: Point2D = Point2D.ZERO
        private set

    /** The point on the destination's [FSMState] circle where the Bézier curve ends.*/
    var destinationPoint: Point2D = Point2D.ZERO
        private set

    /** The intermediate "control" point of the Bézier curve.*/
    private var bezierPoint: Point2D = Point2D.ZERO

    /** The second intermediate control point of the Bézier curve. Used only for cubic curve.*/
    private var bezierPoint2: Point2D = Point2D.ZERO

    /** The [Path] representing the quadratic Bézier curve. */
    private var path: Path = System.createPath()

    /**
     * The rotation angle of cubic curves in radians. Manually set by the user and made persistent.
     * The value is in the range -0.0 ... -2*PI (always negative).
     */
    private var cubicAngle = cubicAngle
        set(value) {
            if (field != value) {
                field = value
                if (!isReading) {
                    updateGeometry(0)
                }
            }
        }

    private val bbox = Rectangle2D()

    private val quadraticHandler: InputEventHandler<EditInputEventContext> by lazy { QuadraticCurveInputHandler() }

    private val cubicHandler: InputEventHandler<EditInputEventContext> by lazy { CubicCurveInputHandler() }

    /**
     * Determines the level of distance between the line connecting the two [FSMState] centers, and the [path]
     * of this [FSMTransition]. Different levels prevent multiple [FSMTransition]'s between the same two [FSMState]s
     * from overlapping each other. Can also be manually shaped by the user.
     */
    private var level: Double = 0.0
        set(value) {
            if (field != value) {
                if (isReading) {
                    field = value
                } else {
                    invalidate()
                    field = value
                    generateQuadraticLayout()
                    invalidate()
                    update()
                }
            }
        }

    /**
     * Set to `true` if the user has manually shaped the Bézier curve by setting [level],
     * in which case [level] is made persistent. However, if the connected [FSMState] are changed,
     * the generated layout is used, and [manuallyShaped] is set to `false`.
     */
    private var manuallyShaped: Boolean = false
        set(value) {
            if (field != value) {
                field = value
            }
        }

    /** ---- [AbstractComponent] */

    override val type: String get() = TYPE

    override var location: Point2D = originPoint

    /**
     * Copying currently not supported, as it would lead to [FSMTransition] being connected to
     * origin [FSMState]s rather than the copied [FSMState]s. Can't be fixed easily.
     */
    override val copyable: Boolean get() = false

    override fun draw(context: DrawContext) {
        context.g.stroke = stroke
        context.g.color = context.choose(color).foregroundColor
        context.g.draw(path)
        arrowHead.draw(context)
        label.draw(context)
    }

    override val boundingBox: RectangularShape get() = Rectangle2D(bbox)

    override fun contains(x: Double, y: Double): Boolean = path.intersects(
        x - CONTAINS_SENSITIVITY,
        y - CONTAINS_SENSITIVITY,
        2 * CONTAINS_SENSITIVITY,
        2 * CONTAINS_SENSITIVITY)

    override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> =
        if (isSelfTransition) {
            cubicHandler as InputEventHandler<T>
        } else {
            quadraticHandler as InputEventHandler<T>
        }

    /**
     * [Storable] resolution doesn't work when FSMTransition is cloned during add(), because then
     * the FSMStates are NOT in scope of the ReferenceResolver. Therefore, we access
     * the FSMState directly in the parent DrawableContainer
     */
    override fun <T : Drawable> handleAdded(container: DrawableContainer<T>) {
        super.handleAdded(container)
        originState = (container as ComponentContainer).getWithId(origStateId) as? FSMState
        destinationState = container.getWithId(destinationStateId) as? FSMState
        if (originState != null && destinationState != null) {
            AntaresModelModule.fsmEditorService.handleTransitionAdded(this, container as FSMDrawing)
        }
    }

    override fun <T : Drawable> handleRemoved(container: DrawableContainer<T>) {
        super.handleRemoved(container)
        AntaresModelModule.fsmEditorService.handleTransitionRemoved(this, container as FSMDrawing)
    }

    /** ---- [Storable] */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeInt("orig", origStateId)
        writer.writeInt("dest", destinationStateId)
        if (StringUtils.isNotBlank(condition)) {
            writer.writeString("condition", condition)
        }
        if (StringUtils.isNotBlank(output)) {
            writer.writeString("output", output)
        }
        if (isSelfTransition) {
            writer.writeDouble("cubicAngle", cubicAngle)
        }
        description.write("desc", writer)
        if (manuallyShaped) {
            writer.writeDouble("level", level)
        }
    }

    override fun read(reader: StoreReader) {
        super.read(reader)

        origStateId = reader.readInt("orig")
        destinationStateId = reader.readInt("dest")
        if (reader.hasAttribute("condition")) {
            condition = reader.readString("condition")
        }
        if (reader.hasAttribute("output")) {
            output = reader.readString("output")
        }
        if (reader.hasAttribute("cubicAngle")) {
            cubicAngle = reader.readDouble("cubicAngle")
        }
        description = Description.read("desc", reader)
        if (reader.hasAttribute("level")) {
            level = reader.readDouble("level")
            manuallyShaped = true
        }

        // Dummy request resolution so that allResolutionDone() gets called
        reader.requestResolution(this, Reference())
    }

    override fun allResolutionDone() {
        super.allResolutionDone()

        /**
         * This is necessary here because if the [FSMTransition] comes in the [FSMDrawing] BEFORE the references
         * [FMSStates][FSMState], the references cannot be resolved during resolution (which does Container.add()).
         * This is e.g. the case if the stacking order was changed.
         */
        if (parent != null) {
            handleAdded(parent!!)
        }
    }

    /** ---- [FSMTransition] */

    fun updateGeometry(level: Int) {
        if (!manuallyShaped && !isSelfTransition) {
            this.level = level.toDouble()
        }
        invalidate()
        updateGeometryImpl()
        invalidate()
        validate()
    }

    /**
     * Out of [originState] and [destinationState], returns the one that is NOT [state].
     * Only defined after this [FSMTransition] has been added to a [Drawing]. Otherwise,
     * throws [NullPointerException].
     */
    fun otherStateThan(state: FSMState): FSMState =
        if (state.id == origStateId) {
            destinationState!!
        } else {
            originState!!
        }

    fun getConnectionPoint(state: FSMState): Point2D? =
        if (state === originState) {
            originPoint
        } else if (state === destinationState) {
            destinationPoint
        } else {
            null
        }

    private fun updateLabel() {
        invalidate()
        val s = StringBuilder(condition)
        if (StringUtils.isNotBlank(output)) {
            s.append(SEPARATOR)
            s.append(output)
        }
        label.text = s.toString()
        updateBoundingBox()
        invalidate()
        validate()
    }

    private fun updateBoundingBox() {
        bbox.setFrame(path.boundingBox)
        bbox.add(arrowHead.boundingBox)
        bbox.add(label.boundingBox)
    }

    private fun updateGeometryImpl() {
        if (originState != null && destinationState != null) {
            if (originState === destinationState) {
                generateRotatedCubicLayout()
            } else {
                generateQuadraticLayout()
            }
        }
    }

    // ---- Quadratic curve for A to B transitions

    private fun generateQuadraticLayout() {
        val bezier0 = calculateQuadraticBezierPoint(originState!!.center, destinationState!!.center)
        originPoint = Geometry.circleLineIntersection(originState!!.center, originState!!.radius, bezier0)
        destinationPoint = Geometry.circleLineIntersection(destinationState!!.center, destinationState!!.radius, bezier0)
        bezierPoint = calculateQuadraticBezierPoint(originPoint, destinationPoint)

        updateQuadraticPath()
    }

    private fun updateQuadraticPath() {
        path = System.createPath()
        path.moveTo(originPoint.x, originPoint.y)
        path.quadTo(bezierPoint.x, bezierPoint.y, destinationPoint.x, destinationPoint.y)

        arrowHead.setLocation(destinationPoint, bezierPoint)

        label.location = calculateQuadraticLabelPoint()
        updateBoundingBox()
    }

    private fun calculateQuadraticBezierPoint(p1: Point2D, p2: Point2D): Point2D {
        var middle = Geometry.middle(p1, p2)
        val normal = Vector2D(Geometry.normal(p1, p2)).normalize
        return middle.add(normal.multiply(-level * STRETCH).point)
    }

    private fun calculateQuadraticLabelPoint(): Point2D {
        val middle = Geometry.middle(originPoint, destinationPoint)
        val normal = Vector2D(Geometry.normal(originPoint, destinationPoint)).normalize
        return middle.add(normal.multiply(-level * STRETCH / 2.0).point)
    }

    // ---- Cubic curve for A to A transitions

    private fun generateRotatedCubicLayout() {
        val bezierPointNormal = Point2D(
            originState!!.center.x + originState!!.radius * CUBIC_SIZE * cos(CUBIC_OPEN_ANGLE),
            originState!!.center.y - originState!!.radius * CUBIC_SIZE * sin(CUBIC_OPEN_ANGLE))
        val bezierPoint2Normal = Point2D(
            originState!!.center.x + originState!!.radius * CUBIC_SIZE * cos(CUBIC_OPEN_ANGLE),
            originState!!.center.y + originState!!.radius * CUBIC_SIZE * sin(CUBIC_OPEN_ANGLE))

        bezierPoint = Geometry.rotateCentered(bezierPointNormal, originState!!.center, cubicAngle)
        bezierPoint2 = Geometry.rotateCentered(bezierPoint2Normal, originState!!.center, cubicAngle)

        originPoint = Geometry.rotateCentered(
            Geometry.circleLineIntersection(originState!!.center, originState!!.radius, bezierPointNormal),
            originState!!.center,
            cubicAngle)
        destinationPoint = Geometry.rotateCentered(
            Geometry.circleLineIntersection(originState!!.center, originState!!.radius, bezierPoint2Normal),
            originState!!.center,
            cubicAngle)

        path = System.createPath()
        path.moveTo(originPoint.x, originPoint.y)
        path.curveTo(bezierPoint.x, bezierPoint.y, bezierPoint2.x, bezierPoint2.y, destinationPoint.x, destinationPoint.y)

        arrowHead.setLocation(destinationPoint, bezierPoint2)

        label.location = calculateCubicLabelPoint()
        updateBoundingBox()
    }

    private fun calculateCubicLabelPoint(): Point2D {
        return Geometry.rotateCentered(
            Point2D(originState!!.centerX + originState!!.radius * CUBIC_SIZE * 0.707, originState!!.centerY),
            originState!!.center,
            cubicAngle)
    }

    private open inner class AbstractCurveInputHandler : InputEventHandlerAdapter<EditInputEventContext>() {
        protected var pressedLocation: Point2D = Point2D.ZERO

        override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
            pressedLocation = context.location
            return this
        }
    }

    /** Interactively change [level] to shape the [path] of this [FSMTransition]. */
    private inner class QuadraticCurveInputHandler : AbstractCurveInputHandler() {
        private var offset: Point2D = Point2D.ZERO
        private var oldLevel = 0.0

        override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
            super.mousePressed(context)
            offset = pressedLocation.subtract(bezierPoint)
            oldLevel = level
            return this
        }

        override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
            level = Geometry.lineDistance(originState!!.center, destinationState!!.center, context.location) / STRETCH * 2
            return this
        }

        override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            if (context.location.distance(pressedLocation) >= Editor.DRAG_THRESHOLD) {
                context.editor.commandManager.execute(ShapeTransitionCommand(context.editor, id, oldLevel, level))
            } else {
                level = oldLevel
            }
            return null
        }
    }

    /** Interactively change [cubicAngle] to rotate a self-transition around its [FSMState]. */
    private inner class CubicCurveInputHandler : AbstractCurveInputHandler() {

        private var oldAngle = 0.0

        override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
            super.mousePressed(context)
            oldAngle = cubicAngle
            return this
        }

        override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
            val angle = Geometry.angle(originState!!.center, context.location)
            cubicAngle = -floor(angle / CUBIC_ANGLE_STEP) * CUBIC_ANGLE_STEP
            return this
        }

        override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            context.editor.commandManager.register(RotateSelfTransitionCommand(context.editor, id, oldAngle, cubicAngle))
            return null
        }
    }

    private class RotateSelfTransitionCommand(
        editor: Editor,
        val transitionId: Int,
        val oldAngle: Double,
        val newAngle: Double
    ) : AbstractCommand("antares.fsm.transition.rotate.name", editor), Undoable {

        private val transition: FSMTransition get() = editor!!.drawing.getWithId(transitionId) as FSMTransition

        override fun execute() {
            transition.cubicAngle = newAngle
        }

        override fun undo() {
            transition.cubicAngle = oldAngle
        }
    }

    private class ShapeTransitionCommand(
        editor: Editor,
        val transitionId: Int,
        val oldLevel: Double,
        val level: Double
    ) : AbstractCommand("antares.fsm.transition.shape.name", editor), Undoable {

        private val transition: FSMTransition get() = editor!!.drawing.getWithId(transitionId) as FSMTransition
        private val oldManuallyShaped = transition.manuallyShaped

        override fun execute() {
            transition.level = level
            transition.manuallyShaped = true
        }

        override fun undo() {
            transition.manuallyShaped = oldManuallyShaped
            transition.level = oldLevel
        }
    }
}