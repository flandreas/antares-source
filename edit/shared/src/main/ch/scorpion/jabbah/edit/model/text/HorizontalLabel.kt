package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.edit.Component

/**
 * A wrapper around a [Label] that can be used as a description at the outside of an object,
 * and that keeps the text always horizontal even if the object is rotated.
 *
 * [HorizontalLabel] assumes that the owner object draws itself rotated, and expects that the
 * owner calls its drawing and bounding box method in an unrotated and untranslated state,
 * because [HorizontalLabel] performs its own rotation and translation behaviour.
 *
 * @property label the wrapped [Label]
 * @property relLocation the location of [label] relative to the owning object. The [Label] is
 * centered and aligned to this position
 */
class HorizontalLabel(
        private val owner: Component,
        relLocation: Point2D,
        orientation: Direction? = Direction.EAST,
        text: String? = null,
        font: Font,
        color: Color? = null) {

    private val label = Label(location = relLocation, text = text, font = font, color = color)

    var text: String
        get() = label.text
        set(value) { label.text = value }

    val boundingBox: Rectangle2D get() {
        return label.boundingBox
    }

    var orientation: Direction? = orientation
        set(value) {
            if (field != value) {
                field = value
                updateAlignment()
            }
        }

    private var relLocation: Point2D = relLocation
        set(value) {
            if (field != value) {
                field = value
                updateLocation()
            }
        }

    init {
        updateLocation()
        updateAlignment()
    }

    fun rotationChanged() {
        updateLocation()
        updateAlignment()
    }

    /** Draws the contained [Label] using an unrotated and untranslated [DrawContext].*/
    fun draw(context: DrawContext) {
        context.g.translate(owner.location.x, owner.location.y)
        label.draw(context)
        context.g.translate(-owner.location.x, -owner.location.y)
    }

    private fun updateLocation() {
        label.location = owner.rotation.rotatePoint(relLocation.x, relLocation.y)
    }

    private fun updateAlignment() {
        if (orientation == null) {
            label.alignment = Alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
        } else {
            when (owner.rotation.rotateDirection(orientation!!)) {
                Direction.EAST -> label.alignment = Alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
                Direction.WEST -> label.alignment = Alignment(HorizontalAlignment.RIGHT, VerticalAlignment.CENTER)
                Direction.NORTH -> label.alignment = Alignment(HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM)
                Direction.SOUTH -> label.alignment = Alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP)
            }
        }
    }
}