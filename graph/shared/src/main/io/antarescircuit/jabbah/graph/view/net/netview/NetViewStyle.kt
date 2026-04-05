package io.antarescircuit.jabbah.graph.view.net.netview

import io.antarescircuit.jabbah.base.EnumProperty
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.graph.view.net.node.NodeView
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.view.net.node.NodeViewStyling
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewBlockStyling
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewLineStyling
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewStyling
import io.antarescircuit.jabbah.graph.view.net.node.NodeViewBlockStyling
import io.antarescircuit.jabbah.graph.view.net.node.NodeViewDotStyling
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.graph.view.net.edge.LayoutType

enum class NetViewStyle(override val customName: String) : EnumProperty<NetViewStyle> {

    LINE("line") {

        override fun createEdgeViewStyling(styleProvider: StyleProvider, edgeView: EdgeView<*>): EdgeViewStyling =
	        EdgeViewLineStyling(edgeView)

        override fun createNodeViewStyling(styleProvider: StyleProvider, nodeView: NodeView<*>): NodeViewStyling =
	        NodeViewDotStyling(nodeView)

	    override fun supportsLayoutType(layoutType: LayoutType): Boolean = true
    },

    BLOCK("block") {
        override fun createEdgeViewStyling(styleProvider: StyleProvider, edgeView: EdgeView<*>): EdgeViewStyling =
	        EdgeViewBlockStyling(edgeView)

        override fun createNodeViewStyling(styleProvider: StyleProvider, nodeView: NodeView<*>): NodeViewStyling =
	        NodeViewBlockStyling(nodeView)

	    override fun supportsLayoutType(layoutType: LayoutType): Boolean =
			layoutType == LayoutType.ORTHOGONAL
    };

    companion object {

	    const val BASE_KEY = "graph.property.edgeViewLineStyle"
        val BLOCK_BORDER_STROKE = Stroke(0.5f)

        const val BLOCK_HW = 10

        fun withName(customName: String): NetViewStyle {
            for (i in 0 until values().size) {
                if (values()[i].customName == customName) {
                    return values()[i]
                }
            }
            throw IllegalArgumentException("unknown NetViewStyling $customName")
        }
    }

    override fun toString(): String {
        return when (this) {
            // TODO Rename to netViewStyle
            BLOCK -> Translations.getString("graph.property.edgeViewLineStyle.block.name")
            LINE -> Translations.getString("graph.property.edgeViewLineStyle.line.name")
        }
    }

    abstract fun createEdgeViewStyling(styleProvider: StyleProvider, edgeView: EdgeView<*>): EdgeViewStyling

    abstract fun createNodeViewStyling(styleProvider: StyleProvider, nodeView: NodeView<*>): NodeViewStyling

	abstract fun supportsLayoutType(layoutType: LayoutType): Boolean

}