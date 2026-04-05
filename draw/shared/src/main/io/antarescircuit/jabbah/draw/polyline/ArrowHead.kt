package io.antarescircuit.jabbah.draw.polyline

import io.antarescircuit.jabbah.base.*
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.AbstractDrawable
import io.antarescircuit.jabbah.base.geom.*
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.draw.module.DrawModule

/**
 * A special [LineTerminator] representing an arrow head.
 * The location of the [LineTerminator] is at the end of the [Polyline] to which it is connected.
 */
class ArrowHead(
	width: Int = DEFAULT_WIDTH,
	private val length: Int = DEFAULT_LENGTH,
	compactness: Float = DEFAULT_COMPACTNESS,
	val filled: Boolean = DEFAULT_FILLED,
	bidirectional: Boolean = DEFAULT_BIDIRECTIONAL
) : AbstractDrawable(), LineTerminator {

	companion object {

		/** The default width. */
		private const val DEFAULT_WIDTH = 4

		/** The default length. */
		private const val DEFAULT_LENGTH = 10

		/** The default compactness. */
		private const val DEFAULT_COMPACTNESS = 1.0f

		/** The default 'filled' attribute. */
		private const val DEFAULT_FILLED = true

		private const val DEFAULT_BIDIRECTIONAL = false

		/** The gap between the two bidirectional arrow heads. */
		private const val GAP = 4

		private val TRANSFORM = System.createAffineTransform()

		private val STROKE = Stroke(1.0f)

		fun createDefault(): ArrowHead = ArrowHead()

		fun createBidirectionalDefault(): ArrowHead = ArrowHead(bidirectional = true)
	}


	/** Holds the location of the arrow head peek.*/
	private var location = Point2D.ZERO

	/** Holds the rotation angle in radians.*/
	private var rotation: Double = 0.0

	/** Holds the location at which the attached [Polyline] segment should end. This is relevant if [filled] is `false`.*/
	override var lineEnd = Point2D.ZERO
		private set

	private val shape: Path = if (bidirectional) {
		createBidirectionalShape(width, length, compactness)
	} else {
		createUnidirectionalShape(width, length, compactness)
	}

	/** ---- [Drawable] */

	override val boundingBox: RectangularShape
		get() = Rectangle2D(shape.boundingBox).expandBy(STROKE.width.toDouble())

	override fun draw(context: DrawContext) {
		val oldStroke = context.g.stroke
		context.g.stroke = STROKE
		context.g.draw(shape)
		if (filled) {
			context.g.fill(shape)
		}
		context.g.stroke = oldStroke

		DrawModule.drawDebugBoundingBox(this, context.g)
	}

	override fun contains(x: Double, y: Double): Boolean = shape.contains(x, y)

	override val size: Int get() = length

	/** ---- [LineTerminator] */

	override fun setLocation(location: Point2D) {
		invalidate()
		setLocationImpl(location.x, location.y)
		invalidate()
		update()
	}

	override fun setLocation(location: Point2D, orientation: Point2D) {
		invalidate()
		setLocationImpl(location.x, location.y)
		TRANSFORM.setToIdentity()
		val rot = Geometry.angle(orientation, location)
		TRANSFORM.setToRotation(rotation - rot, location.x, location.y)
		lineEnd = TRANSFORM.transform(lineEnd)
		shape.transform(TRANSFORM)
		rotation = rot
		invalidate()
		update()
	}

	/** ---- [ArrowHead] */

	private fun setLocationImpl(x: Double, y: Double) {
		TRANSFORM.setToTranslation(x - location.x, y - location.y)
		location = Point2D(x, y)
		shape.transform(TRANSFORM)
		lineEnd = TRANSFORM.transform(lineEnd)
	}

	private fun createUnidirectionalShape(width: Int, length: Int, compactness: Float): Path {
		val path = System.createPath()

		path.moveTo(0, 0)
		path.lineTo(-length, -width)
		lineEnd = if (compactness == 1.0f) {
			path.lineTo(-length, width)
			Point2D(-length, 0)
		} else {
			val innerLength = length * compactness
			path.lineTo(-innerLength, 0.0f)
			path.lineTo(-length, width)
			Point2D(-innerLength.toDouble(), 0.0)
		}

		path.close()
		return path
	}

	private fun createBidirectionalShape(width: Int, length: Int, compactness: Float): Path {
		val path = System.createPath()
		val innerLength = length * compactness

		path.moveTo(0, 0)
		path.lineTo(-length, -width)

		if (compactness == 1.0f) {
			path.lineTo(-length, 0)
			path.lineTo(-length - GAP, 0)
		} else {
			path.lineTo(-innerLength, 0.0f)
			path.lineTo(-length - GAP - (length - innerLength), 0.0f)
		}

		path.lineTo(-length - GAP, -width)
		path.lineTo(-length - GAP - length, 0)
		path.lineTo(-length - GAP, width)

		if (compactness == 1.0f) {
			path.lineTo(-length - GAP, 0)
			path.lineTo(-length, 0)
		} else {
			path.lineTo(-length - GAP - (length - innerLength), 0.0f)
			path.lineTo(-innerLength, 0.0f)
		}

		path.lineTo(-length, width)

		lineEnd = Point2D(-2 * length - GAP, 0)

		path.close()
		return path
	}
}