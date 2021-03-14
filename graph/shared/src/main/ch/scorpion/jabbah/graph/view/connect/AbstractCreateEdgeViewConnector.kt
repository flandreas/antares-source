package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory
import ch.scorpion.jabbah.graph.view.port.PortView

/**
 * A base class of connectors that create a new [EdgeView].
 */
abstract class AbstractCreateEdgeViewConnector(
	private val edgeViewFactory: EdgeViewFactory<Any>,
	draggedEndpointType: EdgeViewEndpointType
) : AbstractConnector(draggedEndpointType) {

	protected val isValidEdgeView: Boolean get() = edgeView != null && edgeView!!.isSufficientlyLarge

	/**
	 * Creates the [EdgeView] to be used for connecting and adds it to the [Drawing].
	 * Removes the [PortView] highlight.
	 *
	 * @param view the [DrawingView] to which the created [EdgeView] is added
	 * @param startPoint the [Point2D] at which the created [EdgeView] starts.
	 * @param net the [Net] model of the [EdgeView] to be created
	 */
	protected fun createEdgeView(view: DrawingView<Drawing<Component>>, startPoint: Point2D, net: Net<Any>?) {
		edgeView = if (net == null) edgeViewFactory.createEdgeView() else edgeViewFactory.createEdgeView(net)

		// Add the connection point twice so that the second point can be dragged
		edgeView!!.addSegmentPoint(startPoint)
		edgeView!!.addSegmentPoint(startPoint)

		view.drawing.add(edgeView!!)
		view.selectionManager.deselectAll()
		view.selectionManager.select(edgeView!!)
	}

	protected open fun cancel(editor: Editor) {
		edgeView?.let {
			it.unconnectFromOrigin()
			it.unconnectFromDestination()
			editor.view.drawing.remove(it)
			ConnectionPointHighlighter.removePortViewHighlight(editor.view)
			reset()
		}
	}
}