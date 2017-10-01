package ch.scorpion.jabbah.edit.model.polyline

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.Stylable
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.polyline.Polyline
import ch.scorpion.jabbah.draw.polyline.PolylineDrawable
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A standard implementation of a [Polyline] [Component].
 */
class PolylineComponent(
    val polyline: PolylineDrawable = PolylineDrawable()
) : AbstractComponent(), Polyline by polyline, Stylable by polyline {

    init {
        DrawableOwner(this, polyline)
    }

    /** ---- [Locatable] */

    override var location: Point2D
        get() = polyline.location
        set(value) {polyline.location = value}

    /** ---- [Drawable] interface */

    override val boundingBox: RectangularShape
        get() = polyline.boundingBox

    override fun draw(context: DrawContext) {
        polyline.draw(context)
    }

    override val canMirror: Boolean
        get() = true

    override fun contains(x: Double, y: Double): Boolean {
        return polyline.contains(x, y)
    }

    /** ---- [Component] interface */

    override val type: String?
        get() = Translations.getString("edit.component.polyline")

    override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
        get() = SelectionDrawingStrategy.ABOVE
        set(value) {super.preferredSelectionDrawingStrategy = value}

    /**
     * [PolylineComponent] and [PolylineDrawable] are both [Stylable]s. If [PolylineComponent] wouldn't define
     * its [PolylineDrawable] as property owner, the properties in [PolylineComponent] would be edited by the user,
     * although those of [PolylineDrawable] are used for drawing.
     */
    override val propertyOwner: Any get() = polyline

    /** ---- [Snappable] interface */

    override val snappableX: Array<SnappableX>
        get() {
            if(pointsCount > 0) {
                return arrayOf(SnappableXCoordinate(polyline.getFirstPoint().x))
            }
            return super.snappableX
        }

    override val snappableY: Array<SnappableY>
        get() {
            if(pointsCount > 0) {
                return arrayOf(SnappableYCoordinate(polyline.getFirstPoint().y))
            }
            return super.snappableY
        }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeBoolean("filled", filled)
        writer.writePoints("points", polyline.getPoints(0, polyline.pointsCount))
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        if (reader.hasAttribute("filled")) {
            filled = reader.readBoolean("filled")
        }
        polyline.setPoints(reader.readPoints("points"))
    }
}