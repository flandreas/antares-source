package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.state.UnhandledEventBehaviour.Unhandled
import ch.scorpion.jabbah.base.state.stateMachine
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.altReleased
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.escapePressed
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseDragged
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseLeftPressed
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseLeftReleased
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseMoved
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeEndpointView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.port.PortView

/**
 * A base class for building connectors that drag a [EdgeEndpointView] towards a
 * target [PortView] or leave the edited [EdgeView] open-ended.
 */
abstract class AbstractDragEdgeViewEndpointConnector(
	protected val connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService,
	draggedEndpointType: EdgeViewEndpointType
) : AbstractConnector(draggedEndpointType) {

	@Suppress("ConstPropertyName")
	companion object {
		private val LOG by logger(AbstractDragEdgeViewEndpointConnector::class)

		private const val sense = "sense"
		private const val drag = "drag"
		private const val insideStart = "insideStart"
		private const val insideTargetPortView = "insideTargetPortView"
		private const val insideTargetEndpoint = "insideTargetEndpoint"
		private const val insideTargetEdgeView = "insideTargetEdgeView"
		private const val insideDenyingPortView = "insideDenyingPortView"
		private const val insideDenyingEdgeView = "insideDenyingEdgeView"
		private const val draggedOpen = "draggedOpen"
		private const val cancelled = "cancelled"
		private const val connected = "connected"
		private const val connectedToEdge = "connectedToEdge"
		private const val connectedToEndpoint = "connectedToEndpoint"
	}

	/** The location where dragging started. */
	protected var oldLocation = Point2D.ZERO

	private var targetEndpointView: EdgeEndpointView? = null

	override fun reset() {
		super.reset()
		targetEndpointView = null
	}

	override val handler = StateMachineInputEventHandler(

		stateMachine<EditInputEventContext>(Unhandled) {

			state(sense) {
				transitTo(insideStart) {
					given { mouseMoved(it) && insideStart(it.location)}
				}
			}

			state(insideStart) {
				onEntry { displayPortViewHighlight(it) }
				onExit { removePortViewHighlight(it) }
				transitTo(insideStart) {
					given { mouseMoved(it) && insideStart(it.location) }
				}
				transitTo(sense) {
					given { mouseMoved(it) && !insideStart(it.location) }
				}
				transitTo(drag) {
					given { mouseLeftPressed(it) }
					onTransit { beginDragging(it) }
				}
				transitTo(sense) {
					given { altReleased(it) }
				}
			}

			state(drag) {
				transitTo(insideTargetPortView) {
					given { mouseDragged(it) && insideTargetPortView(draggedEndpointType, it) }
				}
				transitTo(insideTargetEndpoint) {
					given { mouseDragged(it) && insideTargetEndpoint(it) }
				}
				transitTo(insideTargetEdgeView) {
					given { mouseDragged(it) && insideTargetEdgeView(draggedEndpointType, it) }
				}
				transitTo(insideDenyingPortView) {
					given { mouseDragged(it) && insideDenyingPortView(draggedEndpointType, it) }
				}
				transitTo(insideDenyingEdgeView) {
					given { mouseDragged(it) && insideDenyingEdgeView(draggedEndpointType, it) }
				}
				transitTo(drag) {
					given { mouseDragged(it) && !insideTargetPortView(draggedEndpointType, it)}
					onTransit { moveEdgeViewEndpoint(it) }
				}
				transitTo(draggedOpen) {
					given { mouseLeftReleased(it) }
				}
				transitTo(cancelled) {
					given { escapePressed(it) }
				}
				transitTo(drag) {
					given { altReleased(it) }
				}
				stayOtherwise()
			}

			state(insideTargetPortView) {
				onEntry { snapToTargetPortView(it) }
				onExit { removePortViewHighlight(it) }
				transitTo(insideTargetPortView) {
					given { mouseDragged(it) && insideTargetPortView(draggedEndpointType, it) }
					onTransit {
						snapToTargetPortView(it)
					}
				}
				transitTo(drag) {
					given { mouseDragged(it) && !insideTargetPortView(draggedEndpointType, it) }
				}
				transitTo(connected) {
					given { mouseLeftReleased(it) }
				}
				transitTo(cancelled) {
					given { escapePressed(it) }
				}
			}

			state(insideDenyingPortView) {
				onEntry { snapToDenyingPortView(it) }
				onExit { removePortViewHighlight(it) }
				transitTo(insideDenyingPortView) {
					given { mouseDragged(it) && insideDenyingPortView(draggedEndpointType, it) }
					onTransit {
						snapToDenyingPortView(it)
					}
				}
				transitTo(drag) {
					given { mouseDragged(it) && !insideDenyingPortView(draggedEndpointType, it) }
				}
				transitTo(cancelled) {
					given { mouseLeftReleased(it) }
				}
				transitTo(cancelled) {
					given { escapePressed(it) }
				}
			}

			state(insideTargetEdgeView) {
				onEntry { snapToTargetEdgeView(it) }
				onExit { removePortViewHighlight(it) }
				stayIf({ mouseDragged(it) && insideTargetEdgeView(draggedEndpointType, it) }) {
					onTransit { snapToTargetEdgeView(it) }
				}
				transitTo(drag) {
					given { mouseDragged(it) && !insideTargetEdgeView(draggedEndpointType, it) }
				}
				transitTo(connectedToEdge) {
					given { mouseLeftReleased(it) }
				}
				transitTo(cancelled) {
					given { escapePressed(it) }
				}
				stayOtherwise()
			}

			state(insideDenyingEdgeView) {
				onEntry { snapToDenyingEdgeView(it) }
				onExit { removePortViewHighlight(it) }
				transitTo(drag) {
					given { mouseDragged(it) && !insideDenyingEdgeView(draggedEndpointType, it) }
				}
				transitTo(cancelled) {
					given { mouseLeftReleased(it) }
				}
				transitTo(cancelled) {
					given { escapePressed(it) }
				}
				stayOtherwise {
					onTransit { snapToDenyingEdgeView(it) }
				}
			}

			state(insideTargetEndpoint) {
				onEntry { snapToTargetEndpointView(it) }
				onExit { removePortViewHighlight(it) }
				stayIf({ mouseDragged(it) && insideTargetEndpoint(it) }) {
					onTransit { snapToTargetEndpointView(it) }
				}
				transitTo(drag) {
					given { mouseDragged(it) && !insideTargetEndpoint(it) }
				}
				transitTo(connectedToEndpoint) {
					given { mouseLeftReleased(it) }
				}
				stayOtherwise()
			}

			state(draggedOpen) {
				onEntry {
					edgeView?.underConstruction = false
					removePortViewHighlight(it)
					completeDragOpen(it)
					reset()
				}
			}

			state(connected) {
				onEntry {
					edgeView?.underConstruction = false
					removePortViewHighlight(it)
					completeDragConnecting(it)
					reset()
				}
			}

			state(connectedToEdge) {
				onEntry {
					edgeView?.underConstruction = false
					completeConnectingToEdgeView(it)
					reset()
				}
			}

			state(connectedToEndpoint) {
				onEntry {
					removePortViewHighlight(it)
					completeConnectingToEndpoint(it)
					reset()
				}
			}

			state(cancelled) {
				onEntry {
					it.view.setCursor(Cursor.DEFAULT)
					cancel(it.editor)
				}
			}
		}
	)

	abstract fun completeDragOpen(context: EditInputEventContext)

	abstract fun completeDragConnecting(context: EditInputEventContext)

	abstract fun cancel(editor: Editor)

	fun useFor(edgeView: EdgeView<*>, context: EditInputEventContext) {
		this.edgeView = edgeView as EdgeView<Any>
		handler.sm.start(context)
	}

	// For testing
	val usedFor: EdgeView<*>? get() = this.edgeView

	override fun canConnectTo(type: EdgeViewEndpointType, edgeView: EdgeView<out Any>, graphView: GraphView): Boolean =
		type.canConnectTo(edgeView.net!!, edgeView.getConnection(type.opposite)?.port, graphView)

	protected fun getEndpointView(): EdgeEndpointView {
		return draggedEndpointType.getEndpoint(edgeView!!)
	}

	protected open fun displayPortViewHighlight(context: EditInputEventContext) {
		ConnectionPointHighlighter.displayPortViewHighlight(context.drawingView, getEndpointView().location)
	}

	private fun insideStart(location: Point2D): Boolean {
		return draggedEndpointType.getEndpoint(edgeView!!).contains(location)
	}

	private fun insideTargetEndpoint(context: EditInputEventContext): Boolean {
		val edgeView = context.drawingView.drawing.getDrawable {
			it is EdgeView<*> &&
				(it.getOpenEndpointView(draggedEndpointType.opposite)?.let {
					endpointView -> endpointView.contains(context.location)
				} ?: false)
		}

		if (edgeView != null) {
			targetEndpointView = (edgeView as EdgeView<*>).getOpenEndpointView(draggedEndpointType.opposite)
			return true
		}

		return false
	}

	private fun snapToTargetEndpointView(context: EditInputEventContext) {
		val location = targetEndpointView!!.location
		ConnectionPointHighlighter.displayPortViewHighlight(context.drawingView, location)
		draggedEndpointType.moveTo(edgeView!!, location)
		draggedEndpointType.layout(edgeView!!, null)
	}

	protected open fun beginDragging(context: EditInputEventContext) {
		edgeView?.underConstruction = true
		oldLocation = getEndpointView().location
		context.drawingView.selectionManager.deselectAll()
		context.drawingView.selectionManager.select(edgeView!!)
	}

	private fun completeConnectingToEdgeView(context: EditInputEventContext) {
		if (edgeView == null) {
			LOG.warn("Illegal State: edgeView in completeConnectingToEdgeView is null")
			cancel(context.editor)
			return
		}

		if (targetEdgeView == null || targetEdgeViewSegmentIndex == null) {
			cancel(context.editor)
			return
		}

		context.editor.commandManager.execute(SplitEdgeViewCommand(
			editor = context.editor,
			baseKey = "graph.command.combineEdgeViews",
			connectService = connectService,
			splitEdgeViewId = targetEdgeView!!.id,
			splitLocation = draggedEndpointType.getLocation(edgeView!!),
			segmentIndex = targetEdgeViewSegmentIndex!!,
			newEdgeViewProvider = NewEdgeViewAtSplitRetrieveProvider(context.editor, edgeView!!.id),
			newEdgeViewEndpointType = draggedEndpointType,
			targetConnectableViewId = null,
			targetPortId = null
		))
	}

	private fun completeConnectingToEndpoint(context: EditInputEventContext) {
		if (targetEndpointView == null) {
			cancel(context.editor)
			return
		}
		val targetEndpointType = targetEndpointView!!.edgeView.getEndpointType(targetEndpointView!!)
		if (targetEndpointType == null) {
			cancel(context.editor)
			return
		}

		val type = when (draggedEndpointType) {
            EdgeViewEndpointType.ORIGIN -> "Origin"
            EdgeViewEndpointType.DESTINATION -> "Destination"
        }
		LOG.userTrail("Join EdgeView ${edgeView!!.id} ($type) with EdgeView ${targetEndpointView!!.edgeView.id} at ${targetEndpointView!!.location}")

		context.editor.commandManager.execute(JoinEdgeViewEndpointsCommand<Any>(
			context.editor,
			connectService,
			edgeView!!.id,
			draggedEndpointType,
			targetEndpointView!!.edgeView.id,
			targetEndpointType
		))
	}
}