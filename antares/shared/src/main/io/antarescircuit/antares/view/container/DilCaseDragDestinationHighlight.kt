package io.antarescircuit.antares.view.container

import io.antarescircuit.antares.view.port.DigitalPortViewStyle
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.drawable.AbstractRectangle
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.drag.DragDestination
import io.antarescircuit.jabbah.edit.drag.DragDestinationHighlight
import io.antarescircuit.jabbah.edit.snap.ComponentSnapper

class DilCaseDragDestinationHighlight(
	destination: DilCase
) : AbstractRectangle(Rectangle2D(destination.boundingBox)), DragDestinationHighlight {

	companion object {
		private const val HALF_SIZE = 3
	}

	private val snapPoints = mutableListOf<Point2D>()

	private val stroke: Stroke get() = DrawModule.properties.getStroke(ComponentSnapper.PROP_SNAP_HIGHLIGHT_STROKE)

	init {
		fillSnapPoints(destination)
	}

	/** ---- [DragDestinationHighlight] */

	override fun handleDragged(component: Component, destination: DragDestination) {
		if (component !is DigitalPortViewComponent || destination !is DilCase) {
			return
		}
		if (component.portViewStyle != DigitalPortViewStyle.DIL) {
			changePortViewStyleToDil(component)
		}
		if (component.location.x < destination.centerX) {
			component.direction = Direction.WEST
		} else {
			component.direction = Direction.EAST
		}
	}

	private fun changePortViewStyleToDil(component: DigitalPortViewComponent) {
		component.portViewStyle = DigitalPortViewStyle.DIL
		component.showBitWidthAnnotation = false
	}

	/** ---- [Drawable] */

	override val boundingBox: RectangularShape
		get() = Rectangle2D(super.boundingBox).expandBy(HALF_SIZE.toDouble()) as Rectangle2D

	override fun draw(context: DrawContext) {
		context.g.color = DrawModule.properties.getColor(ComponentSnapper.PROP_SNAP_HIGHLIGHT_COLOR)
		context.g.stroke = stroke
		snapPoints.forEach { drawSnapPoints(context, it) }
	}

	override val lineWidth: Double get() = stroke.width.toDouble()

	private fun fillSnapPoints(destination: DilCase) {
		val snappableX = destination.snappableX.map { it as DilCase.DilPositionX }.toList()
		val snappableY = destination.snappableY.map { it as DilCase.DilPositionY }.toList()

		snappableX
			.filter { it.isBorder }
			.forEach { snapX ->
				snappableY
					.filter { !it.isBorder }
					.forEach { snapY ->
						snapPoints.add(Point2D(snapX.x, snapY.y))
					}
			}
	}

	private fun drawSnapPoints(context: DrawContext, p: Point2D) {
		context.g.drawLine(p.x - HALF_SIZE, p.y - HALF_SIZE, p.x + HALF_SIZE, p.y + HALF_SIZE)
		context.g.drawLine(p.x + HALF_SIZE, p.y - HALF_SIZE, p.x - HALF_SIZE, p.y + HALF_SIZE)
	}
}