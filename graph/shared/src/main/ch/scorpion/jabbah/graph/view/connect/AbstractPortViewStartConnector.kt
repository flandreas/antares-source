package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.state.UnhandledEventBehaviour.Unhandled
import ch.scorpion.jabbah.base.state.stateMachine
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.altPressed
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.altReleased
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.keyReleased
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseClicked
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseDoubleClicked
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseDragged
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseMoved
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mousePressed
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseReleased
import ch.scorpion.jabbah.draw.StateMachineInputEventHandler.Companion.mouseSingleClicked
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.EdgeView
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

	companion object {
		private val LOG by logger(AbstractPortViewStartConnector::class)
	}

	/** The [VerticeView] from which the new connection originates. */
	private var startVerticeView: VerticeView<*>? = null

	/** The [PortView] in [startVerticeView] from which the new connection originates.  */
	protected var startPortView: PortView<*>? = null
		private set

	/** The found target [EdgeView], if any. */
	private var targetEdgeView: EdgeView<*>? = null

	private var targetEdgeViewSegmentIndex: Int? = null

	/**
	 * The indices of the points in [edgeView] that have been manually set (i.e. adjusted) by the user.
	 * Organized as a [Stack] to support repetitive unrollment by pressing ESC.
	 */
	private var adjustment: EdgeViewAdjustmentView? = null

	override val handler = StateMachineInputEventHandler(

		stateMachine<EditInputEventContext>(Unhandled) {

			// Avoid interference of keyPressed events due to ALT key used to activate "adjust" mode
			ignoreEvent { it.keyEvent != null }

			state("sense") {
				transitTo("insideStartDrag") {
					given { it.mouseEvent?.isAltDown != true && mouseMoved(it) && insideStartPortView(it.location) }
				}
				transitTo("insideStartAdjust") {
					given { it.mouseEvent?.isAltDown == true && mouseMoved(it) && insideStartPortView(it.location) }
				}
			}

			state("insideStartDrag") {
				onEntry { displayPortViewHighlight(it) }
				onExit { removePortViewHighlight() }
				stayIf { mouseMoved(it) && insideCurrentPortView(it.location) }
				transitTo("sense") {
					given { mouseMoved(it) && !insideCurrentPortView(it.location) }
				}
				transitTo("insideStartAdjust") {
					given { altPressed(it) }
				}
				stayIf { mouseReleased(it) && it.mouseEvent?.isAltDown == true }
				transitTo("adjust") {
					given { mouseClicked(it) && it.mouseEvent?.isAltDown == true }
					onTransit { beginConnecting(it) }
				}
				transitTo("drag") {
					given { mousePressed(it) && it.mouseEvent?.isAltDown != true }
					onTransit { beginConnecting(it) }
				}
			}

			state("insideStartAdjust") {
				onEntry { displayAlternativePortViewHighlight(it) }
				onExit { removePortViewHighlight() }
				stayIf { mouseMoved(it) && insideCurrentPortView(it.location) }
				transitTo("sense") {
					given { mouseMoved(it) && !insideCurrentPortView(it.location) }
				}
				transitTo("insideStartDrag") {
					given { altReleased(it) }
				}
				transitTo("adjust") {
					given { mouseClicked(it) }
					onTransit { beginConnecting(it) }
				}
				// The following is necessary to properly support "mouseClicked". Couldn't that be automatically
				// supported by the framework?
				stayIf { mousePressed(it) }
				stayIf { mouseReleased(it) }
			}

			superstate("drag") {
				stateMachine(Unhandled) {

					state("drag") {
						stayIf({ mouseDragged(it) && !insideTargetPortView(draggedEndpointType, it) && (!allowEdgeViewAsTarget || !insideTargetEdgeView(it)) }) {
							onTransit { moveEdgeViewEndpoint(it) }
						}
						transitTo("insideTargetPortView") {
							given { mouseDragged(it) && insideTargetPortView(draggedEndpointType, it) }
						}
						transitTo("insideTargetEdgeView") {
							given { mouseDragged(it) && allowEdgeViewAsTarget && insideTargetEdgeView(it) }
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

					state("insideTargetPortView") {
						onEntry { snapToTargetPortView(it) }
						onExit { removePortViewHighlight() }
						stayIf { mouseDragged(it) && insideTargetPortView(draggedEndpointType, it) }
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

					state("insideTargetEdgeView") {
						onEntry { snapToTargetEdgeView(it) }
						onExit { removePortViewHighlight() }
						stayIf({ mouseDragged(it) && insideTargetEdgeView(it) }) {
							onTransit { snapToTargetEdgeView(it) }
						}
						transitTo("drag") {
							given { mouseDragged(it) && !insideTargetEdgeView(it) }
						}
						transitTo("connectedToEdge") {
							given { mouseReleased(it) }
						}
						transitTo("cancelled") {
							given { escapePressed(it) }
						}
					}

					state("connected") {
						onEntry {
							edgeView?.underConstruction = false
							completeConnectingToEndPortOrOpen(it)
							reset()
						}
					}

					state("connectedToEdge") {
						onEntry {
							edgeView?.underConstruction = false
							completeConnectingToEdge(it)
							reset()
						}
					}

					state("cancelled") {
						onEntry { cancel(it.editor) }
					}
				}
			}

			superstate("adjust") {
				onEntry { beginAdjustment(it) }
				stateMachine(Unhandled) {

					state("move") {
						onEntry { it.view.setCursor(Cursor.CROSSHAIR) }
						stayIf { mousePressed(it) }
						stayIf { mouseReleased(it) }
						stayIf { keyReleased(it) }
						transitTo("insideTargetPortView") {
							given { mouseMoved(it) && insideTargetPortView(draggedEndpointType, it) }
						}
						transitTo("insideTargetEdgeView") {
							given { mouseMoved(it) && insideTargetEdgeView(it) }
						}
						stayIf({ mouseMoved(it) }) {
							onTransit { moveAdjustedPoint(it) }
						}
						stayIf({ mouseSingleClicked(it) }) {
							onTransit { addAdjustedPoint(it) }
						}
						transitTo("connected") {
							given { mouseDoubleClicked(it) }
						}
						transitTo("cancelled") {
							given { escapePressed(it) && isLastUndo() }
						}
					}

					state("insideTargetPortView") {
						onEntry { adjustToTargetPortView(it) }
						onExit { removePortViewHighlight() }
						stayIf { mouseMoved(it) && insideTargetPortView(draggedEndpointType, it) }
						transitTo("move") {
							given { mouseMoved(it) && !insideTargetPortView(draggedEndpointType, it) }
						}
						stayIf { mousePressed(it) }
						stayIf { mouseReleased(it) }
						transitTo("connected") {
							given { mouseClicked(it) }
						}
						transitTo("cancelled") {
							given { escapePressed(it) && isLastUndo() }
						}
					}

					state("insideTargetEdgeView") {
						onEntry { snapToTargetEdgeView(it) }
						onExit { removePortViewHighlight() }
						stayIf({ mouseMoved(it) && insideTargetEdgeView(it) }) {
							onTransit { snapToTargetEdgeView(it) }
						}
						transitTo("move") {
							given { mouseMoved(it) && !insideTargetEdgeView(it) }
						}
						stayIf { mousePressed(it) }
						stayIf { mouseReleased(it) }
						transitTo("connectedToEdge") {
							given { mouseClicked(it) }
						}
						transitTo("cancelled") {
							given { escapePressed(it) && isLastUndo() }
						}
					}

					state("connected") {
						onEntry {
							completeConnectingToEndPortOrOpen(it)
							endAdjustment(it)
							reset()
						}
					}

					state("connectedToEdge") {
						onEntry {
							completeConnectingToEdge(it)
							endAdjustment(it)
							reset()
						}
					}

					state("cancelled") {
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
		context.drawingView().animationContainer.add(adjustment!!)
		context.drawingView().animationContainer.validate()
	}

	private fun endAdjustment(context: EditInputEventContext) {
		context.drawingView().animationContainer.remove(adjustment!!)
	}

	private fun beginConnecting(context: EditInputEventContext) {
		LOG.debug("Start creating new EdgeView at PortView")
		createEdgeView(context.drawingView(), startVerticeView!!.getPortConnectionPoint(startPortView!!.port), null)
		edgeView!!.model.connect(startPortView!!.port as Port<Any>)
		connectEdgeViewToStartPort()
	}

	private fun insideTargetEdgeView(context: EditInputEventContext): Boolean {
		val destDrawable = context.drawingView().drawing.getDrawable { it !== edgeView && it.contains(context.location) }
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
		ConnectionPointHighlighter.displayPortViewHighlight(context.drawingView(), connPointAbs)

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

	private fun logConnect() {
		LOG.debug("Connect port of ${startPortView?.owner?.type} with existing EdgeView ${targetEdgeView?.id}")
	}

	private fun completeConnectingToEdge(context: EditInputEventContext) {
		connectService.unconnect(edgeView!!)
		context.drawingView().drawing.remove(edgeView!!)

		context.editor.commandManager.execute(
			SplitEdgeViewCommand(
				editor = context.editor,
				connectService = connectService,
				splitEdgeViewId = targetEdgeView!!.id,
				segmentIndex = targetEdgeViewSegmentIndex!!,
				newEdgeView = edgeView!!,
				newEdgeViewEndpointType = draggedEndpointType,
				targetConnectableViewId = startPortView!!.owner!!.id,
				targetPortId = startPortView!!.port.portId
			)
		)
	}
}