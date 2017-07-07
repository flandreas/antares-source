package ch.scorpion.jabbah.graph.view.net.node

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle.Companion.BLOCK_BORDER_STROKE
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle.Companion.BLOCK_HW

class NodeViewBlockStyling(private val nodeView: NodeView<*>) : NodeViewStyling {

    override val boundingBox = Rectangle2D()

    override fun updateBoundingBox() {
        boundingBox.setFrame(
                nodeView.location.x - BLOCK_HW, nodeView.location.y - BLOCK_HW,
                2.0 * BLOCK_HW, 2.0 * BLOCK_HW)
        boundingBox.setFrame(
                boundingBox.x - BLOCK_BORDER_STROKE.width,
                boundingBox.y - BLOCK_BORDER_STROKE.width,
                boundingBox.width + 2 * -BLOCK_BORDER_STROKE.width,
                boundingBox.height + 2 * -BLOCK_BORDER_STROKE.width)
    }

    override fun draw(context: DrawContext) {
        val oldColor = context.g.color
        val oldStroke = context.g.stroke

        context.g.color = context.color!!.backgroundColor
        context.g.fillRect(
                (nodeView.location.x - NetViewStyle.BLOCK_HW).toInt(), (nodeView.location.y - NetViewStyle.BLOCK_HW).toInt(),
                2 * NetViewStyle.BLOCK_HW, 2 * NetViewStyle.BLOCK_HW)

        context.g.color = context.color!!.foregroundColor
        context.g.stroke = NetViewStyle.BLOCK_BORDER_STROKE
        for (dir in Direction.values()) {
            val edgeView = nodeView.getEdgeView(dir)
            if (edgeView == null) {
                context.g.drawLine(
                        (nodeView.location.x + (dir.next().dx + dir.dx) * NetViewStyle.BLOCK_HW).toInt(),
                        (nodeView.location.y + (dir.next().dy + dir.dy) * NetViewStyle.BLOCK_HW).toInt(),
                        (nodeView.location.x + (dir.previous().dx + dir.dx) * NetViewStyle.BLOCK_HW).toInt(),
                        (nodeView.location.y + (dir.previous().dy + dir.dy) * NetViewStyle.BLOCK_HW).toInt())
            }
        }

        context.g.color = oldColor
        context.g.stroke = oldStroke
    }
}