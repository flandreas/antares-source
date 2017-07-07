package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.port.AbstractPortView
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition

/**
 * Created by andreas on 23.02.17.
 */
class TestPortView<T: Any>(
        port: Port<T>,
        direction: Direction,
        portLabelPosition: PortLabelPosition,
        length: Int
) : AbstractPortView<T>(port, 0, 0, direction, portLabelPosition, length) {

    override val boundingBox: Rectangle2D
        get() {
            val bbox = Rectangle2D()
            bbox.add(location.toRect(0.0))
            bbox.add(connectionPoint.toRect(0.0))
            return bbox
        }

    override var storableId: Int
        get() = throw UnsupportedOperationException()
        set(value) {}

    override fun draw(context: DrawContext) {
        // empty
    }

    override fun contains(x: Double, y: Double): Boolean {
        return boundingBox.contains(x, y)
    }

    override val minSegmentLength: Int
        get() = unconnectedLength

    override fun getConnectedLength(): Int = 0
}