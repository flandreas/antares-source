package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.drawable.RotationDirection
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Look
import ch.scorpion.jabbah.edit.model.text.Alignment
import ch.scorpion.jabbah.edit.model.text.HorizontalLabel
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.Labeled
import ch.scorpion.jabbah.graph.model.Vertice

/**
 * Base class for implementing [VerticeView]s with an external label that use [orientation] for rotating,
 * meaning that their visible representation is changed when being rotated, without effectively applying
 * geometrical rotation around its origin.
 *
 * Since it is not effectively rotated, the label is implemented by [Label], which is by default always upright
 * (i.e., no need to use [HorizontalLabel]).
 */
abstract class OrientableExternallyLabeledRectangularVerticeView<T: Vertice>(
    styleProvider: StyleProvider,
    model: T,
    orientation: Direction
) : OrientableRectangularVerticeView<T>(styleProvider, model), Labeled {

    companion object {
        protected const val LABEL_DIST = Look.SCALE
    }

    /** The [Label] that displays the name of the model [T]. */
    override val label = Label(
        font = font,
        text = model.name)

    override var orientation: Direction = orientation
        set(value) {
            if (value != field) {
                invalidate()
                field = value
                updateView()
                invalidate()
                validate()
            }
        }

    /** Updates the geometry of this [VerticeView] depending on the [orientation] property.*/
    protected abstract fun updateViewImpl()

    /**
     * Update the geometry of this [VerticeView] depending on the [orientation] property,
     * and updates location and alignment of the [label].
     */
    protected fun updateView() {
        updateViewImpl()
        updateLabel()
    }

    /** ---- UI properties */

    /** UI property for editing the model's name, which is displayed as external label.*/
    var name: String?
        get() = model.name
        set(value) {
            model.name = value
        }

    /** ---- [Component] */

    override val useOrientation: Boolean get() = true

    override val boundingBox: RectangularShape
        get() {
            var bb = super.boundingBox
            if (StringUtils.isNotEmpty(label.text)) {
                val lbb = Rectangle2D(label.boundingBox).moveBy(location)
                bb = Rectangle2D(bb).add(lbb)
            }
            return bb
        }

    override fun rotate(direction: RotationDirection, pivot: Point2D?) {
        orientation = when (direction) {
            RotationDirection.Clockwise -> Direction.of(orientation.rotation.previous())
            RotationDirection.CounterClockwise -> Direction.of(orientation.rotation.next())
        }
        pivot?.let {
            location = direction.rotation.rotatePointAround(it, location)
        }
    }

    /** ---- [OrientableExternallyLabeledRectangularVerticeView] */

    /**
     * Updates the text, the location and the alignments of the external [Label] depending
     * on the orientation of this [VerticeView].
     * Assumes that the label is located at the opposite side of the [VerticeView]'s origin.
     */
    protected fun updateLabel() {
        label.text = StringUtils.orEmpty(model.name)
        label.alignment = Alignment.forOrientation(orientation)
        label.location = when (orientation) {
            Direction.EAST -> Point2D(-getOutput().length - bounds.width - LABEL_DIST, 0.0)
            Direction.NORTH -> Point2D(0.0, getOutput().length + bounds.height + LABEL_DIST)
            Direction.WEST -> Point2D(getOutput().length + bounds.width + LABEL_DIST, 0.0)
            Direction.SOUTH -> Point2D(0.0, -getOutput().length - bounds.height - LABEL_DIST)
        }
    }
}