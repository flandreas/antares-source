package ch.scorpion.jabbah.graph.view.net.node

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Snappable
import ch.scorpion.jabbah.edit.SnappableX
import ch.scorpion.jabbah.edit.SnappableY
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.net.NetImpl
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.NetViewElement
import ch.scorpion.jabbah.graph.view.net.netview.AbstractNetViewElement
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter


/**
 * A standard [NodeView] implementation.
 * @param <T> the type of signal that this [NodeView] carries
 */
open class NodeViewImpl<T : Any>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	net: Net<T> = NetImpl(),
	netViewStyle: NetViewStyle? = null
) : AbstractNetViewElement<T>(styleProvider, net), NodeView<T> {

	companion object {
		private const val TYPE = "Node"
	}

	protected var styling: NodeViewStyling = NetViewStyle.LINE.createNodeViewStyling(styleProvider, this)
		private set

	init {
		if (netViewStyle != null) {
			styling = netViewStyle.createNodeViewStyling(styleProvider, this)
		}
	}

	/** ---- [Locatable] interface */

	override var location: Point2D = Point2D.ZERO
		set(value) {
			field = value
			updateGeometry()
		}

	/** ---- [Snappable] interface */

	override val snappableX: Array<SnappableX> get() = emptyArray()

	override val snappableY: Array<SnappableY> get() = emptyArray()

	/** ---- [NetViewElement] interface */

	override val connectedPorts: Set<Port<T>> get() =
		getEdgeViews().flatMap { it.connectedPorts }.toSet()

	override fun handleNetViewStyleChanged() {
		invalidate()
		styling = netView!!.style.createNodeViewStyling(styleProvider, this)
		styling.updateBoundingBox()
		invalidate()
		validate()
	}

	/** ---- [NodeView] interface */

	override fun anyEdgeViewContainsPoint(x: Double, y: Double, excludedEdgeView: EdgeView<*>?): Boolean {
		val incomingCV = getIncomingEdgeView()
		if (incomingCV != null && incomingCV !== excludedEdgeView && incomingCV.polyline.findSegment(x, y, 1) != null) {
			return true
		}
		for (outgoingCV in getOutgoingEdgeViews()) {
			if (outgoingCV !== excludedEdgeView && outgoingCV.polyline.findSegment(x, y, 1) != null) {
				return true
			}
		}
		return false
	}

	override fun getOutgoingEdgeViews(): List<EdgeView<T>> {
		if (parent == null) {
			return emptyList()
		}
		val edgeViews = (parent as GraphView).getEdgeViews()
		return edgeViews
			.filter { it.origin?.connectableView === this }
			.map { it as EdgeView<T> }
	}

	override fun getEdgeViews(): List<EdgeView<T>> {
		if (parent == null) {
			return emptyList()
		}
		val edgeViews = (parent as GraphView).getEdgeViews()
		return edgeViews
			.filter { it.origin?.connectableView === this || it.destination?.connectableView === this }
			.map { it as EdgeView<T> }
	}

	override fun getEdgeView(direction: Direction): EdgeView<T>? {
		val inEv = getIncomingEdgeView()
		if (inEv != null && inEv.getSegmentDirection(inEv.segmentPointCount - 2) == direction.opposite()) {
			return inEv
		}

		return getOutgoingEdgeViews().firstOrNull { it.getSegmentDirection(0) == direction }
	}

	override fun getIncomingEdgeView(): EdgeView<T>? {
		val edgeViews = (parent as GraphView).getEdgeViews()
		return edgeViews
			.filter { it.destination?.connectableView === this }
			.map { it as EdgeView<T> }
			.firstOrNull()
	}

	/** ---- [Component] */

	override val type: String get() = TYPE

	override val deletable: Boolean get() = getEdgeViews().isEmpty()

	/** ---- [Drawable] interface */

	override val boundingBox: Rectangle2D get() = styling.boundingBox

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			val outIter = getOutgoingEdgeViews().iterator()
			while (outIter.hasNext()) {
				if (!outIter.next().accept(visitor)) {
					break
				}
			}
		}
		return visitor.visitLeave(this)
	}

	override fun draw(context: DrawContext) {
		styling.draw(context)
	}

	override fun contains(x: Double, y: Double): Boolean {
		return boundingBox.contains(x, y)
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writePoint("location", location)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		location = reader.readPoint("location")
	}

	override fun resolutionDone() {
		super<AbstractNetViewElement>.resolutionDone()
		invalidate()
		styling.updateBoundingBox()
	}

	/** ---- [ConnectableView] */

	override fun getPortConnectionPoint(port: Port<*>?): Point2D = location

	override fun getUnconnectedPortConnectionPoint(port: Port<*>): Point2D = location

	override fun getPortConnectionLayoutDirections(edgeView: EdgeView<*>, port: Port<*>?, refPoint: Point2D?): Set<Direction> {
		if (refPoint == null) {
			return mutableSetOf(Direction.SOUTH)
		}

		val occupiedDirections = directionsOfEdgeViewsOtherThan(edgeView)
		val prefHorizontalDirections = mutableSetOf<Direction>()
		val prefVerticalDirections = mutableSetOf<Direction>()

		when {
			refPoint.x == location.x -> {
				prefVerticalDirections.add(Direction.NORTH)
				prefVerticalDirections.add(Direction.SOUTH)
			}
			refPoint.x > location.x -> prefHorizontalDirections.add(Direction.EAST)
			else -> prefHorizontalDirections.add(Direction.WEST)
		}

		when {
			refPoint.y == location.y -> {
				prefHorizontalDirections.add(Direction.WEST)
				prefHorizontalDirections.add(Direction.EAST)
			}
			refPoint.y > location.y -> prefVerticalDirections.add(Direction.SOUTH)
			else -> prefVerticalDirections.add(Direction.NORTH)
		}

		prefHorizontalDirections.removeAll(occupiedDirections)
		prefVerticalDirections.removeAll(occupiedDirections)

		val result = mutableSetOf<Direction>()
		result.addAll(prefVerticalDirections)
		result.addAll(prefHorizontalDirections)

		if (result.isEmpty()) {
			return mutableSetOf(Direction.SOUTH)
		}

		return result
	}

	override fun getPort(portId: Int): Port<*>? {
		return null
	}

	override fun <G : Any> handleConnect(edgeView: EdgeView<G>, port: Port<G>?) {
		// empty
	}

	override fun <G : Any> handleUnconnect(edgeView: EdgeView<G>, port: Port<G>?, lockEndpoint: Boolean) {
		// empty
	}

	override fun handleEdgeViewWidthChanged(edgeView: EdgeView<*>) {
		// empty
	}

	override fun <G : Any> getPortView(port: Port<G>): PortView<G>? {
		return null
	}

	/** ---- [NodeViewImpl] */

	private fun updateGeometry() {
		invalidate()
		styling.updateBoundingBox()
		invalidate()
		update()
	}

	/** Collects valid outgoing directions by checking all connected [EdgeView] excluding a particular one. */
	private fun directionsOfEdgeViewsOtherThan(excludedEdgeView: EdgeView<*>): Set<Direction> {
		val directions = mutableSetOf<Direction>()
		for (ev in getEdgeViews()) {
			if (ev !== excludedEdgeView) {
				if (ev.origin?.connectableView === this) {
					// Outgoing
					val segmentDirection = ev.getSegmentDirection(0)
					if (segmentDirection != null) {
						directions.add(segmentDirection)
					}
				} else {
					// Incoming: Excluded Direction is the opposite of the incoming segment
					val segmentDirection = ev.getSegmentDirection(ev.segmentPointCount - 2)
					if (segmentDirection != null) {
						directions.add(segmentDirection.opposite())
					}
				}
			}
		}
		return directions
	}
}