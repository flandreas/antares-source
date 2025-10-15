package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.Status
import ch.scorpion.jabbah.base.StatusType
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.state.UnhandledEventBehaviour.Unhandled
import ch.scorpion.jabbah.base.state.stateMachine
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.altPressed
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.altReleased
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.escapePressed
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseLeftClicked
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseLeftDoubleClicked
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseDragged
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseMoved
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseLeftPressed
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseLeftReleased
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseLeftSingleClicked
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
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
	edgeViewFactory: EdgeViewFactory,
	draggedEndpointType: EdgeViewEndpointType,
	private val allowEdgeViewAsTarget: Boolean = false
) : AbstractCreateEdgeViewConnector(edgeViewFactory, draggedEndpointType) {

	@Suppress("ConstPropertyName")
	companion object {
		private val LOG by logger(AbstractPortViewStartConnector::class)

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
		private const val connectedToEdgeView = "connectedToEdge"
		private const val move = "move"
	}

	/** The [VerticeView] from which the new connection originates. */
	private var startVerticeView: VerticeView<*>? = null

	/** The [PortView] in [startVerticeView] from which the new connection originates.  */
	protected var startPortView: PortView<*>? = null
		private set

	/**
	 * The indices of the points in [edgeView] that have been manually set (i.e. adjusted) by the user.
	 * Organized as a [Stack] to support repetitive unrollment by pressing ESC.
	 */
	private var adjustment: EdgeViewAdjustmentView? = null

	private var oldStatus: String? = null

	override val handler = StateMachineInputEventHandler(

		stateMachine<EditInputEventContext>(Unhandled) {

			// Avoid interference with keyPressed events due to ALT key used to activate "adjust" mode
			ignoreEvent { it.keyEvent != null }

			state(sense) {
				transitTo(insideStartDrag) {
					given { it.mouseEvent?.isAltDown != true && mouseMoved(it) && insideStartPortView(it.location) }
				}
				transitTo(insideStartAdjust) {
					given { it.mouseEvent?.isAltDown == true && mouseMoved(it) && insideStartPortView(it.location) }
				}
			}

			state(insideStartDrag) {
				onEntry {
					displayPortViewHighlight(it)
					oldStatus = Status.replace(StatusType.Tool, Translations.getString("graph.tool.connector.insideStartDrag.stateTip"))
				}
				onExit {
					removePortViewHighlight(it)
					Status.set(StatusType.Tool, oldStatus)
				}
				stayIf { mouseMoved(it) && insideCurrentPortView(it.location) }
				transitTo(sense) {
					given { mouseMoved(it) && !insideCurrentPortView(it.location) }
				}
				transitTo(insideStartAdjust) {
					given { altPressed(it) }
				}
				stayIf { mouseLeftReleased(it) && it.mouseEvent?.isAltDown == true }
				transitTo(adjust) {
					given { mouseLeftClicked(it) && it.mouseEvent?.isAltDown == true }
					onTransit { beginConnecting(it) }
				}
				transitTo(drag) {
					given { mouseLeftPressed(it) && it.mouseEvent?.isAltDown != true }
					onTransit { beginConnecting(it) }
				}
			}

			state(insideStartAdjust) {
				onEntry {
					displayAlternativePortViewHighlight(it)
					oldStatus = Status.replace(StatusType.Tool, Translations.getString("graph.tool.connector.insideStartAdjust.stateTip"))
				}
				onExit {
					removePortViewHighlight(it)
					Status.set(StatusType.Tool, oldStatus)
				}
				stayIf { mouseMoved(it) && insideCurrentPortView(it.location) }
				transitTo(sense) {
					given { mouseMoved(it) && !insideCurrentPortView(it.location) }
				}
				transitTo(insideStartDrag) {
					given { altReleased(it) }
				}
				transitTo(adjust) {
					given { mouseLeftClicked(it) }
					onTransit { beginConnecting(it) }
				}
				stayOtherwise()
			}

			superstate(drag) {
				stateMachine(Unhandled) {

					state(drag) {
						onEntry {
							oldStatus = Status.replace(StatusType.Tool, Translations.getString("graph.tool.connector.drag.drag.stateTip"))
						}
						onExit {
							Status.set(StatusType.Tool, oldStatus)
						}
						transitTo(insideTargetPortView) {
							given { mouseDragged(it) && insideTargetPortView(draggedEndpointType, it) }
						}
						transitTo(insideTargetEdgeView) {
							given { mouseDragged(it) && allowEdgeViewAsTarget && insideTargetEdgeView(draggedEndpointType, it) }
						}
						transitTo(insideDenyingPortView) {
							given { mouseDragged(it) && insideDenyingPortView(draggedEndpointType, it) }
						}
						transitTo(insideDenyingEdgeView) {
							given { mouseDragged(it) && allowEdgeViewAsTarget && insideDenyingEdgeView(draggedEndpointType, it) }
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
						stayOtherwise {
							onTransit {
								if (it.mouseEvent != null) {
									// Bug #1066: Could also be a KeyEvent, where (x,y) is zero
									moveEdgeViewEndpoint(it)
								}
							}
						}
					}

					state(insideTargetPortView) {
						onEntry {
							snapToTargetPortView(it)
							oldStatus = Status.replace(StatusType.Tool, Translations.getString("graph.tool.connector.drag.insideTargetPortView.stateTip"))
						}
						onExit {
							removePortViewHighlight(it)
							Status.set(StatusType.Tool, oldStatus)
						}
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
						onEntry {
							snapToDenyingPortView(it)
							oldStatus = Status.replace(StatusType.Tool, Translations.getString("graph.tool.connector.drag.insideDenyingPortView.stateTip"))
						}
						onExit {
							removePortViewHighlight(it)
							Status.set(StatusType.Tool, oldStatus)
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
						transitTo(cancelled) {
							given { mouseLeftReleased(it) }
						}
						transitTo(cancelled) {
							given { escapePressed(it) }
						}
					}

					state(insideTargetEdgeView) {
						onEntry {
							snapToTargetEdgeView(it)
							oldStatus = Status.replace(StatusType.Tool, Translations.getString("graph.tool.connector.drag.insideTargetEdgeView.stateTip"))
						}
						onExit {
							removePortViewHighlight(it)
							Status.set(StatusType.Tool, oldStatus)
						}
						transitTo(drag) {
							given { mouseDragged(it) && !insideTargetEdgeView(draggedEndpointType, it) }
						}
						transitTo(connectedToEdgeView) {
							given { mouseLeftReleased(it) }
						}
						transitTo(cancelled) {
							given { escapePressed(it) }
						}
						stayOtherwise {
							onTransit {
								if (it.mouseEvent != null) {
									snapToTargetEdgeView(it)
								}
							}
						}
					}

					state(insideDenyingEdgeView) {
						onEntry {
							snapToDenyingEdgeView(it)
							oldStatus = Status.replace(StatusType.Tool, Translations.getString("graph.tool.connector.drag.insideDenyingEdgeView.stateTip"))
						}
						onExit {
							removePortViewHighlight(it)
							Status.set(StatusType.Tool, oldStatus)
						}
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
							onTransit {
								if (it.mouseEvent != null) {
									snapToDenyingEdgeView(it)
								}
							}
						}
					}

					state(connected) {
						onEntry {
							edgeView?.underConstruction = false
							completeConnectingToEndPortOrOpen(it)
							reset()
						}
					}

					state(connectedToEdgeView) {
						onEntry {
							edgeView?.underConstruction = false
							completeConnectingToEdge(it)
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
							oldStatus = Status.replace(StatusType.Tool, Translations.getString("graph.tool.connector.adjust.move.stateTip"))
						}
						onExit {
							Status.set(StatusType.Tool, oldStatus)
						}
						transitTo(insideTargetPortView) {
							given { mouseMoved(it) && insideTargetPortView(draggedEndpointType, it) }
						}
						transitTo(insideTargetEdgeView) {
							given { mouseMoved(it) && insideTargetEdgeView(draggedEndpointType, it) }
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
							given { escapePressed(it) && isLastUndo() }
						}
						stayOtherwise()
					}

					state(insideTargetPortView) {
						onEntry {
							adjustToTargetPortView(it)
							oldStatus = Status.replace(StatusType.Tool, Translations.getString("graph.tool.connector.adjust.insideTargetPortView.stateTip"))
						}
						onExit {
							removePortViewHighlight(it)
							Status.set(StatusType.Tool, oldStatus)
						}
						stayIf { mouseMoved(it) && insideTargetPortView(draggedEndpointType, it) }
						transitTo(move) {
							given { mouseMoved(it) && !insideTargetPortView(draggedEndpointType, it) }
						}

						transitTo(connected) {
							given { mouseLeftPressed(it) }
						}
						transitTo(cancelled) {
							given { escapePressed(it) && isLastUndo() }
						}
						stayOtherwise()
					}

					state(insideTargetEdgeView) {
						onEntry {
							snapToTargetEdgeView(it)
							oldStatus = Status.replace(StatusType.Tool, Translations.getString("graph.tool.connector.adjust.insideTargetEdgeView.stateTip"))
						}
						onExit {
							removePortViewHighlight(it)
							Status.set(StatusType.Tool, oldStatus)
						}
						stayIf({ mouseMoved(it) && insideTargetEdgeView(draggedEndpointType, it) }) {
							onTransit { snapToTargetEdgeView(it) }
						}
						transitTo(move) {
							given { mouseMoved(it) && !insideTargetEdgeView(draggedEndpointType, it) }
						}

						transitTo(connectedToEdgeView) {
							given { mouseLeftPressed(it) }
						}
						transitTo(cancelled) {
							given { escapePressed(it) && isLastUndo() }
						}

						stayOtherwise()
					}

					state(connected) {
						onEntry {
							completeConnectingToEndPortOrOpen(it)
							endAdjustment(it)
							reset()
						}
					}

					state(connectedToEdgeView) {
						onEntry {
							completeConnectingToEdge(it)
							endAdjustment(it)
							reset()
						}
					}

					state(cancelled) {
						onEntry {
							endAdjustment(it)
							cancel(it.editor)
						}
					}
				}
			}
		}
	)

	protected abstract fun connectEdgeViewToStartPort()

	protected abstract fun completeConnectingToEndPortOrOpen(context: EditInputEventContext)

	protected abstract fun createAdjustment(): EdgeViewAdjustmentView

	/**
	 * Prepares this [AbstractPortViewStartConnector] to be used to create [EdgeView]s that the user
	 * starts in the specified [VerticeView].
	 */
	fun useFor(verticeView: VerticeView<*>, context: EditInputEventContext) {
		reset()
		startVerticeView = verticeView
		handler.sm.start(context)
	}

	// For testing
	val usedFor: VerticeView<*>? get() = startVerticeView

	override fun reset() {
		super.reset()
		startVerticeView = null
		startPortView = null
		adjustment = null
	}

	override fun canConnectTo(type: EdgeViewEndpointType, edgeView: EdgeView<out Any>, graphView: GraphView): Boolean =
		type.canConnectTo(edgeView.net!!, startPortView!!.port, graphView)

	private fun insideStartPortView(location: Point2D): Boolean {
		val pv = startVerticeView!!.getPortViewAtConnectionPoint(location)
		startPortView = if (pv != null && !pv.port.isConnected && portTypeCond.invoke(pv.port.portType)) {
			pv
		} else {
			null
		}
		return startPortView != null
	}

	private fun insideCurrentPortView(location: Point2D): Boolean =
		startVerticeView!!.getPortViewAtConnectionPoint(location) === startPortView

	private fun displayPortViewHighlight(context: EditInputEventContext, alternativeView: Boolean = false) {
		displayPortViewHighlight(context, startVerticeView!!.getPortConnectionPoint(startPortView!!.port), alternativeView)
	}

	private fun displayAlternativePortViewHighlight(context: EditInputEventContext) {
		displayPortViewHighlight(context, alternativeView = true)
	}

	private fun beginAdjustment(context: EditInputEventContext) {
		adjustment = createAdjustment()
		context.drawingView.animationContainer.add(adjustment!!)
		context.drawingView.animationContainer.validate()
	}

	private fun endAdjustment(context: EditInputEventContext) {
		context.drawingView.animationContainer.remove(adjustment!!)
	}

	private fun beginConnecting(context: EditInputEventContext) {
		createEdgeView(context.drawingView as DrawingView<GraphView>, startVerticeView!!.getPortConnectionPoint(startPortView!!.port), null)
		LOG.userTrail("Start creating new EdgeView ${edgeView!!.id} on Net ${edgeView!!.model.id} at Port ${startPortView!!.port.portId} of ${startVerticeView!!.type} ${startVerticeView!!.id}")
		edgeView!!.model.connect(startPortView!!.port as Port<Any>)
		connectEdgeViewToStartPort()
	}

	private fun moveAdjustedPoint(context: EditInputEventContext) {
		draggedEndpointType.adjustTo(
			edgeView = edgeView!!,
			layoutIndex = adjustment!!.model.current,
			location = context.location.add(context.editor.snapManager.snap(context.x, context.y)))
	}

	private fun addAdjustedPoint(context: EditInputEventContext) {
		draggedEndpointType.adjustTo(
			edgeView = edgeView!!,
			layoutIndex = adjustment!!.model.current,
			location = context.location.add(context.editor.snapManager.snap(context.x, context.y)))

		adjustment!!.model.add()
		edgeView!!.validate()
	}

	private fun adjustToTargetPortView(context: EditInputEventContext) {
		// Start highlighting current destination PortView
		val connPointAbs = targetPortView!!.owner!!.getPortConnectionPoint(targetPortView!!.port)
		ConnectionPointHighlighter.displayPortViewHighlight(context.drawingView, connPointAbs)

		// Layout EdgeView
		val direction = draggedEndpointType.getDirectionForPortView(targetPortView!!)
		draggedEndpointType.adjustTo(
			edgeView = edgeView!!,
			layoutIndex = adjustment!!.model.current,
			direction = direction,
			location = connPointAbs)

		edgeView?.validate()
	}

	private fun isLastUndo(): Boolean {
		if (adjustment!!.model.size == 1) {
			return true
		}

		val currentLocation = draggedEndpointType.getLocation(edgeView!!)
		draggedEndpointType.remove(edgeView!!)

		adjustment!!.model.undo()
		draggedEndpointType.adjustTo(
			edgeView = edgeView!!,
			layoutIndex = adjustment!!.model.current,
			location = currentLocation)
		edgeView!!.validate()

		return false
	}

	private fun completeConnectingToEdge(context: EditInputEventContext) {
		if (edgeView == null) {
			LOG.warn("Illegal State: edgeView in completeConnectingToEdge is null")
			cancel(context.editor)
			return
		}

		if (targetEdgeView == null || targetEdgeViewSegmentIndex == null) {
			cancel(context.editor)
			return
		}

		LOG.userTrail("Connect to EdgeView ${targetEdgeView!!.id} at ${draggedEndpointType.getLocation(edgeView!!)}")

		connectService.unconnect(edgeView!!)
		context.drawingView.drawing.remove(edgeView!!)

		try {
			context.editor.commandManager.beginTransaction("graph.command.connect", context.drawingView)

			context.editor.commandManager.execute(
				SplitEdgeViewCommand(
					editor = context.editor,
					connectService = connectService,
					splitEdgeViewId = targetEdgeView!!.id,
					splitLocation = draggedEndpointType.getLocation(edgeView!!),
					segmentIndex = targetEdgeViewSegmentIndex!!,
					newEdgeViewProvider = NewEdgeViewAtSplitCloneProvider(edgeView!!),
					newEdgeViewEndpointType = draggedEndpointType,
					targetConnectableViewId = startPortView!!.owner!!.id,
					targetPortId = startPortView!!.port.portId
				)
			)

			GraphViewModule.connectionEstablishedHandler?.handle(context.editor, startPortView!!.port)?.let {
				context.editor.commandManager.execute(it)
			}

			context.editor.commandManager.commitTransaction()
		} catch (e: Exception) {
			if (context.editor.commandManager.isInTransaction) {
				context.editor.commandManager.rollbackTransaction()
			}
			postConnectorErrorMessage(e)
		}
	}
}