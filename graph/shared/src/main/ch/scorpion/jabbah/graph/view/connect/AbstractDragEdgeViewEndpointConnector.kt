package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.state.UnhandledEventBehaviour.Unhandled
import ch.scorpion.jabbah.base.state.stateMachine
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeEndpointView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.port.PortView

/**
 * A base class for building connectors that drag a [EdgeEndpointView] towards a
 * target [PortView] or leave the edited [EdgeView] open-ended.
 */
abstract class AbstractDragEdgeViewEndpointConnector(
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
				onEntry { it?.view?.setCursor(Cursor.DEFAULT) }
				transitTo("insideStart") {
					given { StateMachineInputEventHandler.mouseMoved(it) && insideStart(it.location)}
				}
			}

			state("insideStart") {
				onEntry { displayPortViewHighlight(it!!) }
				onExit { removePortViewHighlight(it!!) }
				transitTo("insideStart") {
					given { StateMachineInputEventHandler.mouseMoved(it) && insideStart(it.location) }
				}
				transitTo("sense") {
					given { StateMachineInputEventHandler.mouseMoved(it) && !insideStart(it.location) }
				}
				transitTo("drag") {
					given { StateMachineInputEventHandler.mousePressed(it) }
					onTransit { beginDragging(it!!) }
				}
			}

			state("drag") {
				transitTo("insideTargetPortView") {
					given { StateMachineInputEventHandler.mouseDragged(it) && insideTargetPortView(draggedEndpointType, it) }
				}
				transitTo("drag") {
					given { StateMachineInputEventHandler.mouseDragged(it) && !insideTargetPortView(draggedEndpointType, it)}
					onTransit { moveEdgeViewEndpoint(it!!) }
				}
				transitTo("draggedOpen") {
					given { StateMachineInputEventHandler.mouseReleased(it) }
				}
				transitTo("cancelled") {
					given { escapePressed(it) }
				}
			}

			state("insideTargetPortView") {
				onEntry { snapToTargetPortView(it!!) }
				onExit { removePortViewHighlight(it!!) }
				transitTo("insideTargetPortView") {
					given { StateMachineInputEventHandler.mouseDragged(it) && insideTargetPortView(draggedEndpointType, it) }
				}
				transitTo("drag") {
					given { StateMachineInputEventHandler.mouseDragged(it) && !insideTargetPortView(draggedEndpointType, it) }
				}
				transitTo("connected") {
					given { StateMachineInputEventHandler.mouseReleased(it) }
				}
				transitTo("cancelled") {
					given { escapePressed(it) }
				}
			}

			state("draggedOpen") {
				onEntry {
					removePortViewHighlight(it!!)
					completeDragOpen(it)
					reset()
				}
			}

			state("connected") {
				onEntry {
					removePortViewHighlight(it!!)
					completeDragConnecting(it)
					reset()
				}
			}

			state("cancelled") {
				onEntry {
					it!!.view.setCursor(Cursor.DEFAULT)
					cancel(it.editor)
				}
			}
		}
	)

	abstract fun completeDragOpen(context: EditInputEventContext)

	abstract fun completeDragConnecting(context: EditInputEventContext)

	abstract fun cancel(editor: Editor)

	fun useFor(edgeView: EdgeView<*>) {
		this.edgeView = edgeView as EdgeView<Any>
		handler.sm.start()
	}

	// For testing
	val usedFor: EdgeView<*>? get() = this.edgeView

	protected fun getEndpointView(): EdgeEndpointView {
		return draggedEndpointType.getEndpoint(edgeView!!)
	}

	protected fun displayPortViewHighlight(context: EditInputEventContext) {
		ConnectionPointHighlighter.displayPortViewHighlight(context.drawingView(), getEndpointView().location)
	}

	protected fun insideStart(location: Point2D): Boolean {
		return draggedEndpointType.getEndpoint(edgeView!!).contains(location)
	}

	protected open fun beginDragging(context: EditInputEventContext) {
		oldLocation = getEndpointView().location
		context.drawingView().selectionManager.deselectAll()
		context.drawingView().selectionManager.select(edgeView!!)
	}
}