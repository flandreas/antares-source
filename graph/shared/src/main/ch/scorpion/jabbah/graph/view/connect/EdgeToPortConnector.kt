package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.port.PortView

/**
 * An [InputEventHandler] that splits an existing [EdgeView] by adding a [NodeView] and connecting it
 * by a new [EdgeView] with the [PortView] of another [VerticeView].
 */
class EdgeToPortConnector(
	private val connectServiceSupplier: () -> GraphViewConnectService,
	edgeViewFactorySupplier: () -> EdgeViewFactory<Any>
) : AbstractCreateEdgeViewConnector(
	portTypeCond = { true },
	edgeViewFactorySupplier = edgeViewFactorySupplier,
	endpointType = EdgeViewEndpointType.DESTINATION
) {

	companion object {
		private val LOG by logger(EdgeToPortConnector::class)
	}

	/** The [EdgeView] from which new [EdgeView]s are branched by this connector. */
	private var branchedEdgeView: EdgeView<*>? = null

	/** The index of the [EdgeView] segment at which splitting takes place.*/
	private var branchedSegmentIndex: Int? = null

	private var splitResult: SplitEdgeViewResult<*>? = null

	/** ---- [InputEventHandler] */

	private fun snap(context: EditInputEventContext): EdgeViewSnapLocatorResult? {
		return branchedEdgeView!!.snap(context.x, context.y, context.editor.view.grid)
	}

	override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
		if (LOG.isTraceEnabled()) {
			LOG.trace("mouseMoved to (${context.x},${context.y})")
		}

		val snapResult = snap(context)
		if (snapResult != null) {
			displayPortViewHighlight(context.drawingView(), snapResult.location)
			return this
		}

		if (portViewHighlight != null) {
			removePortViewHighlight(context.drawingView())
		}
		return null
	}

	override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
		LOG.trace("mousePressed at (${context.x},${context.y})")

		val snapResult = snap(context) ?: return null
		branchedSegmentIndex = snapResult.segmentIndex

		beginConnecting(context.drawingView())
		return this
	}

	override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
		if (LOG.isTraceEnabled()) {
			LOG.trace("mouseDragged to (${context.x},${context.y})")
		}
		// Forward to DragEdgeViewEndpointHandler, but keep control in order to handle mouseReleased by returning this
		super.mouseDragged(context)
		return this
	}

	override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
		super.mouseReleased(context)
		if (isValidEdgeView()) {
			completeConnecting(context)
		} else {
			cancel(context.editor)
		}
		return null
	}

	/** ---- [AbstractCreateEdgeViewConnector] */

	override fun cancel(editor: Editor) {
		if (edgeView != null) {
			val cmd = createSplitEdgeViewCommand(editor)
			cmd.registered()
			cmd.undo()
			super.cancel(editor)
		}
	}

	override fun connectEdgeViewToStartPort() {
		// not used
	}

	/** ---- [EdgeToPortConnector] */

	fun useFor(edgeView: EdgeView<*>) {
		branchedEdgeView = edgeView
	}

	private fun beginConnecting(view: DrawingView<Drawing<Component>>) {
		createEdgeView(view, Point2D(portViewHighlight!!.location), branchedEdgeView!!.model as Net<Any>)
		getEndpointHandler().useFor(edgeView!!)
		removePortViewHighlight(view)

		splitResult = connectServiceSupplier.invoke().split(
			view.drawing as GraphView<GraphElementView<*>>,
			branchedEdgeView!! as EdgeView<Any>,
			branchedSegmentIndex!!,
			edgeView!!,
			null)
	}

	private fun completeConnecting(context: EditInputEventContext) {
		if (getEndpointHandler().targetPortView != null) {
			connectServiceSupplier.invoke().connectToDestination(
				edgeView!!,
				getEndpointHandler().targetPortView!!.owner!!,
				getEndpointHandler().targetPortView!!.port as Port<Any>)
		}

		context.editor.commandManager.register(createSplitEdgeViewCommand(context.editor))
	}

	private fun createSplitEdgeViewCommand(editor: Editor): Command {
		return SplitEdgeViewCommand(
			editor = editor,
			connectService = connectServiceSupplier.invoke(),
			graphView = editor.view.drawing as GraphView<GraphElementView<*>>,
			origEdgeView = branchedEdgeView!!,
			segmentIndex = branchedSegmentIndex!!,
			newEdgeView = edgeView!!,
			targetPortView = getEndpointHandler().targetPortView,
			nodeView = splitResult!!.nodeView)
	}
}