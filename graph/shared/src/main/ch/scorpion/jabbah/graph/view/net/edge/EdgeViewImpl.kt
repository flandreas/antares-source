package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.polyline.ArrowHead
import ch.scorpion.jabbah.draw.polyline.PolylineShape
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.net.NetImpl
import ch.scorpion.jabbah.graph.view.connect.DragEdgeViewDestinationConnector
import ch.scorpion.jabbah.graph.view.connect.DragEdgeViewOriginConnector
import ch.scorpion.jabbah.graph.view.connect.EdgeToPortConnector
import ch.scorpion.jabbah.graph.view.net.netview.AbstractNetViewElement
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.io.*


/**
 * A standard implementation of the [EdgeView] interface
 */
open class EdgeViewImpl<T: Any>(
        styleProvider: StyleProvider,
        private val edgeToPortConnectorSupplier: () -> EdgeToPortConnector,
        origEndpointConnectorSupplier: () -> DragEdgeViewOriginConnector,
        destEndpointConnectorSupplier: () -> DragEdgeViewDestinationConnector,
        currentSystemSpeedCategory: CurrentSystemSpeedCategory,
        net: Net<T>
) : AbstractNetViewElement<T>(styleProvider, currentSystemSpeedCategory, net), EdgeView<T> {

    private companion object {
        val LOG by logger(EdgeViewImpl::class)
        val CONTAINS_SIZE = 4
    }

    constructor(
            styleProvider: StyleProvider,
            edgeToPortConnectorSupplier: () -> EdgeToPortConnector,
            origEndpointConnectorSupplier: () -> DragEdgeViewOriginConnector,
            destEndpointConnectorSupplier: () -> DragEdgeViewDestinationConnector,
            currentSystemSpeedCategory: CurrentSystemSpeedCategory
    ): this(styleProvider, edgeToPortConnectorSupplier, origEndpointConnectorSupplier, destEndpointConnectorSupplier,
            currentSystemSpeedCategory, NetImpl<T>())

    @Suppress("unused")
    constructor(): this(
        DrawStyleModule.styleProvider,
        {GraphViewModule.edgeToPortConnector},
        {GraphViewModule.dragEdgeViewOriginConnector},
        {GraphViewModule.dragEdgeViewDestinationConnector},
        ExecutionModule.currentSystemSpeedCategory
    )

    init {
        modelExchanged(null)
    }

    /** Listens for geometry updates of the [ConnectableView]s to which this [EdgeView] is connected.*/
    private val connectableViewListener = ConnectableViewListener()

    // TODO How to initialize EdgeViewStyling when new EdgeViews are created while interacting with Tools?
    // The proper styling should be derived from adjacent EdgeViews.
    private var styling: EdgeViewStyling = NetViewStyle.LINE.createEdgeViewStyling(styleProvider, this)

    /** Indicates that this [EdgeViewImpl] should not perform an origin layout.  */
    private var suspendOriginLayout: Boolean = false

    /** Indicates that this [EdgeViewImpl] should not perform a destination layout.  */
    private var suspendDestinationLayout: Boolean = false

    /** ---- [Any] */

    override fun toString(): String {
        return "${super.toString()} origin=${origin?.id ?: "null"} dest=${destination?.id ?: "null"}"
    }

    /** ---- [EdgeView] interface */

    override var layout = Layout.ORTHOGONAL
        set(value) {
            if (value == field) {
                return
            }
            field = value
            if (parent != null) {
                layoutAll(null, null)
            }
        }

    override var polyline: PolylineShape = DrawModule.polylineShapeFactory.invoke(null)

    override var origin: ConnectableView? = null

    override var originPort: Port<T>? = null

    override var destination: ConnectableView? = null

    override var destinationPort: Port<T>? = null

    override val originEndpointView: EdgeEndpointView = EdgeEndpointView(this, origEndpointConnectorSupplier, styleProvider)

    override val destinationEndpointView: EdgeEndpointView = EdgeEndpointView(this, destEndpointConnectorSupplier, styleProvider)

    override val segmentPointCount: Int get() = polyline.pointsCount

    override val length: Double get() = polyline.getLength()

    override val width: Int get() = styling.width

    override var isAdjusted: Boolean = false

    override var isArrow: Boolean = false
        set(value) {
            if (field == value) {
                return
            }
            invalidate()
            field = value
            polyline.endLineTerminator = if (value) ArrowHead() else null
            styling.updateBoundingBox()
            invalidate()
            update()
        }

    override val connectionState: EdgeViewConnectionState
        get() {
            return if (originPort != null) {
                if (destinationPort != null) {
                    if (originPort!!.portType.isInput) {
                        if (destinationPort!!.portType.isInput) {
                            EdgeViewConnectionState.InputInput
                        } else {
                            EdgeViewConnectionState.InputOutput
                        }
                    } else {
                        if (destinationPort!!.portType.isInput) {
                            EdgeViewConnectionState.InputOutput
                        } else {
                            EdgeViewConnectionState.OutputOutput
                        }
                    }
                } else {
                    portToEdgeViewConnectionState(originPort!!)
                }
            } else if (destinationPort != null) {
                    portToEdgeViewConnectionState(destinationPort!!)
            } else {
                EdgeViewConnectionState.Unconnected
            }
        }

    private fun portToEdgeViewConnectionState(port: Port<*>): EdgeViewConnectionState {
        return if(port.portType.isInput) EdgeViewConnectionState.Input else EdgeViewConnectionState.Output
    }

    override fun getConnectableView(port: Port<T>): ConnectableView? {
        if (port == originPort) {
            return origin
        }
        if (port == destinationPort) {
            return destination
        }
        return null
    }

    override fun getSegmentPoint(index: Int): Point2D {
        return Point2D(polyline.getPointAt(index))
    }

    override fun addSegmentPoint(point: Point2D) {
        addSegmentPoint(segmentPointCount, point)
    }

    override fun addSegmentPoint(index: Int, point: Point2D) {
        invalidate()
        polyline.addPointAt(index, point.x, point.y)
        updateEndpointViews()
        styling.updateBoundingBox()
        invalidate()
    }

    override fun compact() {
        invalidate()
        if (polyline.compact()) {
            updateAdjusted()
            updateEndpointViews()
            styling.updateBoundingBox()
            invalidate()
        }
    }

    override fun connectToOrigin(origin: ConnectableView?, port: Port<T>?) {
        checkArgument(port == null || origin != null)
        if (this.origin != null) {
            this.origin?.removeDrawableListener(connectableViewListener)
            this.origin?.handleUnconnect(this, originPort)
        }
        this.origin = origin
        this.originPort = port
        if (this.origin != null) {
            this.origin?.addDrawableListener(connectableViewListener)
            origin?.handleConnect(this, port)
        }

        updateEndpointViews()
        styling.updateBoundingBox()
    }

    override fun connectToDestination(destination: ConnectableView?, port: Port<T>?) {
        checkArgument(port == null || destination != null)

        if (this.destination != null) {
            this.destination?.removeDrawableListener(connectableViewListener)
            this.destination?.handleUnconnect(this, destinationPort)
        }
        this.destination = destination
        this.destinationPort = port
        if (this.destination != null) {
            this.destination?.addDrawableListener(connectableViewListener)
            destination?.handleConnect(this, port)
        }
        updateEndpointViews()
        styling.updateBoundingBox()
    }

    override fun calculateMaximumNetLength(reverse: Boolean): Double {
        val cv = if (reverse) origin else destination
        if (cv != null && cv is NodeView<*>) {
            var maxSubnetLength = 0.0
            cv.getEdgeViews().forEach {
                if (it !== this) {
                    val subnetLength = it.calculateMaximumNetLength(cv === it.destination)
                    if (subnetLength > maxSubnetLength) {
                        maxSubnetLength = subnetLength
                    }
                }
            }
            return length + maxSubnetLength
        }
        return length
    }

    override fun moveOriginEndPoint(x: Double, y: Double) {
        LOG.trace("moveOriginEndPoint to ($x,$y)")
        invalidate()
        moveOriginEndPointImpl(x, y)
        layoutOrigin()
        invalidate()
        update()
    }

    private fun moveOriginEndPointImpl(x: Double, y: Double) {
        polyline.removePoint(0)
        addSegmentPoint(0, Point2D(x, y))
    }

    override fun moveDestinationEndPoint(x: Double, y: Double) {
        LOG.trace("moveDestinationEndPoint to ($x,$y)")
        invalidate()
        moveDestinationEndPointImpl(x, y)
        layoutDestination()
        invalidate()
        update()
    }

    private fun moveDestinationEndPointImpl(x: Double, y: Double) {
        polyline.removePoint(polyline.pointsCount - 1)
        // TODO Snap?
        addSegmentPoint(Point2D(x, y))
    }

    override fun movePoint(index: Int, x: Double, y: Double) {
        invalidate()
        polyline.removePoint(index)
        polyline.addPointAt(index, x, y)
        styling.updateBoundingBox()
        invalidate()
        update()
    }

    override fun findSegment(x: Double, y: Double): Int? {
        return findSegment(x, y, CONTAINS_SIZE)
    }

    override fun findSegment(x: Double, y: Double, area: Int): Int? {
        return polyline.findSegment(x, y, area)
    }

    override fun findSegmentPoint(x: Double, y: Double, area: Int): Int? {
        return polyline.findPoint(x, y, area)
    }

    override fun moveSegment(segmentIndex: Int, from: Point2D, to: Point2D): MoveEdgeSegmentInfo {
        val segmentDirection = getSegmentDirection(segmentIndex)
        if (segmentDirection != null) {
            val moveDirection = segmentDirection.next().abs()
            val dx = moveDirection.dx * (to.x - from.x)
            val dy = moveDirection.dy * (to.y - from.y)
            return moveSegment(segmentIndex, if (dx == 0.0) dy else dx)
        }
        return moveSegment(segmentIndex, 0.0)
    }

    override fun moveSegment(segmentIndex: Int, offset: Double): MoveEdgeSegmentInfo {
        var index = segmentIndex
        if (offset == 0.0) {
            return MoveEdgeSegmentInfo(index, 0.0)
        }

        val segmentDirection = getSegmentDirection(index) ?: return MoveEdgeSegmentInfo(index, 0.0)

        invalidate()

        val moveDirection = segmentDirection.next().abs()
        val dx = moveDirection.dx * offset
        val dy = moveDirection.dy * offset

        var newOrigNodePoint: Point2D? = null
        var newDestNodePoint: Point2D? = null

        if (index == 0) {
            if (origin is NodeView<*>) {
                newOrigNodePoint = Point2D(polyline.getPointAt(index)).add(dx, dy)
                if ((origin as NodeView<*>).anyEdgeViewContainsPoint(newOrigNodePoint.x, newOrigNodePoint.y, this)) {
                    polyline.setPointAt(0, newOrigNodePoint.x, newOrigNodePoint.y)
                } else {
                    newOrigNodePoint = null
                }
            }
            if (newOrigNodePoint == null) {
                if (splitOriginSegmentForMove()) {
                    index++
                }
                val newPoint = Point2D(polyline.getPointAt(index)).add(dx, dy)
                polyline.addPointAt(index + 1, newPoint.x, newPoint.y)
                index++
            }
        } else {
            val p = polyline.getPointAt(index)
            polyline.setPointAt(index, p.x + dx, p.y + dy)
            if (index == 1 && !checkOriginSegmentLength()) {
                // Reset
                polyline.setPointAt(index, p.x, p.y)
                return MoveEdgeSegmentInfo(index, 0.0)
            }
        }

        if (index == polyline.pointsCount - 2) {
            if (destination is NodeView<*>) {
                val nodeView = destination as NodeView<*>
                newDestNodePoint = Point2D(polyline.getPointAt(index + 1)).add(dx, dy)
                if (nodeView.anyEdgeViewContainsPoint(newDestNodePoint.x, newDestNodePoint.y, this)) {
                    polyline.setPointAt(index + 1, newDestNodePoint.x, newDestNodePoint.y)
                } else {
                    newDestNodePoint = null
                }
            }
            if (newDestNodePoint == null) {
                splitDestinationSegmentForMove()
                val newPoint = Point2D(polyline.getPointAt(index + 1)).add(dx, dy)
                polyline.addPointAt(index + 1, newPoint.x, newPoint.y)
            }
        } else {
            val p = polyline.getPointAt(index + 1)
            polyline.setPointAt(index + 1, p.x + dx, p.y + dy)
            if (index == polyline.pointsCount - 3 && !checkDestinationSegmentLength()) {
                // Reset
                polyline.setPointAt(
                        index,
                        polyline.getPointAt(index).x - dx,
                        polyline.getPointAt(index).y - dy)
                polyline.setPointAt(index + 1, p.x, p.y)
                return MoveEdgeSegmentInfo(index, 0.0)
            }
        }

        isAdjusted = true

        suspendOriginLayout = true
        suspendDestinationLayout = true

        if (newOrigNodePoint != null) {
            (origin as NodeView<*>).location = Point2D(newOrigNodePoint.x, newOrigNodePoint.y)
        }
        if (newDestNodePoint != null) {
            (destination as NodeView<*>).location = Point2D(newDestNodePoint.x, newDestNodePoint.y)
        }

        val center = polyline.getCenterOfSegment(index)
        compact()

        updateEndpointViews()
        styling.updateBoundingBox()
        invalidate()
        update()

        suspendOriginLayout = false
        suspendDestinationLayout = false

        return MoveEdgeSegmentInfo(findSegment(center.x, center.y)!!, offset)
    }

    override fun getSegmentDirection(segmentIndex: Int): Direction? {
        return layout.getSegmentDirection(this, segmentIndex)
    }

    override fun layoutOrigin() {
        layoutOrigin(null)
    }

    override fun layoutOrigin(direction: Direction?) {
        LOG.debug("EdgeViewImpl: layoutOrigin")
        if (!isAdjusted) {
            layoutAll(direction, null)
            return
        }
        val originPoint = getLayoutOriginPoint()
        if (originPoint != null) {
            val destPointIndex = Math.min(2, polyline.pointsCount - 1)
            val destPoint = Point2D(polyline.getPointAt(destPointIndex))
            val originDirs = if (direction == null) getOriginDirections(destPoint) else setOf(direction)
            val destDir = getSegmentDirection(destPointIndex - 1)
            val list = mutableListOf<Point2D>()
            list.addAll(layout.layout(
                    this,
                    parent as GraphView<*>,
                    LayoutBoundary(
                            point = originPoint,
                            directions = originDirs,
                            isPort = origin != null || direction != null),
                    LayoutBoundary(
                            point = destPoint,
                            directions = destDir?.let { setOf(destDir) } ?: setOf(),
                            isPort = destination != null)))

            list.addAll(polyline.getPoints(destPointIndex, polyline.pointsCount))

            invalidate()
            polyline.setPoints(list)

            updateEndpointViews()
            styling.updateBoundingBox()
            invalidate()

            compact()

            update()
        }

        updateAdjusted()
    }

    override fun layoutDestination() {
        layoutDestination(null)
    }

    override fun layoutDestination(direction: Direction?) {
        LOG.trace("EdgeViewImpl: layoutDestination")
        if (!isAdjusted) {
            layoutAll(null, direction)
            return
        }
        val destPoint = getLayoutDestinationPoint()
        if (destPoint != null) {
            val origPointIndex = Math.max(0, polyline.pointsCount - 3)
            val origPoint = Point2D(polyline.getPointAt(origPointIndex))
            val destDirs = if (direction == null) getDestinationDirections(origPoint) else setOf(direction)
            val origDir = getSegmentDirection(origPointIndex)
            val list = mutableListOf<Point2D>()
            list.addAll(layout.layout(
                    this,
                    parent as GraphView<*>,
                    LayoutBoundary(
                            point = origPoint,
                            directions = origDir?.let { setOf(origDir) } ?: setOf(),
                            isPort = origin != null),
                    LayoutBoundary(
                            point = destPoint,
                            directions = destDirs,
                            isPort = destination != null || direction != null)))
            list.addAll(0, polyline.getPoints(0, origPointIndex))

            invalidate()
            polyline.setPoints(list)

            updateEndpointViews()
            styling.updateBoundingBox()
            invalidate()

            compact()
            update()
        }

        updateAdjusted()
    }

    override val isDegenerated: Boolean
        get() = segmentPointCount < 2 || isOriginDegenerated() || isDestinationDegenerated()

    private fun isOriginDegenerated(): Boolean {
        return polyline.getPointAt(0) == polyline.getPointAt(1)
    }

    private fun isDestinationDegenerated(): Boolean {
        return polyline.getPointAt(polyline.pointsCount - 1) == polyline.getPointAt(polyline.pointsCount - 2)
    }

    override fun isSegmentOrthogonal(index: Int): Boolean {
        return polyline.isSegmentOrthogonal(index)
    }

    override fun split(index: Int, splitLocation: Point2D, edgeViewCreator: (Net<*>) -> EdgeView<*>): EdgeView<*> {
        val tail = edgeViewCreator.invoke(model!!) as EdgeView<T>

        if (isArrow) {
            tail.isArrow = true
            isArrow = false
        }
        while (segmentPointCount - 1 > index) {
            tail.addSegmentPoint(0, getSegmentPoint(segmentPointCount - 1))
            polyline.removePoint(segmentPointCount - 1)
        }

        if (splitLocation != polyline.getLastPoint()) {
            addSegmentPoint(Point2D(Point2D(splitLocation)))
        }
        if (splitLocation != tail.getSegmentPoint(0)) {
            tail.addSegmentPoint(0, Point2D(splitLocation))
        }

        tail.layout = this.layout
        tail.isAdjusted = this.isAdjusted
        tail.isArrow = this.isArrow

        val oldConnectionState = connectionState
        val oldDest = destination
        val oldDestPort = destinationPort

        if (oldConnectionState == EdgeViewConnectionState.InputInput) {
            val orig = origin
            val origPort = originPort
            if (orig != null) {
                connectToOrigin(null, null)
                connectToDestination(orig, origPort)
            }
        }

        if (oldDest != null) {
            if (oldConnectionState != EdgeViewConnectionState.InputInput) {
                connectToDestination(null, null)
            }
            tail.connectToDestination(oldDest, oldDestPort)
        }

        return tail
    }

    override fun join(other: EdgeView<T>): EdgeView<*> {
        if (polyline.getLastPoint() == other.polyline.getFirstPoint()) {
            return joinOtherHeadWithTail(other)
        } else if (polyline.getFirstPoint() == other.polyline.getLastPoint()) {
            return joinOtherTailWithHead(other)
        } else if (polyline.getFirstPoint() == other.polyline.getFirstPoint()) {
            return joinOtherHeadWithHead(other)
        } else {
            throw IllegalArgumentException("joined EdgeView is not adjacent")
        }
    }

    /** ---- [Locatable] interface */

    override var location: Point2D
        set(value) {
            invalidate()
            polyline.setLocation(value.x, value.y)
            styling.updateBoundingBox()
            invalidate()
            update()
        }
        get() = Point2D(polyline.getFirstPoint())

    override fun prepareMoveBy(components: Collection<Locatable>) {
        val originIfExistsMoves = origin == null || components.contains(origin as Locatable)
        val destIfExistsMoves = destination == null || components.contains(destination as Locatable)
        val thisMoves = originIfExistsMoves && destIfExistsMoves

        suspendOriginLayout = originIfExistsMoves && thisMoves
        suspendDestinationLayout = destIfExistsMoves && thisMoves
    }

    override fun moveBy(dx: Double, dy: Double) {
        LOG.debug("moveBy")

        // An EdgeView does only move if all ConnectableView it is connected to are moved as well
        if (origin != null && !suspendOriginLayout || destination != null && !suspendDestinationLayout) {
            return
        }

        originEndpointView.moveBy(dx, dy)
        destinationEndpointView.moveBy(dx, dy)

        // This leads to setLocation() which does the invalidation and update things, so there
        // is no need to do it here as well
        super<AbstractNetViewElement>.moveBy(dx, dy)
    }

    override fun completeMoveBy() {
        suspendOriginLayout = false
        suspendDestinationLayout = false
    }

    /** ---- [Drawable] interface */

    override val boundingBox: Rectangle2D get() = styling.boundingBox

    override fun contains(x: Double, y: Double): Boolean {
        return findSegment(x, y) != null
    }

    override fun getTooltip(x: Double, y: Double): Tooltip? {
        val designError = net!!.designError ?: return super.getTooltip(x, y)
        return Tooltip(designError.description, x, y)
    }

    override fun accept(visitor: HierarchyVisitor): Boolean {
        if (visitor.visitEnter(this)) {
            destination?.accept(visitor)
        }
        return visitor.visitLeave(this)
    }

    override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
        LOG.trace("getInputEventHandler at " + Point2D(context.x, context.y))

        if (context.mouseEvent != null) {
            if (destination == null && destinationEndpointView.contains(context.x, context.y)) {
                return destinationEndpointView.getInputEventHandler(context)
            }
            if (origin == null && originEndpointView.contains(context.x, context.y)) {
                return originEndpointView.getInputEventHandler(context)
            }
            if (context.mouseEvent!!.isAltDown) {
                edgeToPortConnectorSupplier.invoke().useFor(this)
                return edgeToPortConnectorSupplier.invoke() as InputEventHandler<T>
            }
        }
        return super.getInputEventHandler(context)
    }

    override fun draw(context: DrawContext) {
        styling.draw(context)
    }

    /** ---- [NetViewElement] interface */

    override val net: Net<T>? get() = model

    override fun handleNetViewStyleChanged() {
        invalidate()
        styling = netView!!.style.createEdgeViewStyling(styleProvider, this)
        styling.updateBoundingBox()
        origin?.handleEdgeViewWidthChanged(this)
        destination?.handleEdgeViewWidthChanged(this)
        invalidate()
        validate()
    }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("layout", layout.customName)
        if (isArrow) {
            writer.writeBoolean("arrow", true)
        }
        if (origin != null) {
            writer.writeInt("orig", writer.provideIdentity(origin!!))
            if (originPort != null) {
                writer.writeInt("origPort", originPort!!.portId)
                if (originPort!!.portType.isOutput) {
                    writer.writeBoolean("origPortOutput", true)
                }
            }
        }
        if (destination != null) {
            writer.writeInt("dest", writer.provideIdentity(destination!!))
            if (destinationPort != null) {
                writer.writeInt("destPort", destinationPort!!.portId)
                if (destinationPort!!.portType.isOutput) {
                    writer.writeBoolean("destPortOutput", true)
                }
            }
        }
        writer.writeBoolean("adjusted", isAdjusted)
        writer.writePoints("shape", "polylineShape", "points", polyline.getPoints(0, polyline.pointsCount))
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        if (reader.hasAttribute("layout")) {
            layout = Layout.withName(reader.readString("layout"))
        }
        if (reader.hasAttribute("adjusted")) {
            isAdjusted = reader.readBoolean("adjusted")
        }

        if (reader.hasAttribute("orig")) {
            var portId: Int? = null
            if (reader.hasAttribute("origPort")) {
                portId = reader.readInt("origPort")
            }
            val ref = VerticeViewRef(
                    verticeViewId = reader.readInt("orig"),
                    portId = portId)
            reader.requestResolution(this, Reference(
                    name = "orig",
                    referenceId = ref.verticeViewId,
                    resolveAfter = listOf(ref.verticeViewId),
                    additionalInfo = ref))
        }

        if (reader.hasAttribute("dest")) {
            var portId: Int? = null
            if (reader.hasAttribute("destPort")) {
                portId = reader.readInt("destPort")
            }
            val ref = VerticeViewRef(
                    verticeViewId = reader.readInt("dest"),
                    portId = portId)
            reader.requestResolution(this, Reference(
                    name = "dest",
                    referenceId = ref.verticeViewId,
                    resolveAfter = listOf(ref.verticeViewId),
                    additionalInfo = ref))
        }

        polyline = DrawModule.polylineShapeFactory.invoke(reader.readPoints("shape", "polylineShape", "points"))

        if (reader.hasAttribute("arrow")) {
            isArrow = reader.readBoolean("arrow")
        }
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        super.resolve(reference, referenceResolver)

        if ("orig" == reference.name || "dest" == reference.name) {
            val ref = reference.additionalInfo as VerticeViewRef
            val cv = referenceResolver.getStorable(ref.verticeViewId) as ConnectableView
            var port: Port<*>? = null
            if (ref.portId != null) {
                port = cv.getPort(ref.portId)
            }

            if ("orig" == reference.name) {
                connectToOrigin(cv, port as Port<T>?)
            } else {
                connectToDestination(cv, port as Port<T>?)
            }
        }
    }

    override fun resolutionDone() {
        super<AbstractNetViewElement>.resolutionDone()
        invalidate()
        styling.updateBoundingBox()
    }

    private data class VerticeViewRef(val verticeViewId: Int, val portId: Int?)

    /** ---- [Component] interface */

    override val type: String? get() = Translations.getString("graph.component.edge")

    /** ---- [EdgeViewImpl] */

    private fun joinOtherHeadWithHead(other: EdgeView<T>): EdgeView<*> {
        for (i in 0 until other.segmentPointCount) {
            if (polyline.getFirstPoint() != other.getSegmentPoint(i)) {
                addSegmentPoint(0, other.getSegmentPoint(i))
            }
        }
        compact()
        val origin = other.destination
        val originPort = other.destinationPort
        if (other.destination != null) {
            other.connectToDestination(null, null)
        }
        connectToOrigin(origin, originPort)
        return this
    }

    private fun joinOtherHeadWithTail(other: EdgeView<T>): EdgeView<*> {
        for (i in 0 until other.segmentPointCount) {
            if (polyline.getLastPoint() != other.getSegmentPoint(i)) {
                addSegmentPoint(other.getSegmentPoint(i))
            }
        }
        compact()
        val destination = other.destination
        val destinationPort = other.destinationPort
        if (other.destination != null) {
            other.connectToDestination(null, null)
        }
        connectToDestination(destination, destinationPort)
        return this
    }

    private fun joinOtherTailWithHead(other: EdgeView<T>): EdgeView<*> {
        for (i in other.segmentPointCount - 1 downTo 0) {
            if (polyline.getFirstPoint() != other.getSegmentPoint(i)) {
                addSegmentPoint(0, other.getSegmentPoint(i))
            }
        }
        compact()
        val origin = other.origin
        val originPort = other.originPort
        if (other.origin != null) {
            other.connectToOrigin(null, null)
        }
        connectToOrigin(origin, originPort)
        return this
    }

    private fun layoutAll(originDir: Direction?, destDir: Direction?) {
        LOG.trace("EdgeViewImpl: layoutAll, originDir=$originDir, destDir=$destDir")
        val originPoint = getLayoutOriginPoint()
        val originDirs: Set<Direction>
        val destPoint = getLayoutDestinationPoint()
        val destDirs: Set<Direction>

        if (originPoint != null && destPoint != null) {
            invalidate()

            originDirs = if (originDir != null) setOf(originDir) else getOriginDirections(destPoint)
            destDirs = if (destDir != null) setOf(destDir) else getDestinationDirections(originPoint)

            val layoutedPoints = layout.layout(
                    this,
                    parent as GraphView<*>,
                    LayoutBoundary(
                            point = originPoint,
                            directions = originDirs,
                            isPort = origin != null || originDir != null),
                    LayoutBoundary(
                            point = destPoint,
                            directions = destDirs,
                            isPort = destination != null || destDir != null))

            polyline.clear()
            polyline.setPoints(layoutedPoints)
            updateEndpointViews()
            styling.updateBoundingBox()
            invalidate()
            update()
        }
    }

    /**
     * Updates the locations of both [EdgeEndpointView]s.
     * Doesn't call update bounding box of styling, since that is the responsibility of the caller.
     */
    private fun updateEndpointViews() {
        invalidate()
        if (segmentPointCount > 0) {
            originEndpointView.location = Point2D(polyline.getPointAt(0))
            destinationEndpointView.location = Point2D(polyline.getPointAt(polyline.pointsCount - 1))
        }
    }

    private fun getLayoutOriginPoint(): Point2D? {
        if (origin != null) {
            return origin!!.getPortConnectionPoint(originPort)
        }
        if (polyline.pointsCount > 0) {
            return Point2D(polyline.getPointAt(0))
        }
        return null
    }

    private fun getOriginDirections(refPoint: Point2D): Set<Direction> {
        if (origin != null) {
            return origin!!.getPortConnectionLayoutDirections(this, originPort, refPoint)
        }
        return setOf(Direction.EAST)
    }

    private fun getLayoutDestinationPoint(): Point2D? {
        if (destination != null) {
            return destination!!.getPortConnectionPoint(destinationPort)
        }
        if (polyline.pointsCount >= 2) {
            return Point2D(polyline.getPointAt(polyline.pointsCount - 1))
        }
        return null
    }

    private fun getDestinationDirections(refPoint: Point2D): Set<Direction> {
        if (destination != null) {
            return Direction.oppositeSet(destination!!.getPortConnectionLayoutDirections(this, destinationPort, refPoint))
        }
        return Direction.ALL
    }

    private fun updateAdjusted() {
        if (origin is NodeView<*> || destination is NodeView<*>) {
            isAdjusted = isAdjusted && polyline.pointsCount > 2
        } else {
            isAdjusted = isAdjusted && polyline.pointsCount > 3
        }
    }

    /**
     * Checks whether the origin segment should be split because it is to be moved and the originating
     * [ConnectableView] requires a minimum segment length, and adds a new [Point2D] if necessary.
     * @return `true` if the origin segment has been split and a new [Point2D] has been added
     */
    private fun splitOriginSegmentForMove(): Boolean {
        if (origin == null || originPort == null) {
            return false
        }

        val portView = origin!!.getPortView(originPort!!)
        if (portView!!.minSegmentLength == 0) {
            return false
        }

        val newPoint = Point2D(
                polyline.getFirstPoint().x + portView.relativeDirection.dx * portView.minSegmentLength,
                polyline.getFirstPoint().y + portView.relativeDirection.dy * portView.minSegmentLength)

        polyline.addPointAt(1, newPoint.x, newPoint.y)
        return true
    }

    /**
     * Checks whether the destination segment should be split because it is to be moved and the destination
     * [ConnectableView] requires a minimum segment length, and adds a new [Point2D] if necessary.
     * @return `true` if the destination segment has been split and a new [Point2D] has been added
     */
    private fun splitDestinationSegmentForMove(): Boolean {
        if (destination == null || destinationPort == null) {
            return false
        }

        val portView = destination!!.getPortView(destinationPort!!)
        if (portView!!.minSegmentLength == 0) {
            return false
        }

        val newPoint = Point2D(
                polyline.getLastPoint().x + portView.relativeDirection.dx * portView.minSegmentLength,
                polyline.getLastPoint().y + portView.relativeDirection.dy * portView.minSegmentLength)

        polyline.addPointAt(polyline.pointsCount - 1, newPoint.x, newPoint.y)
        return true
    }

    /**
     * Check whether the origin segment is larger than its required minimum length.
     * @return `true` if larger than its minimum length
     */
    private fun checkOriginSegmentLength(): Boolean {
        if (origin == null || originPort == null) {
            return true
        }
        return polyline.getSegmentLength(0) >= origin!!.getPortView(originPort!!)!!.minSegmentLength
    }

    /**
     * Check whether the destination segment is larger than its required minimum length.
     * @return `true` if larger than its minimum length
     */
    private fun checkDestinationSegmentLength(): Boolean {
        if (destination == null || destinationPort == null) {
            return true
        }
        val portView = destination!!.getPortView(destinationPort!!)
        return polyline.getSegmentLength(polyline.pointsCount - 2) >= portView!!.minSegmentLength
    }

    /**
     * Listens for geometry updates of the [ConnectableView]s to which this [EdgeView] is connected and
     * initiates a re-layout when they are changed.
     */
    private inner class ConnectableViewListener : DrawableAdapter() {
        override fun drawableUpdated(event: DrawableEvent) {
            if (parent == null) {
                // No need to do any layouts while EdgeView is being loaded from persistant storage
                return
            }
            LOG.debug("VerticeView updated")
            if (event.source == origin && !suspendOriginLayout) {
                layoutOrigin()
            }
            if (event.source == destination && !suspendDestinationLayout) {
                layoutDestination()
            }
        }
    }
}