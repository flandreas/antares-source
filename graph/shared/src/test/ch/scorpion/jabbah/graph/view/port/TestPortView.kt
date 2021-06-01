package ch.scorpion.jabbah.graph.view.port

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.port.PortImpl
import ch.scorpion.jabbah.io.Storable

class TestPortView<T: Any>(
	port: Port<T> = PortImpl(PortType.INPUT),
	direction: Direction = Direction.WEST,
	portLabelPosition: PortLabelPosition = PortLabelPosition.EXTERNAL,
	length: Int? = null,
	position: Point2D = Point2D.ZERO
) : AbstractPortView<T>(port, position.x.toInt(), position.y.toInt(), direction, portLabelPosition, length ?: LENGTH) {

	companion object {
		const val LENGTH = 10
	}
	override var transparency: Int = 0

    override val boundingBox: Rectangle2D
        get() {
            val bbox = Rectangle2D()
            bbox.add(location.toRect(0.0))
            bbox.add(connectionPoint.toRect(0.0))
            return bbox
        }

    override var storableId: Int = Storable.UNDEFINED_ID

    override fun draw(context: DrawContext) { }

    override fun contains(x: Double, y: Double): Boolean {
        return boundingBox.contains(x, y)
    }

    override val minSegmentLength: Int
        get() = unconnectedLength

    override fun getConnectedLength(): Int = 0

	override fun drawAboveOwner(context: DrawContext) { }

	override fun drawBelowOwner(context: DrawContext) { }

	override fun prepareConnectionDrawContext(context: DrawContext) { }
}