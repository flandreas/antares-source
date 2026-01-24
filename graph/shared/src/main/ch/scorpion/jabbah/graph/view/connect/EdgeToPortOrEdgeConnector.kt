package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.event.Modifier
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.state.UnhandledEventBehaviour.Unhandled
import ch.scorpion.jabbah.base.state.stateMachine
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.escapePressed
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseDragged
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseLeftClicked
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseLeftDoubleClicked
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseLeftPressed
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseLeftReleased
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseLeftSingleClicked
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseMoved
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.EdgeViewSnapLocatorResult
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.NetView
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
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

	@Suppress("ConstPropertyName")
	companion object {
		val SPLIT_EDGE_VIEW_MODIFIER = Modifier.Alt
		private val LOG by logger(EdgeToPortOrEdgeConnector::class)

		private const val sense = "sense"
		private const val insideStartDrag = "insideStartDrag"
		private const val insideStartAdjust = "insideStartAdjust"
		private const val adjust = "adjust"
		private const val drag = "drag"
		private const val insideTargetPortView = "insideTargetPortView"
		private const val insideTargetEdgeView = "insideTargetEdgeView"
		private const val insideDenyingPortView = "insideDenyingPortView"
		private const val insideDenyingEdgeView = "insideDenyingEdgeView"
		private const val connected = "connected"
		private const val cancelled = "cancelled"
		private const val connectedToEdge = "connectedToEdge"
		private const val move = "move"

		// Visible for testing
		fun calculateFreeNodeDirections(nodePos: Point2D, inEVPos: Point2D, outEVPos: Point2D): Set<Direction> {
			return if (inEVPos.x < nodePos.x && outEVPos.y > nodePos.y || outEVPos.x < nodePos.x && inEVPos.y > nodePos.y) {
				Direction.NORTH_EAST
			} else if (inEVPos.x < nodePos.x && outEVPos.y < nodePos.y || outEVPos.x < nodePos.x && inEVPos.y < nodePos.y) {
				Direction.SOUTH_EAST
			} else if (inEVPos.x > nodePos.x && outEVPos.y < nodePos.y || outEVPos.x > nodePos.x && inEVPos.y < nodePos.y) {
				Direction.SOUTH_WEST
			} else if (inEVPos.x > nodePos.x && outEVPos.y > nodePos.y || outEVPos.x > nodePos.x && inEVPos.y > nodePos.y) {
				Direction.NORTH_WEST
			} else {
				Direction.ALL
			}
		}
	}

	/** The [EdgeView] from which a new [EdgeView] is branched by this connector. */
	private var branchedEdgeView: EdgeView<*>? = null

	/** The index of the segment in [branchedEdgeView] at which splitting takes place.*/
	private var branchedSegmentIndex: Int? = null

	/**
	 * The [SplitEdgeViewCommand] created when clicking on the target [EdgeView].
	 * Remembered to set the list of manually clicked points to re-establish them in redo after undo.
	 */
	private var splitEdgeViewCommand: SplitEdgeViewCommand? = null

	/**
	 * Caches the result of [calculateMoveAdjustedPointOrigDirs].
	 */
	private var moveAdjustedPointOrigDirs: Set<Direction>? = null

	override val handler = StateMachineInputEventHandler(

		stateMachine<EditInputEventContext>(Unhandled) {

			ignoreEvent { it.keyEvent != null }

			state(sense) {
				onEntry { it.view.setCursor(Cursor.DEFAULT) }
				transitTo(insideStartDrag) {
					given { CurrentConnectMethod.defaultMethod == ConnectMethod.AutoLayout && mouseMoved(it) && snap(it) != null }
				}
				transitTo(insideStartAdjust) {
					given { CurrentConnectMethod.defaultMethod == ConnectMethod.SetPoints && mouseMoved(it) && snap(it) != null }
				}
			}

			state(insideStartDrag) {
				onEntry { displayPortViewHighlight(it, snap(it)!!.location) }
				transitTo(insideStartDrag) {
					given { mouseMoved(it) && snap(it) != null }
					onTransit { displayPortViewHighlight(it, snap(it)!!.location) }
				}
				transitTo(sense) {
					given { mouseMoved(it) && snap(it) == null }
					onTransit { removePortViewHighlight(it) }
				}
				transitTo(drag) {
					given { mouseLeftPressed(it) && snap(it) != null }
					onTransit {
						beginConnecting(it, adjust = false)
						removePortViewHighlight(it)
					}
				}
			}

			state(insideStartAdjust) {
				onEntry {
					displayPortViewHighlight(it, snap(it)!!.location, alternativeView = true)
				}
				transitTo(insideStartAdjust) {
					given { mouseMoved(it) && snap(it) != null }
					onTransit { displayPortViewHighlight(it, snap(it)!!.location, alternativeView = true) }
				}
				transitTo(sense) {
					given { mouseMoved(it) && snap(it) == null }
					onTransit { removePortViewHighlight(it) }
				}
				transitTo(adjust) {
					given { mouseLeftClicked(it) && snap(it) != null }
					onTransit {
						beginConnecting(it, adjust = true)
						removePortViewHighlight(it)
					}
				}
				stayOtherwise()
			}

			superstate(drag) {
				stateMachine(Unhandled) {

					state(drag) {
						transitTo(sense) {
							// Connecting has been interrupted in beginConnecting() because snap was not valid
							// Transaction has not yet been started, so we can't cancel(), which would rollback
							given { edgeView == null }
						}
						transitTo(insideTargetPortView) {
							given { mouseDragged(it) && insideTargetPortView(draggedEndpointType, it) }
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
							given { mouseDragged(it) && !insideTargetPortView(draggedEndpointType, it) }
							onTransit { moveEdgeViewEndpoint(it) }
						}
						transitTo(connected) {
							given { mouseLeftReleased(it) && isValidEdgeView }
						}
						transitTo(cancelled) {
							given { mouseLeftReleased(it) && !isValidEdgeView }
						}
						transitTo(cancelled) {
							given { escapePressed(it) }
						}
					}

					// This is exactly the same code as in AbstractPortViewStartConnector. However, if we used
					// a common State builder for this State, we would lose the insight into the entire StateMachine here.
					state(insideTargetPortView) {
						onEntry { snapToTargetPortView(it) }
						onExit { removePortViewHighlight(it) }
						transitTo(insideTargetPortView) {
							given { mouseDragged(it) && insideTargetPortView(draggedEndpointType, it) }
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

					state(insideDenyingPortView) {
						onEntry {
							snapToDenyingPortView(it)
						}
						onExit {
							removePortViewHighlight(it)
						}
						transitTo(insideDenyingPortView) {
							given { mouseDragged(it) && insideDenyingPortView(draggedEndpointType, it) }
							onTransit {
								snapToDenyingPortView(it)
							}
						}
						transitTo(drag) {
							given { mouseDragged(it) && !insideDenyingPortView(draggedEndpointType, it) }
						}
						transitTo(cancelled)  {
							given { mouseLeftReleased(it) }
						}
						transitTo(cancelled) {
							given { escapePressed(it) }
						}
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

					state(connected) {
						onEntry {
							completeConnectingToPortViewOrOpen(it, adjust = false)
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

					state(cancelled) {
						onEntry { cancel(it.editor) }
					}

				}
			}

			superstate(adjust) {
				onEntry { beginAdjustment(it) }
				stateMachine(Unhandled) {

					state(move) {
						onEntry {
							it.view.setCursor(Cursor.CROSSHAIR)
						}
						transitTo(sense) {
							// Connecting has been interrupted in beginConnecting() because snap was not valid
							// Transaction has not yet been started, so we can't cancel(), which would rollback
							given { edgeView == null }
						}
						transitTo(insideTargetPortView) {
							given { mouseMoved(it) && insideTargetPortView(draggedEndpointType, it) }
						}
						transitTo(insideTargetEdgeView) {
							given { mouseMoved(it) && insideTargetEdgeView(draggedEndpointType, it) }
						}
						transitTo(insideDenyingPortView) {
							given { mouseMoved(it) && insideDenyingPortView(draggedEndpointType, it) }
						}
						transitTo(insideDenyingEdgeView) {
							given { mouseMoved(it) && insideDenyingEdgeView(draggedEndpointType, it) }
						}
						stayIf({ mouseMoved(it) }) {
							onTransit { moveAdjustedPoint(it) }
						}
						stayIf({ mouseLeftSingleClicked(it) }) {
							onTransit { addAdjustedPoint(it) }
						}
						transitTo(connected) {
							given { mouseLeftDoubleClicked(it) }
						}
						transitTo(cancelled) {
							given { escapePressed(it) && isLastUndoAfterRemovingLastPoint() }
						}
						stayOtherwise()
					}

					state(insideTargetPortView) {
						onEntry {
							adjustToTargetPortView(it)
						}
						onExit {
							removePortViewHighlight(it)
						}
						stayIf { mouseMoved(it) && insideTargetPortView(draggedEndpointType, it) }
						transitTo(move) {
							given { mouseMoved(it) && !insideTargetPortView(draggedEndpointType, it) }
						}
						transitTo(connected) {
							given { mouseLeftClicked(it) }
						}
						transitTo(cancelled) {
							given { escapePressed(it) && isLastUndoAfterRemovingLastPoint() }
						}
						stayOtherwise()
					}

					state(insideDenyingPortView) {
						onEntry {
							snapToDenyingPortView(it)
						}
						onExit {
							removePortViewHighlight(it)
						}
						transitTo(insideDenyingPortView) {
							given { mouseMoved(it) && insideDenyingPortView(draggedEndpointType, it) }
							onTransit {
								snapToDenyingPortView(it)
							}
						}
						transitTo(move) {
							given { mouseMoved(it) && !insideDenyingPortView(draggedEndpointType, it) }
						}
						transitTo(cancelled) {
							given { escapePressed(it) }
						}
					}

					state(insideTargetEdgeView) {
						onEntry { adjustToTargetEdgeView(it) }
						onExit { removePortViewHighlight(it) }
						stayIf({ mouseMoved(it) && insideTargetEdgeView(draggedEndpointType, it) }) {
							onTransit { adjustToTargetEdgeView(it) }
						}
						transitTo(move) {
							given { mouseMoved(it) && !insideTargetEdgeView(draggedEndpointType, it) }
						}
						transitTo(connectedToEdge) {
							given { mouseLeftPressed(it) }
						}
						transitTo(cancelled) {
							given { escapePressed(it) }
						}
						stayOtherwise()
					}

					state(insideDenyingEdgeView) {
						onEntry { snapToDenyingEdgeView(it) }
						onExit { removePortViewHighlight(it) }
						transitTo(move) {
							given { mouseMoved(it) && !insideDenyingEdgeView(draggedEndpointType, it) }
						}
						transitTo(cancelled) {
							given { escapePressed(it) }
						}
						stayOtherwise {
							onTransit { snapToDenyingEdgeView(it) }
						}
					}

					state(connected) {
						onEntry {
							completeConnectingToPortViewOrOpen(it, adjust = true)
							endAdjustment(it)
							reset()
						}
					}

					state(connectedToEdge) {
						onEntry {
							edgeView?.underConstruction = false
							completeConnectingToEdgeView(it)
							endAdjustment(it)
							reset()
						}
					}

					state(cancelled) {
						onEntry {
							cancel(it.editor)
							// Must be done AFTER CommandManager transaction was canceled in cancel(),
							// otherwise UndoAction won't be re-enabled
							endAdjustment(it)
						}
					}
				}
			}
		}
	)

	fun useFor(edgeView: EdgeView<*>, context: EditInputEventContext) {
		reset()
		branchedEdgeView = edgeView
		handler.sm.start(context)
	}

	override fun reset() {
		super.reset()
		splitEdgeViewCommand = null
		moveAdjustedPointOrigDirs = null
	}

	override fun getMoveAdjustedPointOrigDirs(layoutIndex: Int, allowContinuation: Boolean): Set<Direction>? {
		if (moveAdjustedPointOrigDirs == null) {
			moveAdjustedPointOrigDirs = calculateMoveAdjustedPointOrigDirs(layoutIndex, allowContinuation)
		}
		return moveAdjustedPointOrigDirs
	}

	private fun calculateMoveAdjustedPointOrigDirs(layoutIndex: Int, allowContinuation: Boolean): Set<Direction>? {
		if (branchedEdgeView != null && layoutIndex == 0) {
			val incomingNodeEV = branchedEdgeView!!
			val outgoingNodeEV = splitEdgeViewCommand!!.result.tailEdgeView

			// If branching at an EdgeView corner, continuing in the direction of the branchedEdgeView is also valid
			return if (incomingNodeEV.polyline.isSegmentOrthogonalTo(incomingNodeEV.segmentPointCount - 2, 0, outgoingNodeEV.polyline.getPointList())) {
				// Splitting at EdgeView corner
				calculateFreeNodeDirections(
					splitEdgeViewCommand!!.result.nodeView.location,
					incomingNodeEV.getSegmentPoint(incomingNodeEV.segmentPointCount - 2),
					outgoingNodeEV.getSegmentPoint(1)
				)
			} else {
				// Splitting inside EdgeView
				edgeView!!.layout.type.getSegmentDirection(incomingNodeEV, incomingNodeEV.segmentPointCount - 2)?.orthogonalSet()
			}
		}
		return super.getMoveAdjustedPointOrigDirs(layoutIndex, allowContinuation)
	}

	override fun createAdjustment(): EdgeViewAdjustmentView =
		SimpleEdgeViewAdjustmentView.forDestinationAdjustmentOf(edgeView!!)

	private fun snap(context: EditInputEventContext): EdgeViewSnapLocatorResult? =
		snap(context.x, context.y, context.editor.snapManager)

	private fun snap(x: Double, y: Double, snapManager: SnapManager): EdgeViewSnapLocatorResult? {
		val result = branchedEdgeView?.snap(x, y, snapManager)
		branchedSegmentIndex = result?.segmentIndex
		return result
	}

	private fun beginConnecting(context: EditInputEventContext, adjust: Boolean) {
		val snapLocation = ConnectionPointHighlighter.portViewHighlight!!.location
		createEdgeView(context.drawingView as DrawingView<GraphView>, snapLocation, branchedEdgeView!!.netView as NetView<Any>)
		edgeView!!.layout.isAdjusted = adjust

		LOG.userTrail("Start creating junction from EdgeView ${branchedEdgeView!!.id} on Net ${branchedEdgeView!!.model.id} with new EdgeView ${edgeView?.id}, adjust=$adjust")

		// Re-snap to the PortView connection point to retrieve the optimal segment index
		// (avoid bug #627: wire distortion when splitting at EdgeView corner)
		val snapResult = snap(snapLocation.x, snapLocation.y, context.editor.snapManager)

		context.drawingView.drawing.remove(edgeView!!)
		removePortViewHighlight(context)

		if (snapResult == null) {
			edgeView = null
		} else {
			splitEdgeViewCommand = createSplitEdgeViewCommand(context.editor)
			context.editor.commandManager.beginTransaction(splitEdgeViewCommand!!)

			edgeView = splitEdgeViewCommand!!.addedNewEdgeView
			edgeView!!.underConstruction = true

			context.drawingView.selectionManager.deselectAll()
			context.drawingView.selectionManager.select(edgeView!!)
		}
	}

	override fun canConnectTo(type: EdgeViewEndpointType, edgeView: EdgeView<out Any>, graphView: GraphView): Boolean =
        type.canConnectTo(edgeView.net!!, edgeView.getConnection(type.opposite)?.port, graphView)

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
			LOG.userTrail("Create junction from EdgeView ${branchedEdgeView?.id} to port ${targetPortView?.port?.portId} of ${targetPortView?.owner?.type} with ID ${targetPortView?.owner?.id}")
		} else if (targetEdgeView != null) {
			LOG.userTrail("Create junction from EdgeView ${branchedEdgeView?.id} to new junction in EdgeView ${targetEdgeView?.id}")
		} else {
			LOG.userTrail("Create junction from EdgeView ${branchedEdgeView?.id} open-ended")
		}
	}

	private fun completeConnectingToPortViewOrOpen(context: EditInputEventContext, adjust: Boolean) {
		logConnect()

		edgeView!!.underConstruction = false
		if (adjust) {
			splitEdgeViewCommand!!.points = edgeView!!.polyline.getPoints(0, edgeView!!.segmentPointCount).toList()
		}

		try {
			if (targetPortView != null) {
				context.editor.commandManager.execute(createConnectDestinationCommand(context.editor))
				GraphViewModule.connectionEstablishedHandler?.handle(context.editor, targetPortView!!.port)?.let {
					context.editor.commandManager.execute(it)
				}
			} else {
				context.editor.commandManager.register(createMoveDestinationCommand(context.editor))
			}

			context.editor.commandManager.commitTransaction()
		} catch (e: Exception) {
			if (context.editor.commandManager.isInTransaction) {
				context.editor.commandManager.rollbackTransaction()
			}
			postConnectorErrorMessage(e)
		}
	}

	private fun completeConnectingToEdgeView(context: EditInputEventContext) {
		if (targetEdgeView == null) {
			LOG.warn("IllegalState: targetEdgeView in completeConnectingToEdgeView is null")
			cancel(context.editor)
			return
		}
		if (targetEdgeViewSegmentIndex == null) {
			LOG.warn("IllegalState: targetEdgeViewSegmentIndex in completeConnectingToEdgeView is null")
			cancel(context.editor)
			return
		}

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