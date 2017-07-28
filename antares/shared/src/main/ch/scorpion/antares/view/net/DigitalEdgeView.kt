package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.net.DigitalNet
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.connect.DragEdgeViewDestinationConnector
import ch.scorpion.jabbah.graph.view.connect.DragEdgeViewOriginConnector
import ch.scorpion.jabbah.graph.view.connect.EdgeToPortConnector
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewImpl
import ch.scorpion.jabbah.graph.view.style.EdgeStyle


/**
 * Extends [EdgeViewImpl] in order to apply a digital-specific [EdgeView] rendering, i.e. colors that show the
 * value of the [DigitalSignal], and painting large [BitWidth]s with wider strokes.
 *
 * TODO Refactor: Similar code for determining color as in [DigitalNodeView]. Why subclassing?
 * Try to inject the aspect of varying color and stroke into [EdgeView] and [NodeView].
 */
class DigitalEdgeView(
        styleProvider: StyleProvider = DrawStyleModule.styleProvider,
        edgeToPortConnectorSupplier: () -> EdgeToPortConnector = { GraphViewModule.edgeToPortConnector },
        origEndpointConnectorSupplier: () -> DragEdgeViewOriginConnector = { GraphViewModule.dragEdgeViewOriginConnector },
        destEndpointConnectorSupplier: () -> DragEdgeViewDestinationConnector = { GraphViewModule.dragEdgeViewDestinationConnector },
        currentSystemSpeedCategory: CurrentSystemSpeedCategory = ExecutionModule.currentSystemSpeedCategory,
        net: Net<DigitalSignal> = DigitalNet()
) : EdgeViewImpl<DigitalSignal>(
        styleProvider,
        edgeToPortConnectorSupplier,
        origEndpointConnectorSupplier,
        destEndpointConnectorSupplier,
        currentSystemSpeedCategory,
        net
) {

    override fun draw(context: DrawContext) {
        val oldColor = context.g.color
        val oldCompositeColor = context.color

        if (context.appContext as ApplicationMode? === ApplicationMode.EXECUTE && showNetState()) {
            if (!model!!.isError) {
                context.color = model!!.signal!!.getColor()
            }
        } else {
            context.color = context.choose(color)
        }

        context.g.stroke = stroke
        if ((model as DigitalNet).bitWidth.width > 1) {
            context.g.stroke = (style as EdgeStyle).busStroke
        }

        super.draw(context)

        context.color = oldCompositeColor
        context.g.color = oldColor
    }
}

class DigitalEdgeViewFactory(
    private val styleProvider: StyleProvider,
    private val edgeToPortConnectorSupplier: () -> EdgeToPortConnector,
    private val origEndpointConnectorSupplier: () -> DragEdgeViewOriginConnector,
    private val destEndpointConnectorSupplier: () -> DragEdgeViewDestinationConnector,
    private val currentSystemSpeedCategory: CurrentSystemSpeedCategory
) : EdgeViewFactory<DigitalSignal> {

    override fun createEdgeView(): EdgeView<DigitalSignal> {
        return DigitalEdgeView(styleProvider, edgeToPortConnectorSupplier, origEndpointConnectorSupplier,
                destEndpointConnectorSupplier, currentSystemSpeedCategory)
    }

    override fun createEdgeView(net: Net<DigitalSignal>): EdgeView<DigitalSignal> {
        return DigitalEdgeView(styleProvider, edgeToPortConnectorSupplier, origEndpointConnectorSupplier,
                destEndpointConnectorSupplier, currentSystemSpeedCategory, net)
    }
}