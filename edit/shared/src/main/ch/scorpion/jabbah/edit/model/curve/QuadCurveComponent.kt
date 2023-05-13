package ch.scorpion.jabbah.edit.model.curve

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.Rotatable
import ch.scorpion.jabbah.draw.drawable.RotationDirection
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A [Component] consisting of 3 points defining a quadratic curve as of [Path.quadTo].
 * The first and the third point represent the endpoints of the curve, while the
 * second point represents the "control point", defining the curvature of the curve.
 */
class QuadCurveComponent(points: List<Point2D> = DEFAULT_POINTS) : AbstractComponent(), Transparent {

	companion object {
		private const val CONTAINS_SENSITIVITY = 2.0
		private val type = Translations.getString("edit.component.quadraticCurve")
		private val DEFAULT_POINTS = listOf(
			Point2D(0, 0),
			Point2D(100, 100),
			Point2D(200, 0)
		)
	}

	/** Contains the 3 points of the [QuadCurveComponent] in absolute coordinate space.*/
	private var _points = mutableListOf<Point2D>()
		set(value) {
			if (field.size > 0) {
				invalidate()
			}
			field = value
			updatePath()
			invalidate()
			update()
		}

	/** Contains the [Path] representing `_points` in absolute coordinate space. */
	private lateinit var path: Path

	init {
		setPointsImpl(points)
	}

	var points: List<Point2D>
		get() = _points
		set(value) {
			check(value.size == 3)
			setPointsImpl(value)
		}

	override val type: String get() = QuadCurveComponent.type

	override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
		get() = SelectionDrawingStrategy.ABOVE
		set(value) {
			super.preferredSelectionDrawingStrategy = value
		}


	override var location: Point2D
		get() = _points[0]
		set(value) {
			if (location != value) {
				doInvalidating {
					val dx = value.x - points[0].x
					val dy = value.y - points[0].y
					_points.toList().forEachIndexed { index, p -> _points[index] = p.add(dx, dy) }
				}
			}
		}

	override val boundingBox: RectangularShape get() = path.boundingBox

	override fun draw(context: DrawContext) {
		if (context.useContextColors) {
			drawImpl(context, context.color!!.foregroundColor, context.color!!.backgroundColor)
		} else {
			drawImpl(context, transparent.applyTo(foregroundColor), if (filled) transparent.applyTo(backgroundColor) else null)
		}
	}

	override fun contains(x: Double, y: Double): Boolean =
		path.intersects(
			x - CONTAINS_SENSITIVITY,
			y - CONTAINS_SENSITIVITY,
			2 * CONTAINS_SENSITIVITY,
			2 * CONTAINS_SENSITIVITY)

	/** ---- [Transparent] interface */

	private val transparent = TransparentImpl(this)

	override var transparency: Int
		get() = transparent.transparency
		set(value) {
			transparent.transparency = value
		}

	/** ---- [Rotatable] interface */

	override val rotatable: Boolean get() = true

	override val useRotation: Boolean get() = false

	override fun rotate(direction: RotationDirection, pivot: Point2D?) {
		points = points.map { direction.rotation.rotatePointAround(points[0], it) }
	}

	/** ---- [Storable] interface */

	override fun read(reader: StoreReader) {
		super.read(reader)
		val p = mutableListOf<Point2D>()
		p.addAll(reader.readPoints("points"))
		_points = p

	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writePoints("points", points)
	}

	/** ---- [Snappable] interface */

	override val snappableX: Array<SnappableX> get() = arrayOf(SnappableXCoordinate(points[0].x), SnappableXCoordinate(points[2].x))

	override val snappableY: Array<SnappableY> get() = arrayOf(SnappableYCoordinate(points[0].y), SnappableYCoordinate(points[2].y))

	/** ---- [QuadCurveComponent] */

	fun setPointAt(index: Int, location: Point2D) {
		require(index in 0..2)
		doInvalidating { _points[index] = location }
	}

	fun getPointAt(index: Int): Point2D = points[index]

	private fun doInvalidating(logic: () -> Unit) {
		if (points.size == 3) {
			invalidate()
		}
		logic.invoke()
		updatePath()
		invalidate()
		update()
	}

	private fun setPointsImpl(newPoints: List<Point2D>) {
		val p = mutableListOf<Point2D>()
		p.addAll(newPoints)
		_points = p
	}

	private fun updatePath() {
		check(points.size == 3)
		path = System.createPath()
		path.moveTo(points[0].x, points[0].y)
		path.quadTo(points[1].x, points[1].y, points[2].x, points[2].y)
	}

	private fun drawImpl(context: DrawContext, lineColor: Color, fillColor: Color?) {
		val oldColor = context.g.color

		if (shadow && fillColor != null) {
			DropShadow.draw(context, transparency) {
				context.g.fill(path)
			}
		}

		if (fillColor != null) {
			context.g.color = fillColor
			context.g.fill(path)
		}
		context.g.color = lineColor
		context.g.stroke = stroke
		context.g.draw(path)

		context.g.color = oldColor
	}
}