package ch.scorpion.jabbah.edit.model.polyline

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.drawable.Mirrorable
import ch.scorpion.jabbah.draw.drawable.RotationDirection
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.polyline.ArrowHead
import ch.scorpion.jabbah.draw.polyline.Polyline
import ch.scorpion.jabbah.draw.polyline.PolylineDrawable
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.figure.Figure
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A standard implementation of a [Polyline] [Component].
 */
class PolylineComponent(
	val polyline: PolylineDrawable = PolylineDrawable()
) : AbstractComponent(polyline), Polyline by polyline, Transparent, Figure, Mirrorable {

	companion object {
		const val BASE_KEY_ARROW = "draw.property.polyline.arrow"
		private val TYPE = Translations.getString("edit.component.polyline")
	}

	/** Determines whether this [PolylineComponent] displays an [ArrowHead] at its destination. */
	@Suppress("MemberVisibilityCanBePrivate") // Reflection
	var isArrow: Boolean = false
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				updateEndLineTerminator()
				invalidate()
				update()
			}
		}

	init {
		DrawableOwner(this, polyline)
	}

	/** ---- [Locatable] */

	override var location: Point2D
		get() = polyline.location
		set(value) {
			polyline.location = value
		}

	/** ---- [Transparent] interface */

	override var transparency: Int
		get() = polyline.transparency
		set(value) {
			polyline.transparency = value
		}

	/** ---- [Drawable] interface */

	override val boundingBox: RectangularShape
		get() = polyline.boundingBox

	override fun draw(context: DrawContext) {
		polyline.draw(context)
	}

	override fun contains(x: Double, y: Double): Boolean {
		return polyline.contains(x, y)
	}

	/** ---- [Mirrorable] */

	override fun mirrorHorizontally(x: Double) {
		polyline.mirrorHorizontally(x)
	}

	override fun mirrorVertically(y: Double) {
		polyline.mirrorVertically(y)
	}

	/** ---- [Component] interface */

	override val type: String get() = TYPE

	override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
		get() = SelectionDrawingStrategy.ABOVE
		set(value) {
			super.preferredSelectionDrawingStrategy = value
		}

	/** ---- [Snappable] interface */

	override val snappableX: Array<SnappableX>
		get() {
			if (pointsCount > 0) {
				return arrayOf(SnappableXCoordinate(polyline.getFirstPoint().x))
			}
			return super.snappableX
		}

	override val snappableY: Array<SnappableY>
		get() {
			if (pointsCount > 0) {
				return arrayOf(SnappableYCoordinate(polyline.getFirstPoint().y))
			}
			return super.snappableY
		}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writePoints("points", polyline.getPoints(0, polyline.pointsCount))
		if (isArrow) {
			writer.writeBoolean("arrow", isArrow)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		polyline.setPoints(reader.readPoints("points"))
		if (reader.hasAttribute("arrow")) {
			isArrow = reader.readBoolean("arrow")
		}
	}

	override fun rotate(direction: RotationDirection, pivot: Point2D?) {
		polyline.rotate(direction, pivot)
	}

	/** ---- [PolylineComponent] */

	private fun updateEndLineTerminator() {
		if (isArrow) {
			polyline.endLineTerminator = ArrowHead.createDefault()
		} else {
			polyline.endLineTerminator = null
		}
	}
}