package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.NetView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory
import ch.scorpion.jabbah.graph.view.port.PortView

/**
 * A base class of connectors that create a new [EdgeView].
 */
abstract class AbstractCreateEdgeViewConnector(
	private val edgeViewFactory: EdgeViewFactory,
	draggedEndpointType: EdgeViewEndpointType
) : AbstractConnector(draggedEndpointType) {

	protected val isValidEdgeView: Boolean get() = edgeView != null && edgeView!!.isSufficientlyLarge

	/**
	 * Creates the [EdgeView] to be used for connecting and adds it to the [Drawing].
	 * Removes the [PortView] highlight.
	 *
	 * @param view the [DrawingView] to which the created [EdgeView] is added
	 * @param startPoint the [Point2D] at which the created [EdgeView] starts.
	 * @param netView the [NetView] of the [EdgeView] to be created
	 */
	protected fun createEdgeView(view: DrawingView<GraphView>, startPoint: Point2D, netView: NetView<Any>?) {
		(if (netView == null) edgeViewFactory.createEdgeView(view.drawing) else edgeViewFactory.createEdgeView(view.drawing, netView)).also { ev ->
			this.edgeView = ev
			ev.underConstruction = true

			ev.addSegmentPoint(startPoint)
			ev.addSegmentPoint(startPoint)

			view.drawing.add(ev)
			view.selectionManager.deselectAll()
			view.selectionManager.select(ev)
		}
	}

	protected open fun cancel(editor: Editor) {
		edgeView?.let {
			it.unconnectFromOrigin()
			it.unconnectFromDestination()
			editor.view.drawing.remove(it)
			ConnectionPointHighlighter.removePortViewHighlight()
			reset()
		}
	}
}