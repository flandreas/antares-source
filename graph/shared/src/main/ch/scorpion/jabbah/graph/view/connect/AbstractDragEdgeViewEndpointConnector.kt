package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.state.UnhandledEventBehaviour.Unhandled
import ch.scorpion.jabbah.base.state.stateMachine
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.altReleased
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseDragged
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseLeftPressed
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseLeftReleased
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseMoved
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.escapePressed
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.view.EdgeView
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

	companion object {
		private val LOG by logger(AbstractDragEdgeViewEndpointConnector::class)
	}

	/** The location where dragging started. */
	protected var oldLocation = Point2D.ZERO

	override val handler = StateMachineInputEventHandler(

		stateMachine<EditInputEventContext>(Unhandled) {

			state("sense") {
				transitTo("insideStart") {
					given { mouseMoved(it) && insideStart(it.location)}
				}
			}

			state("insideStart") {
				onEntry { displayPortViewHighlight(it) }
				onExit { removePortViewHighlight() }
				transitTo("insideStart") {
					given { mouseMoved(it) && insideStart(it.location) }
				}
				transitTo("sense") {
					given { mouseMoved(it) && !insideStart(it.location) }
				}
				transitTo("drag") {
					given { mouseLeftPressed(it) }
					onTransit { beginDragging(it) }
				}
				transitTo("sense") {
					given { altReleased(it) }
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
					given { mouseDragged(it) && !insideTargetPortView(draggedEndpointType, it)}
					onTransit { moveEdgeViewEndpoint(it) }
				}
				transitTo("draggedOpen") {
					given { mouseLeftReleased(it) }
				}
				transitTo("cancelled") {
					given { escapePressed(it) }
				}
				transitTo("drag") {
					given { altReleased(it) }
				}
			}

			state("insideTargetPortView") {
				onEntry { snapToTargetPortView(it) }
				onExit { removePortViewHighlight() }
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
				onExit { removePortViewHighlight() }
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

			state("draggedOpen") {
				onEntry {
					edgeView?.underConstruction = false
					removePortViewHighlight()
					completeDragOpen(it)
					reset()
				}
			}

			state("connected") {
				onEntry {
					edgeView?.underConstruction = false
					removePortViewHighlight()
					completeDragConnecting(it)
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

	override fun canConnectTo(type: EdgeViewEndpointType, edgeView: EdgeView<out Any>): Boolean =
		type.canConnectTo(edgeView.net!!, edgeView.getConnection(type.opposite)?.port)

	protected fun getEndpointView(): EdgeEndpointView {
		return draggedEndpointType.getEndpoint(edgeView!!)
	}

	protected open fun displayPortViewHighlight(context: EditInputEventContext) {
		ConnectionPointHighlighter.displayPortViewHighlight(context.drawingView(), getEndpointView().location)
	}

	protected fun insideStart(location: Point2D): Boolean {
		return draggedEndpointType.getEndpoint(edgeView!!).contains(location)
	}

	protected open fun beginDragging(context: EditInputEventContext) {
		edgeView?.underConstruction = true
		oldLocation = getEndpointView().location
		context.drawingView().selectionManager.deselectAll()
		context.drawingView().selectionManager.select(edgeView!!)
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
			targetPortId = null,
			joinNetViews = true
		))
	}
}