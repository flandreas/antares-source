package io.antarescircuit.jabbah.graph.view.port

import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.Transparent
import io.antarescircuit.jabbah.graph.container.InternalLabelOrientation
import io.antarescircuit.jabbah.graph.model.Port

class GenericPortView<T : Any>(
	port: Port<T>,
	x: Int = 0,
	y: Int = 0,
	direction: Direction = Direction.SOUTH,
	portLabelPosition: PortLabelPosition = PortLabelPosition.INTERNAL,
	internalLabelOrientation: InternalLabelOrientation = InternalLabelOrientation.Horizontal,
	connectable: Boolean = false
) : AbstractPortView<T>(port, x, y, direction, portLabelPosition, internalLabelOrientation, 0, connectable = connectable) {

	override var transparency: Int = Transparent.FULLY_OPAQUE

	override fun contains(x: Double, y: Double): Boolean = false

	override val minSegmentLength: Int get() = 0

	override val connectedLength: Int get() = 0

	override val unconnectedLength: Int get() = 0

	override val customUnconnectedLength: Int? get() = null

	override val boundingBox: RectangularShape get() = Rectangle2D(location.x, location.y, 0.0, 0.0)

	override fun draw(context: DrawContext) {}

	override fun drawAboveOwner(context: DrawContext) {}

	override fun drawBelowOwner(context: DrawContext) {}

	override fun prepareConnectionDrawContext(context: DrawContext) {}

}