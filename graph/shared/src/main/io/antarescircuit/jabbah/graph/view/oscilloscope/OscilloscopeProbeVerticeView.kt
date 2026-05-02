package io.antarescircuit.jabbah.graph.view.oscilloscope

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.KeyEvent
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rotation
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.*
import io.antarescircuit.jabbah.draw.drawable.Locatable
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.model.GenericGraphType
import io.antarescircuit.jabbah.graph.model.GraphElementEvent
import io.antarescircuit.jabbah.graph.model.GraphType
import io.antarescircuit.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import io.antarescircuit.jabbah.graph.view.AbstractGraphElementView
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.app.oscilloscope.DropOscilloscopeProbeCommand
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import io.antarescircuit.jabbah.graph.view.port.GenericPortView
import io.antarescircuit.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import io.antarescircuit.jabbah.io.*

data class OscilloscopeProbeNameEvent(
	val source: OscilloscopeProbeVerticeView<*>,
	val oldName: String,
	val newName: String
)

/**
 * The location of this [OscilloscopeProbeVerticeView] as a [Locatable] is the tip of the bubble shape, which is also
 * the connection point.
 *
 * @param T the type of signal that this [OscilloscopeProbeVerticeView]'s [OscilloscopeProbeVertice] can consume.
 * @param dragGhost `true` when dragging this [OscilloscopeProbeVerticeView] into the drawing, `false` if it already
 * was part of the drawing at the start of a drag operation
 */
open class OscilloscopeProbeVerticeView<T : Any>(
	name: String = "",
	graphType: GraphType = GenericGraphType,
	color: CompositeColor = CompositeColor(),
	model: OscilloscopeProbeVertice<T> = OscilloscopeProbeVertice.create(name, graphType),
	dragGhost: Boolean = false,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractRectangularVerticeView<OscilloscopeProbeVertice<T>>(styleProvider, model) {

	companion object {
		private val LOG by logger(OscilloscopeProbeVerticeView::class)
		private const val CONN_POINT_SIZE = 4.0

		const val MAX_PROBE_NAME_LENGTH = 3
	}

	init {
		modelExchanged(null)
		setBounds(
			-CONN_POINT_SIZE, -OscilloscopeProbeViewIcon.SIZE,
			OscilloscopeProbeViewIcon.SIZE + CONN_POINT_SIZE, OscilloscopeProbeViewIcon.SIZE + CONN_POINT_SIZE)
	}

	var name: String
		get() = model.getPort<Any>().name!!
		set(value) {
			if (name != value) {
				check(value.length <= MAX_PROBE_NAME_LENGTH) { Translations.getString("graph.property.probeNameTooLong.msg", MAX_PROBE_NAME_LENGTH) }
				val oldName = name
				invalidate()
				model.getPort<T>().name = value
				icon.name = value
				BaseModule.eventBus.post(OscilloscopeProbeNameEvent(this, oldName, value))
				validate()
			}
		}

	var refColor: CompositeColor
		get() = icon.color
		set(value) {
			icon.color = value
		}

	var dragGhost: Boolean = dragGhost
		private set

	private val icon = OscilloscopeProbeViewIcon(name, color)

	/** The [EdgeView] to which this [OscilloscopeProbeVerticeView] is connected.*/
	var edgeView: EdgeView<*>? = null

	private val handler = Handler()

	/** ---- [Drawable] interface */

	@Suppress("UNCHECKED_CAST")
	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> =
		handler as InputEventHandler<T>

	/** ---- [Storable] interface */

	override fun resolutionDone() {
		super.resolutionDone()
		icon.name = name
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (edgeView != null) {
			writer.writeInt("edgeView", writer.provideIdentity(edgeView!!))
		}
		writer.writeBoolean("visible", visible)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("edgeView")) {
			reader.requestResolution(this, Reference(
				name = "edgeView",
				referenceId = reader.readInt("edgeView")))
		}
		if (reader.hasAttribute("visible")) {
			visible = reader.readBoolean("visible")
		}
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		super.resolve(reference, referenceResolver)
		if (reference.name == "edgeView") {
			edgeView = referenceResolver.getStorable(reference.referenceId)!!
		}
	}

	/** ---- [Component] */

	override fun rotationChanged(newRotation: Rotation) {
		super.rotationChanged(newRotation)
		icon.ownerRotation = rotation
	}

	override val isDragManager: Boolean get() = true

	override val copyable: Boolean get() = false

	/** ---- [AbstractRectangularVerticeView] */

	override fun modelExchanged(oldModel: OscilloscopeProbeVertice<T>?) {
		super.modelExchanged(oldModel)
		addPortView(GenericPortView<T>(model.getInput(), 0, 0, Direction.SOUTH))
	}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)
		icon.draw(context, Point2D(0.0, -icon.dim.width))
		if (model.isConnected) {
			val connPoint = connectionPoint().subtract(location)
			context.g.color = context.choose(icon.color).foregroundColor
			context.g.fillOval(
				connPoint.x - CONN_POINT_SIZE,
				connPoint.y - CONN_POINT_SIZE,
				2.0 * CONN_POINT_SIZE,
				2.0 * CONN_POINT_SIZE)
		}
	}

	/** ---- [AbstractGraphElementView] */

	override fun handleStateChanged(event: GraphElementEvent) {
		LOG.trace("State of ProbeView changed")
		super.handleStateChanged(event)
	}

	/** ---- [OscilloscopeProbeVerticeView] */

	/** Returns the connection point at the tip of the drop shape in absolute coordinates.*/
	fun connectionPoint(): Point2D {
		return getPortConnectionPoint(model.getPort<Any>())
	}

	private inner class Handler : InputEventHandlerAdapter<EditInputEventContext>() {

		private var isMouseDown = false
		private var isDragging = false
		private var moveLastLocation = Point2D.ZERO

		override fun keyPressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			if (!isMouseDown) {
				return null
			}

			if (context.keyEvent?.key == KeyEvent.VK_ESCAPE) {
				cancelDrag(context.drawingView.drawing)
				return null
			}

			return this
		}

		override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			if (!isMouseDown) {
				if (dragGhost) {
					LOG.userTrail("Start dragging Oscilloscope probe $id '$name' into GraphView")
				} else {
					LOG.userTrail("Start moving Oscilloscope probe $id '$name' within GraphView")
				}
				isMouseDown = true
				isDragging = false
				moveLastLocation = context.location
			}
			return this
		}

		override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext> {
			if (!isDragging && moveLastLocation.distance(context.location) < Editor.DRAG_THRESHOLD) {
				return this
			}

			isDragging = true

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
			edgeView = findEdgeView(context)
			if (edgeView != null) {
				ConnectionPointHighlighter.displayPortViewHighlight(context.drawingView, connectionPoint())
			} else {
				ConnectionPointHighlighter.removePortViewHighlight()
			}

			return this
		}

		override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			isMouseDown = false
			if (!isDragging) {
				cancelDrag(context.drawingView.drawing)
				return null
			}

			ConnectionPointHighlighter.removePortViewHighlight()

			if (dragGhost) {
				context.drawingView.drawing.remove(this@OscilloscopeProbeVerticeView)
			}

			@Suppress("UNCHECKED_CAST")
			val command = DropOscilloscopeProbeCommand(
				context.drawingView as DrawingView<GraphElementView<*>, GraphView>,
				name,
				connectionPoint(),
				probeVerticeViewId = if (dragGhost) null else this@OscilloscopeProbeVerticeView.id
			)
			EditModule.commandManager.execute(command)

			if (command.connectedEdgeViewId == null) {
				if (dragGhost) {
					LOG.userTrail("Dropped Oscilloscope probe '$name' into graph")
				} else {
					LOG.userTrail("Moved Oscilloscope probe '$name' within graph")
				}
			} else {
				if (dragGhost) {
					LOG.userTrail("Dropped Oscilloscope probe '$name' in graph, connected to EdgeView ${command.connectedEdgeViewId}")
				} else {
					LOG.userTrail("Moved Oscilloscope probe '$name' within graph, connected to EdgeView ${command.connectedEdgeViewId}")
				}
			}

			isDragging = false

			return null
		}

		private fun cancelDrag(drawing: Drawing<*>) {
			if (dragGhost) {
				// Unset dragGhost so that OscilloscopeView recognizes that it must re-establish the
				// probe in its origin row
				dragGhost = false
				drawing.remove(this@OscilloscopeProbeVerticeView)
			}
		}

		private fun findEdgeView(context: EditInputEventContext): EdgeView<*>? =
			context.drawingView.drawing.getDrawable { it.contains(connectionPoint()) && it is EdgeView<*> } as EdgeView<*>?
	}
}