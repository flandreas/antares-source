package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.select.RubberBand.Companion.PROP_FILL_PAINT
import ch.scorpion.jabbah.edit.select.RubberBand.Companion.PROP_STROKE
import ch.scorpion.jabbah.edit.select.RubberBand.Companion.PROP_STROKE_PAINT
import kotlin.math.abs
import kotlin.math.min

/**
 * A [RubberBand] of rectangular shape.
 */
class RectangularRubberBand : AbstractRectangle(Rectangle2D()), RubberBand {

	companion object {
		private const val DRAG_THRESHOLD = 2
	}

	/** ---- [Unzoomable] interface */

	override var zoomPan: ZoomPan? = null

	/** ---- [RubberBand] interface */

	override val inputEventHandler: InputEventHandler<EditInputEventContext> = EventHandler()

	/** ---- [Drawable] */

	override fun draw(context: DrawContext) {
		if (zoomPan != null) {
			val p1 = zoomPan!!.transform.modelToView(Point2D(x, y))
			val p2 = zoomPan!!.transform.modelToView(Point2D(x + width, y + height))
			drawRectangle(
				context,
				p1.x, p1.y, p2.x - p1.x, p2.y - p1.y,
				DrawModule.properties.getColor(PROP_STROKE_PAINT),
				DrawModule.properties.getColor(PROP_FILL_PAINT),
				DrawModule.properties.getStroke(PROP_STROKE)
			)
		}
	}

	/** ---- [RectangularDrawable] */

	override val lineWidth: Double get() = DrawModule.properties.getStroke(PROP_STROKE).width.toDouble()

	/** ---- [RectangularRubberBand] */

	private inner class EventHandler : InputEventHandlerAdapter<EditInputEventContext>() {
		private var pressedLocation = Point2D.ZERO
		private var isDragging = false

		override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			super.mousePressed(context)
			zoomPan = context.view.zoomPan
			pressedLocation = context.location
			return this
		}

		override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			super.mouseDragged(context)

			if (!isDragging && pressedLocation.distance(context.x, context.y) >= DRAG_THRESHOLD) {
				isDragging = true
				// Add RubberBand before setting bounds, because otherwise the zoom factor is not yet set,
				// which will result in an infinite bounding box
				context.drawingView().ghostContainer.add(this@RectangularRubberBand)
			}
			if (isDragging) {
				setBounds(
					min(pressedLocation.x, context.x),
					min(pressedLocation.y, context.y),
					abs(pressedLocation.x - context.x),
					abs(pressedLocation.y - context.y)
				)
				validate()
			}
			return this
		}

		override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			super.mouseReleased(context)
			isDragging = false
			context.drawingView().ghostContainer.remove(this@RectangularRubberBand)
			context.drawingView().ghostContainer.validate()
			return this
		}
	}
}