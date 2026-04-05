package io.antarescircuit.jabbah.graph.view.port

import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.port.PortImpl

class TestPortView<T: Any>(
    port: Port<T> = PortImpl(PortType.INPUT),
    direction: Direction = Direction.WEST,
    portLabelPosition: PortLabelPosition = PortLabelPosition.EXTERNAL,
    length: Int? = null,
    position: Point2D = Point2D.Companion.ZERO
) : AbstractPortView<T>(port, position.x.toInt(), position.y.toInt(), direction, portLabelPosition, length = length ?: LENGTH) {

	companion object {
		const val LENGTH = 10
	}
	override var transparency: Int = 0

    override val boundingBox: RectangularShape
        get() {
            val bbox = Rectangle2D()
            bbox.add(location.toRect(0.0))
            bbox.add(connectionPoint.toRect(0.0))
            return bbox
        }

    override fun draw(context: DrawContext) { }

    override fun contains(x: Double, y: Double): Boolean = boundingBox.contains(x, y)

    override val minSegmentLength: Int get() = unconnectedLength

    override val connectedLength: Int get() = 0

	override val unconnectedLength: Int = length ?: LENGTH

	override val customUnconnectedLength: Int? get() = null

	override fun drawAboveOwner(context: DrawContext) { }

	override fun drawBelowOwner(context: DrawContext) { }

	override fun prepareConnectionDrawContext(context: DrawContext) { }
}