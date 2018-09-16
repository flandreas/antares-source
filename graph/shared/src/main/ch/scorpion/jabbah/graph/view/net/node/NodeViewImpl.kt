package ch.scorpion.jabbah.graph.view.net.node

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.net.NetImpl
import ch.scorpion.jabbah.graph.view.NetViewElement
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
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
open class NodeViewImpl<T: Any>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	currentSystemSpeedCategory: CurrentSystemSpeedCategory = ExecutionModule.currentSystemSpeedCategory,
	net: Net<T> = NetImpl(),
	netViewStyle: NetViewStyle? = null
) : AbstractNetViewElement<T>(styleProvider, currentSystemSpeedCategory, net), NodeView<T> {

    /** Can be `null`during deserialization.*/
    private var styling: NodeViewStyling? = null

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

    /** ---- [NetViewElement] interface */

    override val net: Net<T>? get() = model

    override fun handleNetViewStyleChanged() {
        invalidate()
        styling = netView!!.style.createNodeViewStyling(styleProvider, this)
        styling!!.updateBoundingBox()
        invalidate()
        validate()
    }

    /** ---- [NodeView] interface */

    override fun anyEdgeViewContainsPoint(x: Double, y: Double, excludedEdgeView: EdgeView<*>?): Boolean {
        val incomingCV = getIncomingEdgeView()
        if (incomingCV != null && incomingCV !== excludedEdgeView && incomingCV.findSegment(x, y, 1) != null) {
            return true
        }
        for (outgoingCV in getOutgoingEdgeViews()) {
            if (outgoingCV !== excludedEdgeView && outgoingCV.findSegment(x, y, 1) != null) {
                return true
            }
        }
        return false
    }

    override fun getOutgoingEdgeViews(): List<EdgeView<T>> {
        if (parent == null) {
            return emptyList()
        }
        val edgeViews = (parent as GraphView<*>).getEdgeViews()
        return edgeViews
                .filter { it.origin === this }
                .map { it as EdgeView<T> }
    }

    override fun getEdgeViews(): List<EdgeView<T>> {
        if (parent == null) {
            return emptyList()
        }
        val edgeViews = (parent as GraphView<*>).getEdgeViews()
        return edgeViews
                .filter { it.origin === this || it.destination === this }
                .map { it as EdgeView<T> }
    }

    override fun getEdgeView(direction: Direction): EdgeView<T>? {
        val inEv = getIncomingEdgeView()
        if (inEv != null && inEv.getSegmentDirection(inEv.segmentPointCount - 2) == direction.opposite()) {
            return inEv
        }

        return getOutgoingEdgeViews()
                .filter({ it.getSegmentDirection(0)!! == direction })
                .firstOrNull()
    }

    override fun getIncomingEdgeView(): EdgeView<T>? {
        val edgeViews = (parent as GraphView<*>).getEdgeViews()
        return edgeViews
                .filter { it.destination === this }
                .map { it as EdgeView<T>}
                .firstOrNull()
    }

    /** ---- [Component] */

    override val type: String?
        // Will not be selectable any more
        get() = null

    override val deletable: Boolean get() = false

    /** ---- [Drawable] interface */

    override val boundingBox: Rectangle2D get() = styling!!.boundingBox

    override fun invalidate() {
        if (styling != null) {
            // Don't do invalidation while bootstraping.
            super.invalidate()
        }
    }

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
        styling?.draw(context)
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
        styling?.updateBoundingBox()
    }

    /** ---- [ConnectableView] */

    override fun getPortConnectionPoint(port: Port<*>?): Point2D {
        return location
    }

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

    override fun <G : Any> handleUnconnect(edgeView: EdgeView<G>, port: Port<G>?) {
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
        if (styling != null) {
            styling?.updateBoundingBox()
        }
        invalidate()
        update()
    }

    /** Collects valid outgoing directions by checking all connected [EdgeView] excluding a particular one. */
    private fun directionsOfEdgeViewsOtherThan(excludedEdgeView: EdgeView<*>): Set<Direction> {
        val directions = mutableSetOf<Direction>()
        for (ev in getEdgeViews()) {
            if (ev !== excludedEdgeView) {
                if (ev.origin === this) {
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