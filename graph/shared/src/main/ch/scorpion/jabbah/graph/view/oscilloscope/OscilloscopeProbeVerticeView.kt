package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.connect.ConnectionPointHighlighter
import ch.scorpion.jabbah.graph.view.port.GenericPortView
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import ch.scorpion.jabbah.io.*

/**
 * The location of this [OscilloscopeProbeVerticeView] as a [Locatable] is the tip of the bubble shape, which is also
 * the connection point.
 * @param T the type of signal that this [OscilloscopeProbeVerticeView]'s [OscilloscopeProbeVertice] can consume.
 */
class OscilloscopeProbeVerticeView<T : Any>(
	rowNumber: Int = 1,
	color: CompositeColor = CompositeColor(),
	model: OscilloscopeProbeVertice<T>? = OscilloscopeProbeVertice(name = rowNumber.toString()),
	styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractRectangularVerticeView<OscilloscopeProbeVertice<T>>(styleProvider, model) {

	companion object {
		private val LOG by logger(OscilloscopeProbeVerticeView::class)
		private const val CONN_POINT_SIZE = 4.0
	}

	init {
		visible = false
		modelExchanged(null)
		setBounds(
			-CONN_POINT_SIZE, -OscilloscopeProbeViewDrawable.SIZE,
			OscilloscopeProbeViewDrawable.SIZE + CONN_POINT_SIZE, OscilloscopeProbeViewDrawable.SIZE + CONN_POINT_SIZE)
	}

	var rowNumber: Int
		get() = model!!.getPort<Any>().name!!.toInt()
		set(value) {
			invalidate()
			model!!.getPort<T>().name = value.toString()
			drawable.rowNumber = value
			validate()
		}

	var refColor: CompositeColor
		get() = drawable.color
		set(value) {
			drawable.color = value
		}

	private val drawable = OscilloscopeProbeViewDrawable(Point2D(0.0, -OscilloscopeProbeViewDrawable.SIZE), rowNumber, color, styleProvider)

	/** The [EdgeView] to which this [OscilloscopeProbeVerticeView] is connected.*/
	private var edgeView: EdgeView<T>? = null

	private val handler = Handler()

	private var moveLastLocation = Point2D.ZERO

	/** ---- [Component] interface */

	override val type: String? get() = null

	/** ---- [Drawable] interface */

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
		return handler as InputEventHandler<T>
	}

	/** ---- [Storable] interface */

	override fun resolutionDone() {
		super.resolutionDone()
		drawable.rowNumber = rowNumber
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (edgeView != null) {
			writer.writeInt("edgeView", writer.provideIdentity(edgeView!!))
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("edgeView")) {
			reader.requestResolution(this, Reference(
				name = "edgeView",
				referenceId = reader.readInt("edgeView")))
		}
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		super.resolve(reference, referenceResolver)
		if (reference.name == "edgeView") {
			edgeView = referenceResolver.getStorable(reference.referenceId) as EdgeView<T>
		}
	}

	/** ---- [AbstractRectangularVerticeView] */

	override fun modelExchanged(oldModel: OscilloscopeProbeVertice<T>?) {
		super.modelExchanged(oldModel)
		addPortView(GenericPortView<T>(model!!.getInput(), 0, 0, Direction.SOUTH))
	}

	override fun drawImpl(context: DrawContext, drawPortViews: Boolean) {
		super.drawImpl(context, drawPortViews)
		drawable.draw(context)
		if (model!!.isConnected) {
			val connPoint = connectionPoint().subtract(location)
			context.g.color = context.choose(drawable.color).foregroundColor
			context.g.fillOval(
				connPoint.x - CONN_POINT_SIZE,
				connPoint.y - CONN_POINT_SIZE,
				2.0 * CONN_POINT_SIZE,
				2.0 * CONN_POINT_SIZE)
		}
	}

	/** ---- [AbstractGraphElementView] */

	override fun handleStateChanged(event: GraphElementEvent) {
		LOG.debug("State of ProbeView changed")
		super.handleStateChanged(event)
	}

	/** ---- [OscilloscopeProbeVerticeView] */

	/** Returns the connection point at the tip of the drop shape in absolute coordinates.*/
	private fun connectionPoint(): Point2D {
		return getPortConnectionPoint(model!!.getPort<Any>())
	}

	private inner class Handler : InputEventHandlerAdapter<EditInputEventContext>() {

		override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			LOG.debug("OscilloscopeProbeVerticeView pressed ${context.x},${context.y}")
			moveLastLocation = Point2D(context.location)
			return this
		}

		override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			LOG.debug("OscilloscopeProbeVerticeView dragged ${context.x},${context.y}")

			// Snap
			val dx = context.x - moveLastLocation.x
			val dy = context.y - moveLastLocation.y
			var offset = Point2D.ZERO
			if (context.editor.snapManager.snapEnabled) {
				offset = context.editor.snapManager.snap(this@OscilloscopeProbeVerticeView, dx, dy)
			}

			// Perform drag
			moveBy(dx + offset.x, dy + offset.y)
			moveLastLocation = Point2D(context.x + offset.x, context.y + offset.y)
			validate()

			// Sensing EdgeView
			if (findEdgeView(context) != null) {
				LOG.debug("OscilloscopeProbeVerticeView found EdgeView at ${context.x},${context.y}")
				ConnectionPointHighlighter.displayPortViewHighlight(context.drawingView(), connectionPoint())
			} else {
				ConnectionPointHighlighter.removePortViewHighlight(context.drawingView())
			}

			return this
		}

		override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			LOG.debug("OscilloscopeProbeVerticeView released ${context.x},${context.y}")
			ConnectionPointHighlighter.removePortViewHighlight(context.drawingView())

			// TODO Create Commands

			invalidate()
			val newEdgeView = findEdgeView(context) as EdgeView<T>?
			if (edgeView != null && edgeView !== newEdgeView) {
				edgeView!!.model!!.unconnect(model!!.getPort<T>())
			}
			if (newEdgeView != null && newEdgeView !== edgeView) {
				newEdgeView.model!!.connect(model!!.getPort())
			}
			edgeView = newEdgeView

			invalidate()
			validate()

			return null
		}

		private fun findEdgeView(context: EditInputEventContext): EdgeView<*>? {
			return context.drawingView().drawing.getDrawable { it.contains(connectionPoint()) && it is EdgeView<*> } as EdgeView<*>?
		}
	}
}