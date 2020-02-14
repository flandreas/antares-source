package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.state.UnhandledEventBehaviour.Unhandled
import ch.scorpion.jabbah.base.state.stateMachine
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseDragged
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseMoved
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mousePressed
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseReleased
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory

class EdgeToPortConnector(
	private val connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService,
	edgeViewFactory: EdgeViewFactory<Any> = GraphViewModule.getEdgeViewFactory()
) : AbstractCreateEdgeViewConnector(
	edgeViewFactory = edgeViewFactory,
	draggedEndpointType = EdgeViewEndpointType.DESTINATION
) {

	/** The [EdgeView] from which a new [EdgeView] is branched by this connector. */
	private var branchedEdgeView: EdgeView<*>? = null

	/** The index of the segment in [branchedEdgeView] at which splitting takes place.*/
	private var branchedSegmentIndex: Int? = null

	private var splitCommand: Command? = null

	override val handler = StateMachineInputEventHandler(

		stateMachine<EditInputEventContext>(Unhandled) {

			state("sense") {
				onEntry { it?.view?.setCursor(Cursor.DEFAULT) }
				transitTo("insideEdge") {
					given { mouseMoved(it) && snap(it) != null }
				}
			}

			state("insideEdge") {
				onEntry { displayPortViewHighlight(it!!, snap(it)!!.location) }
				transitTo("insideEdge") {
					given { mouseMoved(it) && snap(it) != null }
					onTransit { displayPortViewHighlight(it!!, snap(it)!!.location) }
				}
				transitTo("sense") {
					given { snap(it) == null }
					onTransit { removePortViewHighlight(it!!) }
				}
				transitTo("drag") {
					given { mousePressed(it) }
					onTransit {
						beginConnecting(it!!)
						removePortViewHighlight(it)
					}
				}
			}

			state("drag") {
				transitTo("insideTargetPortView") {
					given { mouseDragged(it) && insideTargetPortView(draggedEndpointType, it) }
				}
				transitTo("drag") {
					given { mouseDragged(it) && !insideTargetPortView(draggedEndpointType, it) }
					onTransit { moveEdgeViewEndpoint(it!!) }
				}
				transitTo("connected") {
					given { mouseReleased(it) && isValidEdgeView }
				}
				transitTo("cancelled") {
					given { mouseReleased(it) && !isValidEdgeView }
				}
				transitTo("cancelled") {
					given { escapePressed(it) }
				}
			}

			// This is exactly the same code as in AbstractPortViewStartConnector. However, if we would use
			// a common State builder for this State, we would loose the insight in the entire StateMachine here.
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
					given { escapePressed(it) }
				}
			}

			state("connected") {
				onEntry {
					completeConnecting(it!!)
					reset()
				}
			}

			state("cancelled") {
				onEntry { cancel(it!!.editor) }
			}
		}
	)

	fun useFor(edgeView: EdgeView<*>) {
		reset()
		branchedEdgeView = edgeView
		handler.sm.start()
	}

	private fun snap(context: EditInputEventContext): EdgeViewSnapLocatorResult? {
		val result = branchedEdgeView!!.snap(context.x, context.y, context.editor.view.grid)
		branchedSegmentIndex = result?.segmentIndex
		return result
	}

	private fun beginConnecting(context: EditInputEventContext) {
		createEdgeView(context.drawingView(), Point2D(ConnectionPointHighlighter.portViewHighlight!!.location), branchedEdgeView!!.model as Net<Any>)
		removePortViewHighlight(context)
		splitCommand = createSplitEdgeViewCommand(context.editor)
		splitCommand!!.execute()
	}

	override fun cancel(editor: Editor) {
		splitCommand?.undo()
		ConnectionPointHighlighter.removePortViewHighlight(editor.view)
		reset()
	}

	private fun completeConnecting(context: EditInputEventContext) {
		if (targetPortView != null) {
			connectService.connectToDestination(edgeView!!, targetPortView!!.createConnection() as Connection<Any>)
		}

		context.editor.commandManager.register(splitCommand!!)
		splitCommand = null
	}

	private fun createSplitEdgeViewCommand(editor: Editor): Command {
		return SplitEdgeViewCommand(
			editor = editor,
			connectService = connectService,
			graphView = editor.view.drawing as GraphView<GraphElementView<*>>,
			origEdgeView = branchedEdgeView!!,
			segmentIndex = branchedSegmentIndex!!,
			newEdgeView = edgeView!!,
			newEdgeViewEndpointType = EdgeViewEndpointType.ORIGIN,
			targetPortView = targetPortView,
			nodeView = null)
	}
}