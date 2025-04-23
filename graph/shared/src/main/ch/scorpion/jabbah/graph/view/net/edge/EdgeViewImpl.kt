package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.drawable.Movable
import ch.scorpion.jabbah.draw.drawable.Rotatable
import ch.scorpion.jabbah.draw.drawable.RotationDirection
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.polyline.ArrowHead
import ch.scorpion.jabbah.draw.polyline.LineTerminator
import ch.scorpion.jabbah.draw.polyline.PolylineShape
import ch.scorpion.jabbah.draw.polyline.PolylineShapeFactory
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.net.NetImpl
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.EdgeView.Companion.PROP_MIN_EDGE_VIEW_LENGTH
import ch.scorpion.jabbah.graph.view.connect.DragEdgeViewDestinationConnector
import ch.scorpion.jabbah.graph.view.connect.DragEdgeViewOriginConnector
import ch.scorpion.jabbah.graph.view.connect.EdgeToPortOrEdgeConnector
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType.DESTINATION
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewEndpointType.ORIGIN
import ch.scorpion.jabbah.graph.view.net.netview.AbstractNetViewElement
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyling
import ch.scorpion.jabbah.graph.view.net.netview.NetViewTraversal
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.io.*
import kotlin.math.min
import kotlin.reflect.KClass

/**
 * A standard implementation of the [EdgeView] interface
 */
open class EdgeViewImpl<T : Any>(
	styleProvider: StyleProvider,
	override val edgeToPortOrEdgeConnectorSupplier: () -> EdgeToPortOrEdgeConnector,
	origEndpointConnectorSupplier: () -> DragEdgeViewOriginConnector,
	destEndpointConnectorSupplier: () -> DragEdgeViewDestinationConnector,
	net: Net<T>,
	netViewStyle: NetViewStyle? = null
) : AbstractNetViewElement<T>(styleProvider, net), EdgeView<T> {

	companion object {
		private val LOG by logger(EdgeViewImpl::class)

		private val TYPE get() = Translations.getString("graph.component.edge")
		private val NO_OP_ACTOR_HANDLER = InputEventHandlerAdapter<ActorInteractionContext>()

		private val EDIT_TIP_TOOLTIP: Tooltip? = if (BaseModule.properties.getBoolean(PROP_BEGINNER_HELP_TOOLTIP)) {
			Tooltip(Translations.getString("graph.action.splitEdgeView.tip", EdgeToPortOrEdgeConnector.SPLIT_EDGE_VIEW_MODIFIER.label), Rectangle2D.ZERO)
		} else {
			null
		}

		private val MIN_LENGTH = BaseModule.properties.getInt(PROP_MIN_EDGE_VIEW_LENGTH)

		val DEF_EDGE_TO_PORT_CONNECTOR_SUPPLIER = { GraphViewModule.edgeToPortOrEdgeConnector }
		val DEF_ORIG_ENDPOINT_CONNECTOR_SUPPLIER = { GraphViewModule.dragEdgeViewOriginConnector }
		val DEF_DEST_ENDPOINT_CONNECTOR_SUPPLIER = { GraphViewModule.dragEdgeViewDestinationConnector }
	}

	constructor(
		styleProvider: StyleProvider,
		edgeToPortOrEdgeConnectorSupplier: () -> EdgeToPortOrEdgeConnector,
		origEndpointConnectorSupplier: () -> DragEdgeViewOriginConnector,
		destEndpointConnectorSupplier: () -> DragEdgeViewDestinationConnector
	) : this(
		styleProvider,
		edgeToPortOrEdgeConnectorSupplier,
		origEndpointConnectorSupplier,
		destEndpointConnectorSupplier,
		NetImpl<T>()
	)

	@Suppress("unused")
	constructor() : this(
		DrawStyleModule.styleProvider,
		{ GraphViewModule.edgeToPortOrEdgeConnector },
		{ GraphViewModule.dragEdgeViewOriginConnector },
		{ GraphViewModule.dragEdgeViewDestinationConnector }
	)

	override var styling: NetViewStyling = NetViewStyle.LINE.createEdgeViewStyling(styleProvider, this)

	/** The tooltip displayed when not in execution mode.*/
	private val designErrorTooltip = resettableLazy { createDesignErrorTooltip() }

	init {
		modelExchanged(null)
		netViewStyle?.let { styling = it.createEdgeViewStyling(styleProvider, this) }
	}

	/** ---- [Any] */

	override fun toString(): String {
		return "EdgeView id=$id origin=${origin?.connectableView?.id ?: "null"} dest=${destination?.connectableView?.id ?: "null"}"
	}

	/** ---- [Cloneable] interface */

	override fun doClone(): Component {
		val clone = super.doClone() as EdgeViewImpl<*>
		clone.resetModel()
		return clone
	}

	private fun resetModel() {
		model = model.cloneEmpty()
	}

	/** ---- [Component] */

	override fun collectSelectBuddies(drawing: Drawing<Component>, buddies: MutableSet<Component>) {
		buddies.addAll(netView!!.getElements().filter { it !== this@EdgeViewImpl })
	}

	/** ---- [ActorView] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler = NO_OP_ACTOR_HANDLER

	override fun <T: InputEventContext> getExecutionTooltip(context: T): Tooltip? {
		val content = StringBuilder(StringUtils.orEmpty(model.description.value))
		if (content.isNotEmpty()) {
			content.appendLine()
		}

		net?.executionError?.tooltipText?.let {
			content.append(it)
		} ?: content.append(getExecutionTooltipContent())

		return Tooltip(content.toString(), context.x, context.y)
	}

	protected open fun getExecutionTooltipContent(): String =
		"${Translations.getString("graph.currentValue.name")}: ${model.signalDescription}"

	override fun executionStarted(signalHandler: SignalHandler) { }

	override fun executionStopped(signalHandler: SignalHandler) { }

	/** ---- [GraphElementView] interface */

	override val isFullyConnected: Boolean get() = origin != null && destination != null

	/** ---- [EdgeView] interface */

	override var underConstruction: Boolean = false

	override val layout: EdgeViewLayout = EdgeViewLayoutImpl(this)

	override var polyline: PolylineShape = PolylineShapeFactory.create(null)

	override val executionStroke: Stroke get() = stroke

	final override var origin: Connection<T>? = null
		private set(value) {
			if (field != value) {
				invalidate()
				field = value
				updateBeginLineTerminator()
				styling.updateBoundingBox()
				invalidate()
				update()
			}
		}

	final override var destination: Connection<T>? = null
		private set(value) {
			if (field != value) {
				invalidate()
				field = value
				updateEndLineTerminator()
				styling.updateBoundingBox()
				invalidate()
				update()
			}
		}

	override val originEndpointView: EdgeEndpointView = EdgeEndpointView(this, origEndpointConnectorSupplier, styleProvider)

	override val destinationEndpointView: EdgeEndpointView = EdgeEndpointView(this, destEndpointConnectorSupplier, styleProvider)

	override val segmentPointCount: Int get() = polyline.pointsCount

	override val width: Int get() = (styling as EdgeViewStyling).width

	override var isArrow: Boolean = false
		set(value) {
			if (field == value) {
				return
			}
			invalidate()
			field = value
			updateBeginLineTerminator()
			updateEndLineTerminator()
			styling.updateBoundingBox()
			invalidate()
			update()
		}

	override val hasBrokenPortRef: Boolean get() {
		origin?.let { conn ->
			if (conn.port != null && !model.isConnectedWith(conn.port)) {
				return true
			}
		}
		destination?.let { conn ->
			if (conn.port != null && !model.isConnectedWith(conn.port)) {
				return true
			}
		}

		return false
	}

	private fun updateBeginLineTerminator() {
		var lineTerminator: LineTerminator? = null
		if (isArrow) {
			lineTerminator = when (origin?.port?.portType) {
				PortType.INPUT -> ArrowHead.createDefault()
				PortType.INOUT -> ArrowHead.createBidirectionalDefault()
				else -> null
			}
		}
		polyline.beginLineTerminator = lineTerminator
	}

	private fun updateEndLineTerminator() {
		var lineTerminator: LineTerminator? = null
		if (isArrow) {
			lineTerminator = when (destination?.port?.portType) {
				PortType.INPUT -> ArrowHead.createDefault()
				PortType.INOUT -> ArrowHead.createBidirectionalDefault()
				else -> null
			}
		}
		polyline.endLineTerminator = lineTerminator
	}

	override fun getConnection(port: Port<*>): Connection<T>? {
		if (port === origin?.port) {
			return origin
		}
		if (port === destination?.port) {
			return destination
		}
		return null
	}

	override fun getConnection(endpointType: EdgeViewEndpointType): Connection<T>? =
		when (endpointType) {
			ORIGIN -> origin
			DESTINATION -> destination
		}

	override fun getConnection(connectableView: ConnectableView): Connection<T>? =
		if (origin?.connectableView === connectableView) {
			origin
		} else if (destination?.connectableView === connectableView) {
			destination
		} else {
			null
		}

	override fun getOppositeConnection(connectableView: ConnectableView): Connection<T>? =
		if (origin?.connectableView == connectableView) {
			destination
		} else if (destination?.connectableView === connectableView) {
			origin
		} else {
			null
		}

	override fun getOppositeConnection(port: Port<*>): Connection<T>? =
		if (origin?.port === port) {
			destination
		} else if (destination?.port === port) {
			origin
		} else {
			null
		}

	override fun getConnectionEndpointType(connection: Connection<*>): EdgeViewEndpointType? {
		return when (connection) {
			origin -> ORIGIN
			destination -> DESTINATION
			else -> null
		}
	}

	override fun getConnectionEndpointType(connectableView: ConnectableView): EdgeViewEndpointType? {
		return when (connectableView) {
			origin?.connectableView -> ORIGIN
			destination?.connectableView -> DESTINATION
			else -> null
		}
	}

	override fun getEndpointType(edgeEndpointView: EdgeEndpointView): EdgeViewEndpointType? {
		return when (edgeEndpointView) {
			originEndpointView -> ORIGIN
			destinationEndpointView -> DESTINATION
			else -> null
		}
	}

	override fun getOpenEndpointView(type: EdgeViewEndpointType): EdgeEndpointView? {
		return when (type) {
			ORIGIN -> if (origin == null) originEndpointView else null
			DESTINATION -> if (destination == null) destinationEndpointView else null
		}
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
		update()
		return this
	}

	override fun removeSegmentPoint(index: Int): EdgeView<T> {
		invalidate()
		polyline.removePoint(index)
		updateEndpointViews()
		styling.updateBoundingBox()
		invalidate()
		update()
		return this
	}

	override fun clear(): EdgeView<T> {
		invalidate()
		polyline.clear()
		styling.updateBoundingBox()
		invalidate()
		update()
		return this
	}

	override fun setLaidOutPoints(points: List<Point2D>, compact: Boolean) {
		invalidate()
		polyline.setPoints(points)

		updateEndpointViews()
		styling.updateBoundingBox()
		invalidate()

		if (compact) {
			compact()
		}

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

	override fun unconnectFromOrigin() {
		origin?.let {
			it.connectableView.removeDrawableListener(layout)
			it.connectableView.handleUnconnect(this, it.port)
		}
		origin = null
	}

	override fun connectToOrigin(connection: Connection<T>) {
		unconnectFromOrigin()
		origin = connection
		origin!!.let {
			it.connectableView.addDrawableListener(layout)
			it.connectableView.handleConnect(this, it.port, createConnectionGeometry(connection))
		}
		updateEndpointViews()
		styling.updateBoundingBox()
	}

	override fun unconnectFromDestination(lockEndpoint: Boolean) {
		destination?.let {
			it.connectableView.removeDrawableListener(layout)
			it.connectableView.handleUnconnect(this, it.port, lockEndpoint)
		}
		destination = null
	}

	override fun connectToDestination(connection: Connection<T>) {
		unconnectFromDestination()
		destination = connection
		destination!!.let {
			it.connectableView.addDrawableListener(layout)
			it.connectableView.handleConnect(this, it.port, createConnectionGeometry(connection))
		}
		updateEndpointViews()
		styling.updateBoundingBox()
	}

	override fun createConnectionGeometry(connection: Connection<*>): EdgeViewConnectionGeometry =
		EdgeViewConnectionGeometry(
			width,
			getConnectionEndpointType(connection)!!.getLineTerminator(this)?.size ?: 0)

	override fun calculateMaximumNetLength(reverse: Boolean): Double {
		val cv = if (reverse) origin?.connectableView else destination?.connectableView
		if (cv != null && cv is NodeView<*>) {
			var maxSubnetLength = 0.0
			cv.getEdgeViews().forEach {
				if (it !== this) {
					val subnetLength = it.calculateMaximumNetLength(cv === it.destination?.connectableView)
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
		// Snapping is done by higher application layers
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
			if (origin?.connectableView is NodeView<*>) {
				newOrigNodePoint = Point2D(polyline.getPointAt(index)).add(dx, dy)
				if ((origin?.connectableView as NodeView<*>).anyEdgeViewContainsPoint(newOrigNodePoint.x, newOrigNodePoint.y, this)) {
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
			if (destination?.connectableView is NodeView<*>) {
				val nodeView = destination?.connectableView as NodeView<*>
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
			(origin?.connectableView as NodeView<*>).location = Point2D(newOrigNodePoint.x, newOrigNodePoint.y)
		}
		if (newDestNodePoint != null) {
			(destination?.connectableView as NodeView<*>).location = Point2D(newDestNodePoint.x, newDestNodePoint.y)
		}

		val center = polyline.getCenterOfSegment(index)
		compact()

		updateEndpointViews()
		styling.updateBoundingBox()
		invalidate()
		update()

		layout.suspendOriginLayout = false
		layout.suspendDestinationLayout = false

		return polyline.findSegment(center.x, center.y)?.let {
			MoveEdgeSegmentInfo(it, offset)
		} ?: MoveEdgeSegmentInfo(min(index, segmentPointCount - 2), offset)
	}

	override fun getSegmentDirection(segmentIndex: Int): Direction? =
		layout.type.getSegmentDirection(this, segmentIndex)

	override val isDegenerated: Boolean
		get() = segmentPointCount < 2 || isOriginDegenerated() || isDestinationDegenerated()

	override val isSufficientlyLarge: Boolean get() = polyline.length > MIN_LENGTH

	private fun isOriginDegenerated(): Boolean =
		polyline.getPointAt(0) == polyline.getPointAt(1)

	private fun isDestinationDegenerated(): Boolean =
		polyline.getPointAt(polyline.pointsCount - 1) == polyline.getPointAt(polyline.pointsCount - 2)

	override fun split(index: Int, splitLocation: Point2D, edgeViewCreator: (NetView<T>) -> EdgeView<T>): EdgeView<T> =
		EdgeViewSplitterJoiner.split(this, index, splitLocation, edgeViewCreator)

	override fun join(edgeView: EdgeView<T>): EdgeView<*> =
		EdgeViewSplitterJoiner.join(this, edgeView)

	override fun snap(x: Double, y: Double, snapManager: SnapManager?): EdgeViewSnapLocatorResult? =
		EdgeViewSnapLocator.snap(this, x, y, snapManager)

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

	/** ---- [Movable] interface */

	private fun prepareTransformation(components: Collection<*>) {
		val originIfExistsTransforms = origin == null || components.contains(origin!!.connectableView as Locatable)
		val destIfExistsTransforms = destination == null || components.contains(destination!!.connectableView as Locatable)
		val thisTransforms = originIfExistsTransforms && destIfExistsTransforms

		layout.suspendOriginLayout = originIfExistsTransforms && thisTransforms
		layout.suspendDestinationLayout = destIfExistsTransforms && thisTransforms
	}

	private fun canNotTransform(): Boolean =
		// An EdgeView does only transform if all ConnectableView it is connected to are transformed as well
		origin != null && !layout.suspendOriginLayout || destination != null && !layout.suspendDestinationLayout

	override fun prepareMoveBy(components: Collection<Movable>) {
		prepareTransformation(components)
	}

	override fun moveBy(dx: Double, dy: Double) {
		if (canNotTransform()) {
			return
		}

		originEndpointView.moveBy(dx, dy)
		destinationEndpointView.moveBy(dx, dy)

		// This leads to setLocation() which does the invalidation and update things, so there
		// is no need to do it here as well
		super<AbstractNetViewElement>.moveBy(dx, dy)
	}

	private fun completeTransformation() {
		layout.suspendOriginLayout = false
		layout.suspendDestinationLayout = false
	}

	override fun completeMoveBy() {
		completeTransformation()
	}

	/** ---- [Rotatable] interface */

	override fun isRotatableWith(selection: Collection<*>): Boolean =
		(origin == null || selection.contains(origin!!.connectableView))
			&& (destination == null || selection.contains(destination!!.connectableView))

	override fun prepareRotateBy(components: Collection<Rotatable>) {
		prepareTransformation(components)
	}

	override fun rotate(direction: RotationDirection, pivot: Point2D?) {
		polyline.rotate(direction, pivot)
		updateEndpointViews()
		styling.updateBoundingBox()
	}

	override fun completeRotateBy() {
		completeTransformation()
	}

	/** ---- [Snappable] interface: [EdgeView]s aren't snap point sources */

	override val snappableX: Array<SnappableX> get() = Snappable.EMPTY_X

	override val snappableY: Array<SnappableY> get() = Snappable.EMPTY_Y

	/** ---- [Drawable] interface */

	override val boundingBox: RectangularShape get() = styling.boundingBox

	override fun contains(x: Double, y: Double): Boolean {
		return polyline.findSegment(x, y) != null
	}

	override fun <T: InputEventContext> getTooltip(context: T): Tooltip? =
		if (!context.readonly) {
			if (designErrorTooltip.value != null) {
				designErrorTooltip.value.also { it!!.sourceRect = Rectangle2D.pointLike(context.location) }
			} else {
				if (EDIT_TIP_TOOLTIP != null && !context.mouseEvent!!.hasModifier(EdgeToPortOrEdgeConnector.SPLIT_EDGE_VIEW_MODIFIER)) {
					EDIT_TIP_TOOLTIP.also { it.sourceRect = Rectangle2D.pointLike(context.location) }
				} else {
					null
				}
			}
		} else {
			null
		}

	override fun accept(visitor: HierarchyVisitor): Boolean {
		return visitor.visit(this)
	}

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> =
		layout.type.getInputEventHandler(this, context) { super.getInputEventHandler(context) }

	override fun draw(context: DrawContext) {
		styling.draw(context)
	}

	/** ---- [NetViewElement] interface */

	override fun traverse(traversal: NetViewTraversal<T>) {
		if (traversal.edgeViews.contains(this)) {
			return
		}
		traversal.edgeViews.add(this)

		if (origin?.connectableView is NodeView<*>) {
			(origin!!.connectableView as NodeView<T>).traverse(traversal)
		} else if (origin?.port != null) {
			traversal.ports.add(origin!!.port!!)
		}

		if (destination?.connectableView is NodeView<*>) {
			(destination!!.connectableView as NodeView<T>).traverse(traversal)
		} else if (destination?.port != null) {
			traversal.ports.add(destination!!.port!!)
		}
	}

	override fun isConnectedWithAnyPort(ports: Set<Port<T>>): Boolean {
		val traversal = NetViewTraversal<T>()
		traverse(traversal)
		return ports.intersect(traversal.ports).isNotEmpty()
	}

	override fun handleNetViewStyleChanged() {
		invalidate()
		styling = netView!!.style.createEdgeViewStyling(styleProvider, this)
		styling.updateBoundingBox()

		origin?.portView?.connectionGeometry = createConnectionGeometry(origin!!)
		destination?.portView?.connectionGeometry = createConnectionGeometry(destination!!)

		invalidate()
		validate()
	}

	override fun collectConnectedVerticeViews(type: KClass<*>, result: MutableSet<Component>) {
		origin?.connectableView?.let {
			if (it is VerticeView<*> && it::class == type && !result.contains(it)) {
				result.add(it)
			}
		}
		destination?.connectableView?.let {
			if (it is VerticeView<*> && it::class == type && !result.contains(it)) {
				result.add(it)
			}
		}
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("layout", layout.type.customName)
		if (isArrow) {
			writer.writeBoolean("arrow", true)
		}

		origin?.let {
			writer.writeInt("orig", writer.provideIdentity(it.connectableView))
			if (it.port != null) {
				writer.writeInt("origPort", it.port.portId)
				if (it.port.portType.isOutput) {
					writer.writeBoolean("origPortOutput", true)
				}
			}
		}

		destination?.let {
			writer.writeInt("dest", writer.provideIdentity(it.connectableView))
			if (it.port != null) {
				writer.writeInt("destPort", it.port.portId)
				if (it.port.portType.isOutput) {
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

		polyline = PolylineShapeFactory.create(reader.readPoints("shape", "polylineShape", "points"))

		if (reader.hasAttribute("arrow")) {
			isArrow = reader.readBoolean("arrow")
		}

		updateEndpointViews()
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		super.resolve(reference, referenceResolver)

		if ("orig" == reference.name || "dest" == reference.name) {
			val ref = reference.additionalInfo as VerticeViewRef
			val cv: ConnectableView? = referenceResolver.getStorable(ref.verticeViewId)
			if (cv == null) {
				LOG.warn("Couldn't resolve ConnectableView ${ref.verticeViewId} to connect to EdgeView")
				return
			}
			var port: Port<*>? = null
			if (ref.portId != null) {
				try {
					port = cv.getPort(ref.portId)
				} catch (e: NoSuchElementException) {
					LOG.warn("Couldn't resolve Port ${ref.portId} to connect to EdgeView")
					return
				}
			}

			if (cv.isConnectable) {
				if ("orig" == reference.name) {
					connectToOrigin(Connection(cv, port as Port<T>?))
				} else {
					connectToDestination(Connection(cv, port as Port<T>?))
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

	/** ---- [AbstractGraphElementView] */

	override fun handleStateChanged(event: GraphElementEvent) {
		if (event.signalHandler is Scheduler && event.reason == Net.STATE_CHANGE_SIGNAL) {
			if (!GraphApplicationContext.isShowNetState(event.signalHandler)) {
				// Avoid leading to unnecessary View repainting
				return
			}
		}
		super.handleStateChanged(event)
		if (event.signalHandler != null) {
			designErrorTooltip.reset()
		}
	}

	/** ---- [Component] interface */

	override val type: String get() = TYPE

	override val deletable: Boolean get() = !underConstruction && super.deletable

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
	private fun checkOriginSegmentLength(): Boolean =
		origin?.port == null || polyline.getSegmentLength(0) >= origin!!.portView!!.minSegmentLength

	/**
	 * Check whether the destination segment is larger than its required minimum length.
	 * @return `true` if larger than its minimum length
	 */
	private fun checkDestinationSegmentLength(): Boolean =
		destination?.port == null || polyline.getSegmentLength(polyline.pointsCount - 2) >= destination!!.portView!!.minSegmentLength

	private fun createDesignErrorTooltip(): Tooltip? {
		if (model.designError != null) {
			return Tooltip(model.designError!!.description, Rectangle2D.ZERO)
		}
		return null
	}

	/** Draws a small indicator for the begin Connection. Only used while developing. */
	protected fun drawBeginConnectionAnnotation(context: DrawContext) {
		beginConnectionAnnotatePoint?.let {
			context.g.color = ch.scorpion.jabbah.draw.graphics.Color.YELLOW
			context.g.fillCircle(it.x, it.y, 2.0)
		}
	}

	private val beginConnectionAnnotatePoint get(): Point2D? =
		getSegmentDirection(0)?.let { dir ->
			polyline.getFirstPoint().add(8.0 * dir.dx, 8.0 * dir.dy)
		}
}