package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.state.stateMachine
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory
import ch.scorpion.jabbah.graph.view.port.PortView

// Work in progress..
class FlexibleConnector(
	private val portTypeCond: (PortType) -> Boolean,
	private val edgeViewFactorySupplier: () -> EdgeViewFactory<Any>,
	endpointType: EdgeViewEndpointType
) {

	/** The [VerticeView] from which the new connection originates. */
	private var startVerticeView: VerticeView<*>? = null

	/** The [PortView] in [startVerticeView] from which the new connection originates.  */
	private var startPortView: PortView<*>? = null

	private val stateMachine = stateMachine<EditInputEventContext> {

		state("sense") {
			onEntry { it!!.view.setCursor(Cursor.DEFAULT) }
		}

		state("inside") {
			onEntry { displayPortViewHighlight(it!!) }
			onExit { removePortViewHighlight(it!!) }
		}
	}

	/**
	 * Prepares this [FlexibleConnector] to be used to created [EdgeView]s that the user
	 * starts in the specified [VerticeView].
	 */

	fun useFor(verticeView: VerticeView<*>) {
		startVerticeView = verticeView
		startPortView = null
		stateMachine.start()
	}

	private fun insideStartPortView(x: Double, y: Double): Boolean {
		val pv = startVerticeView!!.getPortViewAtConnectionPoint(x, y)
		if (pv != null && !pv.port.isConnected && portTypeCond.invoke(pv.port.portType)) {
			startPortView = pv
		}
		else {
			startPortView = null
		}
		return startPortView != null
	}

	private fun displayPortViewHighlight(context: EditInputEventContext) {
		ConnectionPointHighlighter.displayPortViewHighlight(
			context.drawingView(),
			startVerticeView!!.getPortConnectionPoint(startPortView!!.port))
		context.view.setCursor(Cursor.CROSSHAIR)
	}

	private fun removePortViewHighlight(context: EditInputEventContext) {
		ConnectionPointHighlighter.removePortViewHighlight(context.drawingView())
		startPortView = null
	}

}