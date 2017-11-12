package ch.scorpion.jabbah.graph.view.port

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.graph.model.Port

class GenericPortView<T: Any>(
        port: Port<T>,
        x: Int = 0,
        y: Int = 0,
        direction: Direction = Direction.SOUTH,
        portLabelPosition: PortLabelPosition = PortLabelPosition.INTERNAL,
        connectable: Boolean = false
) : AbstractPortView<T>(port, x, y, direction, portLabelPosition, 0, connectable) {

    override var transparency: Int = Transparent.FULLY_OPAQUE

    override fun contains(x: Double, y: Double): Boolean = false

    override val minSegmentLength: Int get() = 0

    override fun getConnectedLength(): Int = 0

    override val boundingBox: RectangularShape get() = Rectangle2D(location.x, location.y, 0.0, 0.0)

    override fun draw(context: DrawContext) { }
}