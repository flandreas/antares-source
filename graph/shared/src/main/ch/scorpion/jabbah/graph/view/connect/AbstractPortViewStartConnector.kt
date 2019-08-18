package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.state.stateMachine
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseDragged
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseMoved
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mousePressed
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseReleased
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory
import ch.scorpion.jabbah.graph.view.port.PortView

/**
 * A base class for building connectors used for starting new [EdgeView]s on a [VerticeView]'s [PortView].
 */
abstract class AbstractPortViewStartConnector(
	private val portTypeCond: (PortType) -> Boolean,
	protected val connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService,
	edgeViewFactory: EdgeViewFactory<Any>,
	draggedEndpointType: EdgeViewEndpointType,
	private val allowEdgeViewAsTarget: Boolean = false
) : AbstractCreateEdgeViewConnector(edgeViewFactory, draggedEndpointType) {

	/** The [VerticeView] from which the new connection originates. */
	private var startVerticeView: VerticeView<*>? = null

	/** The [PortView] in [startVerticeView] from which the new connection originates.  */
	protected var startPortView: PortView<*>? = null
		private set

	/** The found target [EdgeView], if any. */
	protected var targetEdgeView: EdgeView<*>? = null

	protected var targetEdgeViewSegmentIndex: Int? = null

	override val handler = StateMachineInputEventHandler(

		stateMachine<EditInputEventContext>(strict = false) {

			state("sense") {
				onEntry { it?.view?.setCursor(Cursor.DEFAULT) }
				transitTo("insideStart") {
					given { mouseMoved(it) && insideStartPortView(it.location) }
				}
			}

			state("insideStart") {
				onEntry { displayPortViewHighlight(it!!) }
				onExit { removePortViewHighlight(it!!) }
				transitTo("insideStart") {
					given { mouseMoved(it) && insideStartPortView(it.location)}
				}
				transitTo("sense") {
					given { mouseMoved(it) && !insideStartPortView(it.location) }
				}
				transitTo("drag") {
					given { mousePressed(it) }
					onTransit { beginConnecting(it!!) }
				}
			}

			state("drag") {
				transitTo("insideTargetPortView") {
					given { mouseDragged(it) && insideTargetPortView(draggedEndpointType, it) }
				}
				transitTo("insideTargetEdgeView") {
					given { mouseDragged(it) && allowEdgeViewAsTarget && insideTargetEdgeView(it) }
				}
				transitTo("drag") {
					given { mouseDragged(it) && !insideTargetPortView(draggedEndpointType, it) && (!allowEdgeViewAsTarget || !insideTargetEdgeView(it)) }
					onTransit { moveEdgeViewEndpoint(it!!) }
				}
				transitTo("connected") {
					given { mouseReleased(it) && isValidEdgeView }
				}
				transitTo("cancelled") {
					given { mouseReleased(it) && !isValidEdgeView }
				}
				transitTo("cancelled") {
					given { cancelled(it) }
				}
			}

			state("insideTargetPortView") {
				onEntry { snapToTargetPortView(it!!) }
				onExit { removePortViewHighlight(it!!) }
				transitTo("insideTargetPortView") {
					given { mouseDragged(it) && insideTargetPortView(draggedEndpointType, it) }
				}
				transitTo("drag") {
					given { mouseDragged(it) && !insideTargetPortView(draggedEndpointType, it) }
				}
				transitTo("connected") {
					given { mouseReleased(it) }
				}
				transitTo("cancelled") {
					given { cancelled(it) }
				}
			}

			state("insideTargetEdgeView") {
				onEntry { snapToTargetEdgeView(it!!) }
				onExit { removePortViewHighlight(it!!) }
				transitTo("insideTargetEdgeView") {
					given { mouseDragged(it) && insideTargetEdgeView(it) }
					onTransit { snapToTargetEdgeView(it!!) }
				}
				transitTo("drag") {
					given { mouseDragged(it) && !insideTargetEdgeView(it) }
				}
				transitTo("connectedToEdge") {
					given { mouseReleased(it) }
				}
				transitTo("cancelled") {
					given { cancelled(it) }
				}
			}

			state("connected") {
				onEntry {
					completeConnectingToEndPort(it!!)
					reset()
				}
			}

			state("connectedToEdge") {
				onEntry {
					completeConnectingToEdge(it!!)
					reset()
				}
			}

			state("cancelled") {
				onEntry { cancel(it!!.editor) }
			}
		}
	)

	protected abstract fun connectEdgeViewToStartPort()

	protected abstract fun completeConnectingToEndPort(context: EditInputEventContext)

	/**
	 * Prepares this [AbstractPortViewStartConnector] to be used to create [EdgeView]s that the user
	 * starts in the specified [VerticeView].
	 */
	fun useFor(verticeView: VerticeView<*>) {
		reset()
		startVerticeView = verticeView
		handler.sm.start()
	}

	override fun reset() {
		super.reset()
		startVerticeView = null
		startPortView = null
	}

	private fun insideStartPortView(location: Point2D): Boolean {
		val pv = startVerticeView!!.getPortViewAtConnectionPoint(location)
		if (pv != null && !pv.port.isConnected && portTypeCond.invoke(pv.port.portType)) {
			startPortView = pv
		}
		else {
			startPortView = null
		}
		return startPortView != null
	}

	private fun displayPortViewHighlight(context: EditInputEventContext) {
		displayPortViewHighlight(context, startVerticeView!!.getPortConnectionPoint(startPortView!!.port))
	}

	private fun beginConnecting(context: EditInputEventContext) {
		createEdgeView(context.drawingView(), startVerticeView!!.getPortConnectionPoint(startPortView!!.port), null)
		edgeView!!.model!!.connect(startPortView!!.port as Port<Any>)
		connectEdgeViewToStartPort()
	}

	private fun insideTargetEdgeView(context: EditInputEventContext): Boolean {
		val destDrawable = context.drawingView().drawing.getDrawable { it !== edgeView && it.contains(context.location)}
		if (destDrawable == null || destDrawable !is EdgeView<*>) {
			clearTargetEdgeView()
			return false
		}

		clearTargetPortView()
		targetEdgeView = destDrawable

		return true
	}

	private fun clearTargetEdgeView() {
		targetEdgeView = null
	}

	private fun snapToTargetEdgeView(context: EditInputEventContext) {
		targetEdgeView!!.snap(context.x, context.y, context.editor.view.grid)?.let { snapResult ->
			targetEdgeViewSegmentIndex = snapResult.segmentIndex
			ConnectionPointHighlighter.displayPortViewHighlight(context.drawingView(), snapResult.location)
			draggedEndpointType.moveTo(edgeView!!, snapResult.location)
			draggedEndpointType.layout(edgeView!!, null)
			edgeView!!.layout
		}
	}

	private fun completeConnectingToEdge(context: EditInputEventContext) {
		context.drawingView().drawing.remove(edgeView!!)

		context.editor.commandManager.execute(
			SplitEdgeViewCommand(
				editor = context.editor,
				connectService = connectService,
				graphView = context.editor.drawing as GraphView<GraphElementView<*>>,
				origEdgeView = targetEdgeView!!,
				segmentIndex = targetEdgeViewSegmentIndex!!,
				newEdgeView = edgeView!!,
				targetPortView = startPortView!!,
				nodeView = null
			)
		)
	}
}