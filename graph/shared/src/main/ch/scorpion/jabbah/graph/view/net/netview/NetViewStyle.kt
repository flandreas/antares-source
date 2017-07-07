package ch.scorpion.jabbah.graph.view.net.netview

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.net.node.NodeViewStyling
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewBlockStyling
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewLineStyling
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewStyling
import ch.scorpion.jabbah.graph.view.net.node.NodeViewBlockStyling
import ch.scorpion.jabbah.graph.view.net.node.NodeViewDotStyling
import ch.scorpion.jabbah.draw.graphics.Stroke

enum class NetViewStyle(val customName: String) {

    LINE("line") {

        override fun createEdgeViewStyling(styleProvider: StyleProvider, edgeView: EdgeView<*>): EdgeViewStyling {
            return EdgeViewLineStyling(edgeView)
        }

        override fun createNodeViewStyling(styleProvider: StyleProvider, nodeView: NodeView<*>): NodeViewStyling {
            return NodeViewDotStyling(nodeView)
        }
    },

    BLOCK("block") {
        override fun createEdgeViewStyling(styleProvider: StyleProvider, edgeView: EdgeView<*>): EdgeViewStyling {
            return EdgeViewBlockStyling(edgeView)
        }

        override fun createNodeViewStyling(styleProvider: StyleProvider, nodeView: NodeView<*>): NodeViewStyling {
            return NodeViewBlockStyling(nodeView)
        }
    };

    companion object {

        val BLOCK_BORDER_STROKE = Stroke()

        val BLOCK_HW = 10

        fun withName(customName: String): NetViewStyle {
            for (i in 0..values().size - 1) {
                if (values()[i].customName == customName) {
                    return values()[i]
                }
            }
            throw IllegalArgumentException("unknown NetViewStyling $customName")
        }
    }

    override fun toString(): String {
        when (this) {
            // TODO Rename to netViewStyle
            BLOCK -> return Translations.getString("graph.property.edgeViewLineStyle.block.name")
            LINE -> return Translations.getString("graph.property.edgeViewLineStyle.line.name")
        }
    }

    abstract fun createEdgeViewStyling(styleProvider: StyleProvider, edgeView: EdgeView<*>): EdgeViewStyling

    abstract fun createNodeViewStyling(styleProvider: StyleProvider, nodeView: NodeView<*>): NodeViewStyling

}