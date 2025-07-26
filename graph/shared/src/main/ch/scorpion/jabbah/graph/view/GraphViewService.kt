package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.model.DrawingService
import ch.scorpion.jabbah.edit.model.DrawingServiceImpl
import ch.scorpion.jabbah.graph.view.connect.GraphViewConnectService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeView

interface GraphViewService : DrawingService

open class GraphViewServiceImpl(
    protected val connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService
) : DrawingServiceImpl(), GraphViewService {

    override fun delete(components: List<Component>, drawing: Drawing<*>) {
        for (component in components) {
            if (component is VerticeView<*>) {
                unconnectDeletedVerticeView(component, drawing as GraphView)
            } else if (component is EdgeView<*>) {
                if (drawing.contains(component)) {
                    // Might have been joined away by a previous removal of another EdgeView
                    unconnectDeletedEdgeView(component as EdgeView<Any>)
                }
            }
        }

        super.delete(
            components
                .filter { drawing.contains(it) }
                .map { possibleWrapper(it, drawing) },
            drawing
        )

        if (components.any { it is OscilloscopeView }) {
            GraphViewModule.oscilloscopeViewService.handleOscilloscopeDeleted(graphView = drawing as GraphView)
        }
    }

    private fun unconnectDeletedVerticeView(verticeView: VerticeView<*>, graphView: GraphView) {
        graphView.getEdgeViews()
            .filter { ev -> ev.origin?.connectableView === verticeView }
            .forEach { ev -> connectService.unconnectEdgeViewOrigin(ev) }
        graphView.getEdgeViews()
            .filter { ev -> ev.destination?.connectableView === verticeView }
            .forEach { ev -> connectService.unconnectEdgeViewDestination(ev) }
    }

    private fun unconnectDeletedEdgeView(edgeView: EdgeView<*>) {
        connectService.unconnect(edgeView)
    }

    private fun getWrapperOf(component: Component, drawing: Drawing<*>): GraphElementViewWrapper? {
        return drawing.drawables
            .filter { it is GraphElementViewWrapper && it.component === component }
            .map { it as GraphElementViewWrapper }
            .firstOrNull()
    }

    private fun possibleWrapper(component: Component, drawing: Drawing<*>): Component {
        return getWrapperOf(component, drawing) ?: component
    }
}