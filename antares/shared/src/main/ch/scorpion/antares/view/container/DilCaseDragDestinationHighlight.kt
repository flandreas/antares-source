package ch.scorpion.antares.view.container

import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.port.DigitalPortViewStyle
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.drag.DragDestination
import ch.scorpion.jabbah.edit.drag.DragDestinationHighlight
import ch.scorpion.jabbah.edit.snap.ComponentSnapper
import ch.scorpion.jabbah.graph.container.PortViewComponent

class DilCaseDragDestinationHighlight(
	destination: DilCase
) : AbstractRectangle(destination.boundingBox), DragDestinationHighlight {

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
		if (component !is PortViewComponent<*> || destination !is DilCase) {
			return
		}
		val portView = component.portView as DigitalPortView

		if (portView.portViewStyle != DigitalPortViewStyle.DIL) {
			portView.portViewStyle = DigitalPortViewStyle.DIL
		}

		if (component.location.x < destination.centerX) {
			portView.direction = Direction.WEST
		} else {
			portView.direction = Direction.EAST
		}
	}

	/** ---- [Drawable] */

	override val boundingBox: Rectangle2D
		get() = super.boundingBox.expandBy(HALF_SIZE.toDouble()) as Rectangle2D

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