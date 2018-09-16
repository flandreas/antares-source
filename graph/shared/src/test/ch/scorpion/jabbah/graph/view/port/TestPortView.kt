package ch.scorpion.jabbah.graph.view.port

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.port.PortImpl

class TestPortView<T: Any>(
	port: Port<T> = PortImpl(PortType.INPUT),
	direction: Direction = Direction.WEST,
	portLabelPosition: PortLabelPosition = PortLabelPosition.EXTERNAL,
	length: Int = 0
) : AbstractPortView<T>(port, 0, 0, direction, portLabelPosition, length) {

    override var transparency: Int = 0

    override val boundingBox: Rectangle2D
        get() {
            val bbox = Rectangle2D()
            bbox.add(location.toRect(0.0))
            bbox.add(connectionPoint.toRect(0.0))
            return bbox
        }

    override var storableId: Int
        get() = throw UnsupportedOperationException()
        set(value) { }

    override fun draw(context: DrawContext) {
        // empty
    }

    override fun contains(x: Double, y: Double): Boolean {
        return boundingBox.contains(x, y)
    }

    override val minSegmentLength: Int
        get() = unconnectedLength

    override fun getConnectedLength(): Int = 0

	override fun drawAboveOwner(context: DrawContext) {
		// empty
	}

	override fun drawBelowOwner(context: DrawContext) {
		// empty
	}
}