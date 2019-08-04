package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory

/**
 * Abstract base implementation of an [InputEventHandler] that creates a new [EdgeView]
 * to connect [ConnectableView]s.
 *
 * Designed as a single instance being used by multiple [VerticeView]s. Therefore, determine the [VerticeView] on which
 * this [AbstractCreateEdgeViewConnector] operates by calling [useFor] before every usage.
 */
abstract class AbstractCreateEdgeViewConnector(
	private val portTypeCond: (PortType) -> Boolean,
	private val edgeViewFactorySupplier: () -> EdgeViewFactory<Any>,
	endpointType: EdgeViewEndpointType,
	allowEdgeViewAsTarget: Boolean = false
) : InputEventHandlerAdapter<EditInputEventContext>(DragEdgeViewEndpointHandler(endpointType, allowEdgeViewAsTarget)) {

	companion object {
		private val LOG by logger(AbstractCreateEdgeViewConnector::class)
	}

	/** The new [EdgeView] that is being dragged, `null` before mouse has been pressed */
	protected var edgeView: EdgeView<Any>? = null

	/** The [VerticeView] from which the new connection originates. */
	protected var startVerticeView: VerticeView<*>? = null

	/** The [PortView] in [startVerticeView] from which the new connection originates.  */
	protected var startPortView: PortView<*>? = null

	/** ---- [InputEventHandler] */

	override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
		if (locateStartPort(context)) {
			return this
		}
		exitStartPortViewIfNecessary(context)
		return null
	}

	override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
		if (!beginConnecting(context)) {
			return null
		}
		return this
	}

	override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
		if (LOG.isTraceEnabled()) {
			LOG.trace("mouseDragged to (${context.x},${context.y})")
		}
		// Forward to DragEdgeViewEndpointHandler, but keep control in order to handle mouseReleased
		if (edgeView != null) {
			super.mouseDragged(context)
		}
		return this
	}

	override fun keyPressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
		if (context.keyEvent?.key == KeyEvent.VK_ESCAPE) {
			cancel(context.editor)
		}
		return this
	}

	override fun keyReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? = this

	/** ---- [AbstractCreateEdgeViewConnector] */

	protected abstract fun connectEdgeViewToStartPort()

	/**
	 * Prepares this [AbstractConnectionPointHighlighter] to be used to created [EdgeView]s that the user
	 * starts in the specified [VerticeView].
	 */
	fun useFor(verticeView: VerticeView<*>) {
		this.startVerticeView = verticeView
	}

	/**
	 * Creates the [EdgeView] to be used for connecting and adds it to the [Drawing].
	 * Removes the [PortView] highlight.
	 *
	 * @param view the [DrawingView] to which the created [EdgeView] is added
	 * @param startPoint the [Point2D] at which the created [EdgeView] starts.
	 * @param net the [Net] model of the [EdgeView] to be created
	 */
	protected fun createEdgeView(view: DrawingView<Drawing<Component>>, startPoint: Point2D, net: Net<Any>?) {
		edgeView = if (net == null) edgeViewFactorySupplier.invoke().createEdgeView() else edgeViewFactorySupplier.invoke().createEdgeView(net)

		// Add the connection point twice so that the second point can be dragged
		edgeView!!.addSegmentPoint(startPoint)
		edgeView!!.addSegmentPoint(startPoint)

		view.drawing.add(edgeView!!)
		view.selectionManager.deselectAll()
		view.selectionManager.select(edgeView!!)
	}

	protected fun locateStartPort(context: EditInputEventContext): Boolean {
		if (startVerticeView!!.contains(context.x, context.y)) {
			val pv = startVerticeView!!.getPortViewAtConnectionPoint(context.x, context.y)
			if (pv != null && !pv.port.isConnected && portTypeCond.invoke(pv.port.portType)) {
				startPortView = pv
				if (!ConnectionPointHighlighter.hasPortViewHighlight) {
					val connPoint = startVerticeView!!.getPortConnectionPoint(startPortView!!.port)
					ConnectionPointHighlighter.displayPortViewHighlight(context.drawingView(), Point2D(connPoint))
				}
				return true
			}
		}
		return false
	}

	protected fun beginConnecting(context: EditInputEventContext): Boolean {
		if (!ConnectionPointHighlighter.hasPortViewHighlight) {
			return false
		}

		createEdgeView(context.drawingView(), startVerticeView!!.getPortConnectionPoint(startPortView!!.port), null)
		getEndpointHandler().useFor(edgeView!!)
		ConnectionPointHighlighter.removePortViewHighlight(context.drawingView())

		edgeView!!.model!!.connect(startPortView!!.port as Port<Any>)
		connectEdgeViewToStartPort()

		return true
	}

	protected fun exitStartPortViewIfNecessary(context: EditInputEventContext) {
		if (ConnectionPointHighlighter.hasPortViewHighlight) {
			ConnectionPointHighlighter.removePortViewHighlight(context.drawingView())
			startPortView = null
		}
	}

	protected open fun cancel(editor: Editor) {
		if (edgeView != null) {
			LOG.debug("creating EdgeView canceled by user")
			edgeView?.connectToOrigin(null, null)
			edgeView?.connectToDestination(null, null)
			editor.view.drawing.remove(edgeView!!)
			ConnectionPointHighlighter.removePortViewHighlight(editor.view)
			edgeView = null
		}
	}

	protected fun getEndpointHandler(): DragEdgeViewEndpointHandler {
		return successor as DragEdgeViewEndpointHandler
	}

	/**
	 * Determines whether the [EdgeView] that is being added by this [AbstractCreateEdgeViewConnector]
	 * is valid, i.e. that it exists and has a non-zero length.
	 */
	protected fun isValidEdgeView(): Boolean = edgeView != null && edgeView!!.polyline.length > 0.0
}