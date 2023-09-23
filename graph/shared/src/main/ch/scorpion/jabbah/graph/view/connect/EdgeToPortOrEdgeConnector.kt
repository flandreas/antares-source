package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.event.Modifier
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.state.UnhandledEventBehaviour.Unhandled
import ch.scorpion.jabbah.base.state.stateMachine
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseDragged
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseMoved
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseLeftPressed
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseLeftReleased
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.escapePressed
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.EdgeViewSnapLocatorResult
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.NetView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory

class EdgeToPortOrEdgeConnector(
	private val connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService,
	edgeViewFactory: EdgeViewFactory = GraphViewModule.getEdgeViewFactory()
) : AbstractCreateEdgeViewConnector(
	edgeViewFactory = edgeViewFactory,
	draggedEndpointType = EdgeViewEndpointType.DESTINATION
) {

	companion object {
		val SPLIT_EDGE_VIEW_MODIFIER = Modifier.Alt
		private val LOG by logger(EdgeToPortOrEdgeConnector::class)
	}

	/** The [EdgeView] from which a new [EdgeView] is branched by this connector. */
	private var branchedEdgeView: EdgeView<*>? = null

	/** The index of the segment in [branchedEdgeView] at which splitting takes place.*/
	private var branchedSegmentIndex: Int? = null

	override val handler = StateMachineInputEventHandler(

		stateMachine<EditInputEventContext>(Unhandled) {

			ignoreEvent { it.keyEvent != null }

			state("sense") {
				onEntry { it.view.setCursor(Cursor.DEFAULT) }
				transitTo("insideEdge") {
					given { mouseMoved(it) && snap(it) != null }
				}
			}

			state("insideEdge") {
				onEntry { displayPortViewHighlight(it, snap(it)!!.location) }
				transitTo("insideEdge") {
					given { mouseMoved(it) && snap(it) != null }
					onTransit { displayPortViewHighlight(it, snap(it)!!.location) }
				}
				transitTo("sense") {
					given { mouseMoved(it) && snap(it) == null }
					onTransit { removePortViewHighlight(it) }
				}
				transitTo("drag") {
					given { mouseLeftPressed(it) }
					onTransit {
						beginConnecting(it)
						removePortViewHighlight(it)
					}
				}
			}

			state("drag") {
				transitTo("insideTargetPortView") {
					given { mouseDragged(it) && insideTargetPortView(draggedEndpointType, it) }
				}
				transitTo("insideTargetEdgeView") {
					given { mouseDragged(it) && insideTargetEdgeView(draggedEndpointType, it) }
				}
				transitTo("drag") {
					given { mouseDragged(it) && !insideTargetPortView(draggedEndpointType, it) }
					onTransit { moveEdgeViewEndpoint(it) }
				}
				transitTo("connected") {
					given { mouseLeftReleased(it) && isValidEdgeView }
				}
				transitTo("cancelled") {
					given { mouseLeftReleased(it) && !isValidEdgeView }
				}
				transitTo("cancelled") {
					given { escapePressed(it) }
				}
			}

			// This is exactly the same code as in AbstractPortViewStartConnector. However, if we would use
			// a common State builder for this State, we would loose the insight in the entire StateMachine here.
			state("insideTargetPortView") {
				onEntry { snapToTargetPortView(it) }
				onExit { removePortViewHighlight(it) }
				transitTo("insideTargetPortView") {
					given { mouseDragged(it) && insideTargetPortView(draggedEndpointType, it) }
				}
				transitTo("drag") {
					given { mouseDragged(it) && !insideTargetPortView(draggedEndpointType, it) }
				}
				transitTo("connected") {
					given { mouseLeftReleased(it) }
				}
				transitTo("cancelled") {
					given { escapePressed(it) }
				}
			}

			state("insideTargetEdgeView") {
				onEntry { snapToTargetEdgeView(it) }
				onExit { removePortViewHighlight(it) }
				stayIf({ mouseDragged(it) && insideTargetEdgeView(draggedEndpointType, it) }) {
					onTransit { snapToTargetEdgeView(it) }
				}
				transitTo("drag") {
					given { mouseDragged(it) && !insideTargetEdgeView(draggedEndpointType, it) }
				}
				transitTo("connectedToEdge") {
					given { mouseLeftReleased(it) }
				}
				transitTo("cancelled") {
					given { escapePressed(it) }
				}
				stayOtherwise()
			}

			state("connected") {
				onEntry {
					completeConnecting(it)
					reset()
				}
			}

			state("connectedToEdge") {
				onEntry {
					edgeView?.underConstruction = false
					completeConnectingToEdgeView(it)
					reset()
				}
			}

			state("cancelled") {
				onEntry { cancel(it.editor) }
			}
		}
	)

	fun useFor(edgeView: EdgeView<*>, context: EditInputEventContext) {
		reset()
		branchedEdgeView = edgeView
		handler.sm.start(context)
	}

	private fun snap(context: EditInputEventContext): EdgeViewSnapLocatorResult? {
		val result = branchedEdgeView!!.snap(context.x, context.y, context.editor.snapManager)
		branchedSegmentIndex = result?.segmentIndex
		return result
	}

	private fun beginConnecting(context: EditInputEventContext) {
		createEdgeView(context.drawingView as DrawingView<GraphView>, Point2D(ConnectionPointHighlighter.portViewHighlight!!.location), branchedEdgeView!!.netView as NetView<Any>)
		LOG.userTrail("Start creating junction of EdgeView ${edgeView?.id}")
		context.drawingView.drawing.remove(edgeView!!)
		removePortViewHighlight(context)

		val command = createSplitEdgeViewCommand(context.editor)
		context.editor.commandManager.beginTransaction(command)

		edgeView = command.addedNewEdgeView
		edgeView!!.underConstruction = true

		context.drawingView.selectionManager.deselectAll()
		context.drawingView.selectionManager.select(edgeView!!)
	}

	override fun cancel(editor: Editor) {
		editor.commandManager.rollbackTransaction()
		ConnectionPointHighlighter.removePortViewHighlight()
		reset()
	}

	private fun logConnect() {
		if (targetPortView != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Create junction from ${EdgeViewEndpointType.ORIGIN.getLocation(edgeView!!)} to ${targetPortView?.owner?.getUnconnectedPortConnectionPoint(targetPortView!!.port)}")
			}
			LOG.userTrail("Create junction from EdgeView ${branchedEdgeView?.id} to port ${targetPortView?.port?.portId} of ${targetPortView?.owner?.id}")
		} else if (targetEdgeView != null) {
			LOG.userTrail("Create junction from EdgeView ${branchedEdgeView?.id} to new junction in EdgeView ${targetEdgeView?.id}")
		} else {
			LOG.userTrail("Create junction from EdgeView ${branchedEdgeView?.id} open-ended")
		}
	}

	private fun completeConnecting(context: EditInputEventContext) {
		logConnect()

		edgeView?.underConstruction = false

		if (targetPortView != null) {
			context.editor.commandManager.execute(createConnectDestinationCommand(context.editor))
		} else {
			context.editor.commandManager.register(createMoveDestinationCommand(context.editor))
		}

		context.editor.commandManager.commitTransaction()
	}

	private fun completeConnectingToEdgeView(context: EditInputEventContext) {
		logConnect()

		edgeView?.underConstruction = false
		context.editor.commandManager.execute(createSplitEdgeViewToEdgeViewCommand(context.editor))
		context.editor.commandManager.commitTransaction()
	}

	private fun createSplitEdgeViewToEdgeViewCommand(editor: Editor): Command {
		return SplitEdgeViewCommand(
			editor,
			connectService = connectService,
			splitEdgeViewId = targetEdgeView!!.id,
			segmentIndex = targetEdgeViewSegmentIndex!!,
			splitLocation = EdgeViewEndpointType.DESTINATION.getLocation(edgeView!!),
			newEdgeViewProvider = NewEdgeViewAtSplitRetrieveProvider(editor, edgeView!!.id),
			newEdgeViewEndpointType = EdgeViewEndpointType.DESTINATION,
			targetConnectableViewId = null,
			targetPortId = null
		)
	}

	private fun createSplitEdgeViewCommand(editor: Editor): SplitEdgeViewCommand {
		return SplitEdgeViewCommand(
			editor = editor,
			connectService = connectService,
			splitEdgeViewId = branchedEdgeView!!.id,
			segmentIndex = branchedSegmentIndex!!,
			splitLocation = EdgeViewEndpointType.ORIGIN.getLocation(edgeView!!),
			newEdgeViewProvider = NewEdgeViewAtSplitCloneProvider(edgeView!!),
			newEdgeViewEndpointType = EdgeViewEndpointType.ORIGIN,
			targetConnectableViewId = null,
			targetPortId = null)
	}

	private fun createConnectDestinationCommand(editor: Editor): Command {
		return ConnectDestinationCommand(
			editor = editor,
			service = connectService,
			edgeViewId = edgeView!!.id,
			destConnectableViewId = targetPortView!!.owner!!.id,
			destPortId = targetPortView!!.port.portId)
	}

	private fun createMoveDestinationCommand(editor: Editor): Command {
		return MoveDestinationEndpointCommand(
			editor = editor,
			edgeViewId = edgeView!!.id,
			oldLocation = edgeView!!.polyline.getFirstPoint(),
			newLocation = edgeView!!.polyline.getLastPoint())
	}
}