package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.DigitalNet
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.net.node.NodeViewFactory
import ch.scorpion.jabbah.graph.view.net.node.NodeViewImpl


/**
 * Overwritten in order to draw in signal-specific color.
 *
 * TODO Refactor: Similar code for determining color as in [DigitalEdgeView]. Why sublassing?
 * Try to inject the aspect of varying color and stroke into [EdgeView] and [NodeView].
 */
class DigitalNodeView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    net: Net<DigitalSignal> = DigitalNet(),
    netViewStyle: NetViewStyle? = null
) : NodeViewImpl<DigitalSignal>(styleProvider, net, netViewStyle) {

    override fun draw(context: DrawContext) {
        val oldColor = context.g.color
        val oldCompositeColor = context.color

        if (context.appContext as ApplicationMode? === ApplicationMode.EXECUTE) {
            if (!model!!.isError) {
                context.color = model!!.signal!!.getColor()
            }
        } else {
            context.color = context.choose(color)
        }

        super.draw(context)

        context.color = oldCompositeColor
        context.g.color = oldColor
    }
}

class DigitalNodeViewFactory(private val styleProvider: StyleProvider) : NodeViewFactory<DigitalSignal> {
    constructor(): this(DrawStyleModule.styleProvider)

    override fun create(): NodeView<DigitalSignal> {
        return DigitalNodeView(styleProvider)
    }

    override fun create(net: Net<DigitalSignal>): NodeView<DigitalSignal> {
        return DigitalNodeView(styleProvider, net)
    }
}