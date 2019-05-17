package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.polyline.ArrowHead
import ch.scorpion.jabbah.draw.polyline.PolylineShape
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Snapper
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.net.NetImpl
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.connect.DragEdgeViewDestinationConnector
import ch.scorpion.jabbah.graph.view.connect.DragEdgeViewOriginConnector
import ch.scorpion.jabbah.graph.view.connect.EdgeToPortConnector
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.netview.AbstractNetViewElement
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.io.*


/**
 * A standard implementation of the [EdgeView] interface
 */
open class EdgeViewImpl<T : Any>(
	styleProvider: StyleProvider,
	override val edgeToPortConnectorSupplier: () -> EdgeToPortConnector,
	origEndpointConnectorSupplier: () -> DragEdgeViewOriginConnector,
	destEndpointConnectorSupplier: () -> DragEdgeViewDestinationConnector,
	currentSystemSpeedCategory: CurrentSystemSpeedCategory,
	net: Net<T>
) : AbstractNetViewElement<T>(styleProvider, currentSystemSpeedCategory, net), EdgeView<T> {

	private companion object {
		val LOG by logger(EdgeViewImpl::class)
	}

	constructor(
		styleProvider: StyleProvider,
		edgeToPortConnectorSupplier: () -> EdgeToPortConnector,
		origEndpointConnectorSupplier: () -> DragEdgeViewOriginConnector,
		destEndpointConnectorSupplier: () -> DragEdgeViewDestinationConnector,
		currentSystemSpeedCategory: CurrentSystemSpeedCategory
	) : this(styleProvider, edgeToPortConnectorSupplier, origEndpointConnectorSupplier, destEndpointConnectorSupplier,
		currentSystemSpeedCategory, NetImpl<T>())

	@Suppress("unused")
	constructor() : this(
		DrawStyleModule.styleProvider,
		{ GraphViewModule.edgeToPortConnector },
		{ GraphViewModule.dragEdgeViewOriginConnector },
		{ GraphViewModule.dragEdgeViewDestinationConnector },
		ExecutionModule.currentSystemSpeedCategory
	)

	init {
		modelExchanged(null)
	}

	// TODO How to initialize EdgeViewStyling when new EdgeViews are created while interacting with Tools?
	// The proper styling should be derived from adjacent EdgeViews.
	private var styling: EdgeViewStyling = NetViewStyle.LINE.createEdgeViewStyling(styleProvider, this)

	/** ---- [Any] */

	override fun toString(): String {
		return "${super.toString()} origin=${origin?.id ?: "null"} dest=${destination?.id ?: "null"}"
	}

	/** ---- [EdgeView] interface */

	override val layout: EdgeViewLayout = EdgeViewLayoutImpl(this)

	override var polyline: PolylineShape = DrawModule.polylineShapeFactory.invoke(null)

	override var origin: ConnectableView? = null

	override var originPort: Port<T>? = null

	override var destination: ConnectableView? = null

	override var destinationPort: Port<T>? = null

	override val originEndpointView: EdgeEndpointView = EdgeEndpointView(this, origEndpointConnectorSupplier, styleProvider)

	override val destinationEndpointView: EdgeEndpointView = EdgeEndpointView(this, destEndpointConnectorSupplier, styleProvider)

	override val segmentPointCount: Int get() = polyline.pointsCount

	override val width: Int get() = styling.width

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
		return if (port.portType.isInput) EdgeViewConnectionState.Input else EdgeViewConnectionState.Output
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

	override fun addSegmentPoint(point: Point2D): EdgeView<T> {
		return addSegmentPoint(segmentPointCount, point)
	}

	override fun addSegmentPoint(index: Int, point: Point2D): EdgeView<T> {
		invalidate()
		polyline.addPointAt(index, point.x, point.y)
		updateEndpointViews()
		styling.updateBoundingBox()
		invalidate()
		return this
	}

	override fun setLaidOutPoints(points: List<Point2D>) {
		invalidate()
		polyline.setPoints(points)

		updateEndpointViews()
		styling.updateBoundingBox()
		invalidate()

		compact()
		update()
	}

	override fun compact() {
		invalidate()
		if (polyline.compact()) {
			layout.updateAdjusted()
			updateEndpointViews()
			styling.updateBoundingBox()
			invalidate()
		}
	}

	override fun connectToOrigin(origin: ConnectableView?, port: Port<T>?) {
		checkArgument(port == null || origin != null)
		if (this.origin != null) {
			this.origin?.removeDrawableListener(layout)
			this.origin?.handleUnconnect(this, originPort)
		}
		this.origin = origin
		this.originPort = port
		if (this.origin != null) {
			this.origin?.addDrawableListener(layout)
			origin?.handleConnect(this, port)
		}

		updateEndpointViews()
		styling.updateBoundingBox()
	}

	override fun connectToDestination(destination: ConnectableView?, port: Port<T>?) {
		checkArgument(port == null || destination != null)

		if (this.destination != null) {
			this.destination?.removeDrawableListener(layout)
			this.destination?.handleUnconnect(this, destinationPort)
		}
		this.destination = destination
		this.destinationPort = port
		if (this.destination != null) {
			this.destination?.addDrawableListener(layout)
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
			return polyline.length + maxSubnetLength
		}
		return polyline.length
	}

	override fun moveOriginEndPoint(x: Double, y: Double) {
		LOG.trace("moveOriginEndPoint to ($x,$y)")
		invalidate()
		moveOriginEndPointImpl(x, y)
		layout.layoutOrigin()
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
		layout.layoutDestination()
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
				if (EdgeViewSplitterJoiner.splitOriginSegmentForMove(this)) {
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
				EdgeViewSplitterJoiner.splitDestinationSegmentForMove(this)
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

		layout.isAdjusted = true

		layout.suspendOriginLayout = true
		layout.suspendDestinationLayout = true

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

		layout.suspendOriginLayout = false
		layout.suspendDestinationLayout = false

		return MoveEdgeSegmentInfo(polyline.findSegment(center.x, center.y)!!, offset)
	}

	override fun getSegmentDirection(segmentIndex: Int): Direction? {
		return layout.type.getSegmentDirection(this, segmentIndex)
	}

	override val isDegenerated: Boolean
		get() = segmentPointCount < 2 || isOriginDegenerated() || isDestinationDegenerated()

	private fun isOriginDegenerated(): Boolean {
		return polyline.getPointAt(0) == polyline.getPointAt(1)
	}

	private fun isDestinationDegenerated(): Boolean {
		return polyline.getPointAt(polyline.pointsCount - 1) == polyline.getPointAt(polyline.pointsCount - 2)
	}

	override fun split(index: Int, splitLocation: Point2D, edgeViewCreator: (Net<T>) -> EdgeView<T>): EdgeView<T> {
		return EdgeViewSplitterJoiner.split(this, index, splitLocation, edgeViewCreator)
	}

	override fun join(edgeView: EdgeView<T>): EdgeView<*> {
		return EdgeViewSplitterJoiner.join(this, edgeView)
	}

	override fun snap(x: Double, y: Double, backgroundSnapper: Snapper?): EdgeViewSnapLocatorResult? {
		return EdgeViewSnapLocator.snap(this, x, y, backgroundSnapper)
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
		get() = polyline.getFirstPoint()

	override fun prepareMoveBy(components: Collection<Locatable>) {
		val originIfExistsMoves = origin == null || components.contains(origin as Locatable)
		val destIfExistsMoves = destination == null || components.contains(destination as Locatable)
		val thisMoves = originIfExistsMoves && destIfExistsMoves

		layout.suspendOriginLayout = originIfExistsMoves && thisMoves
		layout.suspendDestinationLayout = destIfExistsMoves && thisMoves
	}

	override fun moveBy(dx: Double, dy: Double) {
		LOG.debug("moveBy")

		// An EdgeView does only move if all ConnectableView it is connected to are moved as well
		if (origin != null && !layout.suspendOriginLayout || destination != null && !layout.suspendDestinationLayout) {
			return
		}

		originEndpointView.moveBy(dx, dy)
		destinationEndpointView.moveBy(dx, dy)

		// This leads to setLocation() which does the invalidation and update things, so there
		// is no need to do it here as well
		super<AbstractNetViewElement>.moveBy(dx, dy)
	}

	override fun completeMoveBy() {
		layout.suspendOriginLayout = false
		layout.suspendDestinationLayout = false
	}

	/** ---- [Drawable] interface */

	override val boundingBox: Rectangle2D get() = styling.boundingBox

	override fun contains(x: Double, y: Double): Boolean {
		return polyline.findSegment(x, y) != null
	}

	override fun getTooltip(x: Double, y: Double): Tooltip? {
		val designError = net!!.designError ?: return super.getTooltip(x, y)
		return Tooltip(designError.description, x, y)
	}

	override fun accept(visitor: HierarchyVisitor): Boolean {
		return visitor.visit(this)
	}

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
		LOG.trace("getInputEventHandler at " + Point2D(context.x, context.y))
		return layout.type.getInputEventHandler(this, context) { super.getInputEventHandler(context) }
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
		writer.writeString("layout", layout.type.customName)
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
		writer.writeBoolean("adjusted", layout.isAdjusted)
		writer.writePoints("shape", "polylineShape", "points", polyline.getPoints(0, polyline.pointsCount))
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("layout")) {
			layout.type = LayoutType.withName(reader.readString("layout"))
		}
		if (reader.hasAttribute("adjusted")) {
			layout.isAdjusted = reader.readBoolean("adjusted")
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

			if (cv.isConnectable) {
				if ("orig" == reference.name) {
					connectToOrigin(cv, port as Port<T>?)
				} else {
					connectToDestination(cv, port as Port<T>?)
				}
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
}