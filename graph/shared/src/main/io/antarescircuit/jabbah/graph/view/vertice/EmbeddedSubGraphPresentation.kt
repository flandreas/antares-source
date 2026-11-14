package io.antarescircuit.jabbah.graph.view.vertice

import io.antarescircuit.jabbah.base.Disposable
import io.antarescircuit.jabbah.base.Tooltip
import io.antarescircuit.jabbah.base.geom.*
import io.antarescircuit.jabbah.draw.*
import io.antarescircuit.jabbah.draw.drawable.AbstractDrawable.DrawableOwner
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.edit.model.rectangle.RectangularComponent
import io.antarescircuit.jabbah.edit.model.text.*
import io.antarescircuit.jabbah.execution.actor.*
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphPortView
import io.antarescircuit.jabbah.graph.view.port.PortView
import io.antarescircuit.jabbah.graph.view.style.GraphStyleType
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

/**
 * Displays the inner [GraphView] of a [SubGraphVerticeViewImpl].
 * Owns only the derived view; the enclosing vertice retains every external port and connection.
 */
internal class EmbeddedSubGraphPresentation(
	val owner: SubGraphVerticeViewImpl,
	val graph: GraphView
): Disposable {

	private companion object {
		const val DRAW_CONNECTING_LINES = false
	}

	private val padding = 2 * Look.SCALE.toDouble()

	private val title = Label("", owner.styleProvider.getStyle(
		GraphStyleType.SUBSYSTEM).font,
		horizontalAlignment = HorizontalAlignment.CENTER,
		verticalAlignment = VerticalAlignment.TOP)

	private val originalMirrorH = owner.isHorizontallyMirrored

	private val originalMirrorV = owner.isVerticallyMirrored

	private val originalPorts = owner.getPortViews().map { Triple(it, it.location, it.direction) }

	private val listener = DrawableOwner(owner, graph)

	private val contentBounds = Rectangle2D(graph.boundingBox)

	private fun updateTitle() {
		title.font = owner.customFont ?: owner.styleProvider.getStyle(GraphStyleType.SUBSYSTEM).font
		title.text = owner.describingName
		title.location = Point2D(owner.graphWidth / 2, titleInset)
	}

	/** The north inset already includes clearance for the port labels. */
	private val titleInset: Double get() = maxOf(
		RectangularComponent.TEXT_INSET.toDouble(),
		if (ports.any { it.side == Direction.NORTH }) insets.north else 0.0,
	)

	internal val titleBounds: Rectangle2D get() {
		updateTitle()
		return Rectangle2D(title.boundingBox)
	}

	private val contentTop get() = titleBounds.maxY + padding

	private data class Insets(val west: Double, val east: Double, val north: Double, val south: Double)

	private data class LayoutKey(
		val insets: Insets, val width: Double, val height: Double, val location: Point2D,
		val rotation: Rotation, val mirrorH: Boolean, val mirrorV: Boolean, val grid: Double,
		val minimumWidth: Double, val minimumHeight: Double, val lengths: List<Int>,
	)

	private var lastLayout: LayoutKey? = null

	private data class PortLayout(val outer: PortView<*>, val inner: GraphPortView<*>, val side: Direction)

	private data class Leader(val start: Point2D, val end: Point2D, val side: Direction)

	private val leaders = mutableListOf<Leader>()

	private val ports get() = owner.getPortViews().mapNotNull { outer ->
		val inner = graph.getGraphPortView(outer.port.name ?: return@mapNotNull null) ?: return@mapNotNull null
		val side = innerDirection(outer.port.name) ?: return@mapNotNull null
		PortLayout(outer, inner, side)
	}

	private val gridDistance get() = owner.embeddingGridDistance ?: 0.0

	private fun layoutKey() = LayoutKey(
		insets, owner.graphWidth, owner.graphHeight, owner.location, owner.rotation,
		owner.isHorizontallyMirrored, owner.isVerticallyMirrored, gridDistance,
		minimumWidth, minimumHeight, owner.getPortViews().map { it.length },
	)

	init {
		updatePortDirections()
	}

	override fun dispose() {
		listener.dispose()
		graph.dispose()
		originalPorts.forEach { (port, location, direction) ->
			val mirrorH = originalMirrorH != owner.isHorizontallyMirrored
			val mirrorV = originalMirrorV != owner.isVerticallyMirrored
			port.location = Point2D(if (mirrorH) -location.x else location.x, if (mirrorV) -location.y else location.y)
			port.direction = if ((mirrorH && direction.dx != 0) || (mirrorV && direction.dy != 0)) direction.opposite() else direction
		}
	}

	private fun innerDirection(portName: String?): Direction? {
		val inner = graph.getGraphPortView(portName ?: return null) ?: return null
		// The inner connector points into the circuit; the enclosing port points out of it.
		return inner.getPortViews().firstOrNull()?.relativeDirection?.opposite()
	}

	private fun updatePortDirections() {
		for (outer in owner.getPortViews()) {
			val direction = innerDirection(outer.port.name) ?: continue
			outer.direction = when {
				owner.isHorizontallyMirrored && direction.dx != 0 -> direction.opposite()
				owner.isVerticallyMirrored && direction.dy != 0 -> direction.opposite()
				else -> direction
			}
		}
	}

	private val insets: Insets get() {
		val depths = Direction.entries.associateWith { padding }.toMutableMap()
		for (outer in owner.getPortViews()) {
			val side = innerDirection(outer.port.name) ?: continue
			val box = outer.boundingBox
			// Measure only the portion inside the frame, in the port's unrotated coordinates.
			// This includes the actual label font, alignment, border distance and annotations.
			val depth = when (outer.direction) {
				Direction.WEST -> box.maxX - outer.location.x
				Direction.EAST -> outer.location.x - box.minX
				Direction.NORTH -> box.maxY - outer.location.y
				Direction.SOUTH -> outer.location.y - box.minY
			}
			depths[side] = maxOf(depths.getValue(side), depth + Look.SCALE + gridDistance / 2)
		}
		return Insets(
			depths.getValue(Direction.WEST),
			depths.getValue(Direction.EAST),
			depths.getValue(Direction.NORTH),
			depths.getValue(Direction.SOUTH))
	}

	/** Required centre-to-centre separation, including unscaled labels and grid slots. */
	private fun portSpacing(group: List<PortLayout>, vertical: Boolean): Double {
		val extent = group.maxOfOrNull { if (vertical) it.outer.boundingBox.height else it.outer.boundingBox.width } ?: 0.0
		val required = maxOf(Look.GRID.toDouble(), extent + Look.SCALE)
		return if (gridDistance > 0) ceil(required / gridDistance) * gridDistance else required
	}

	private fun minimumContentSpan(vertical: Boolean): Double {
		val groups = ports.filter { (it.side.dx != 0) == vertical }.groupBy { it.side }.values
		return maxOf(padding * 2, groups.maxOfOrNull { (it.size - 1) * portSpacing(it, vertical) + 2 * gridDistance } ?: 0.0)
	}

	val minimumWidth: Double get() {
		updateTitle()
		val inset = insets
		return maxOf(minimumContentSpan(false) + inset.west + inset.east, title.boundingBox.width + padding * 2)
	}

	val minimumHeight get() = minimumContentSpan(true) + contentTop + insets.south

	val preferredWidth get() = maxOf(minimumWidth, contentBounds.width + insets.west + insets.east)
	val preferredHeight get() = maxOf(minimumHeight, contentBounds.height + contentTop + insets.south)

	internal val zoom: Double get() {
		val inset = insets
		val width = maxOf(1.0, owner.graphWidth - inset.west - inset.east)
		val height = maxOf(1.0, owner.graphHeight - contentTop - inset.south)
		return minOf(1.0, if (contentBounds.width > 0) width / contentBounds.width else 1.0,
			if (contentBounds.height > 0) height / contentBounds.height else 1.0)
	}

	internal val contentBox: Rectangle2D get() {
		val inset = insets
		return Rectangle2D(inset.west, contentTop, contentBounds.width * zoom, contentBounds.height * zoom)
	}

	private val offset: Point2D get() {
		val box = contentBox
		return Point2D(box.x - contentBounds.x * zoom, box.y - contentBounds.y * zoom)
	}

	internal fun fromInner(point: Point2D): Point2D = point.multiply(zoom).add(offset)
	internal fun toInner(point: Point2D): Point2D = point.subtract(offset).divide(zoom)

	fun needsLayout(): Boolean = lastLayout != layoutKey()

	private fun sidePoint(side: Direction, position: Double): Point2D = when (side) {
		Direction.WEST -> Point2D(0.0, position)
		Direction.EAST -> Point2D(owner.graphWidth, position)
		Direction.NORTH -> Point2D(position, 0.0)
		Direction.SOUTH -> Point2D(position, owner.graphHeight)
	}

	/** Snap the endpoint in the containing drawing, then recover its port origin. */
	private fun snapPortOrigin(port: PortLayout, position: Double): Point2D {
		val origin = sidePoint(port.side, position)
		val distance = port.side.multiply(port.outer.length.toDouble())
		if (gridDistance <= 0) return origin
		val world = owner.handleToWorld(origin.add(distance).add(owner.location))
		val snapped = Point2D(round(world.x / gridDistance) * gridDistance, round(world.y / gridDistance) * gridDistance)
		return owner.localPoint(snapped).subtract(distance)
	}

	fun layoutPorts() {
		updatePortDirections()
		leaders.clear()
		val inset = insets
		val scale = zoom
		val translation = offset
		fun transform(point: Point2D) = point.multiply(scale).add(translation)

		for ((side, group) in ports.groupBy { it.side }) {
			val vertical = side.dx != 0
			fun coordinate(point: Point2D) = if (vertical) point.y else point.x
			val ordered = group.sortedWith(compareBy<PortLayout> { coordinate(it.inner.location) }.thenBy { it.outer.port.portId })
			val spacing = portSpacing(group, vertical)
			val low = if (vertical) contentTop else inset.west
			val high = if (vertical) owner.graphHeight - inset.south else owner.graphWidth - inset.east
			val phase = coordinate(snapPortOrigin(ordered.first(), 0.0))
			val lower = if (gridDistance > 0) phase + ceil((low - phase) / gridDistance) * gridDistance else low
			val upper = if (gridDistance > 0) phase + floor((high - phase) / gridDistance) * gridDistance else high
			var previous = lower - spacing

			for ((index, port) in ordered.withIndex()) {
				var desired = coordinate(transform(port.inner.location))
				if (gridDistance > 0) desired = phase + round((desired - phase) / gridDistance) * gridDistance
				val last = maxOf(lower, upper - (ordered.size - index - 1) * spacing)
				val position = maxOf(previous + spacing, desired.coerceIn(lower, last))
				val origin = snapPortOrigin(port, position)
				port.outer.location = owner.mirrorPoint(origin)
				previous = coordinate(origin)
				val bounds = port.inner.boundingBox
				val anchor = when (side) {
					Direction.WEST -> Point2D(bounds.minX, port.inner.location.y)
					Direction.EAST -> Point2D(bounds.maxX, port.inner.location.y)
					Direction.NORTH -> Point2D(port.inner.location.x, bounds.minY)
					Direction.SOUTH -> Point2D(port.inner.location.x, bounds.maxY)
				}
				// Leave the unscaled port label clear of the connecting line.
				val box = port.outer.boundingBox
				val depth = when (port.outer.direction) {
					Direction.WEST -> box.maxX - port.outer.location.x
					Direction.EAST -> port.outer.location.x - box.minX
					Direction.NORTH -> box.maxY - port.outer.location.y
					Direction.SOUTH -> port.outer.location.y - box.minY
				}
				val clearance = maxOf(0.0, depth) + Look.SCALE / 2.0
				val startDepth = if (side == Direction.NORTH) maxOf(clearance, contentTop - padding / 2) else clearance
				leaders.add(Leader(origin.subtract(side.multiply(startDepth)), transform(anchor), side))
			}
		}
		lastLayout = layoutKey()
	}

	private fun drawLeaders(context: DrawContext) {
		val style = owner.styleProvider.getStyle(GraphStyleType.EDGE)
		context.g.color = context.chooseForeground(style.color.foregroundColor)
		context.g.stroke = style.stroke
		for ((start, end, side) in leaders) {
			if (side.dx != 0) {
				val x = (start.x + end.x) / 2
				context.g.drawLine(start.x, start.y, x, start.y)
				context.g.drawLine(x, start.y, x, end.y)
				context.g.drawLine(x, end.y, end.x, end.y)
			} else {
				val y = (start.y + end.y) / 2
				context.g.drawLine(start.x, start.y, start.x, y)
				context.g.drawLine(start.x, y, end.x, y)
				context.g.drawLine(end.x, y, end.x, end.y)
			}
		}
	}

	fun bounds(): Rectangle2D {
		val a = owner.mirrorPoint(Point2D.ZERO).add(owner.location)
		val b = owner.mirrorPoint(Point2D(owner.graphWidth, owner.graphHeight)).add(owner.location)
		val result = Rectangle2D(minOf(a.x, b.x), minOf(a.y, b.y), owner.graphWidth, owner.graphHeight)
		owner.getPortViews().forEach { result.add(Rectangle2D(it.boundingBox).moveBy(owner.location)) }
		return result
	}

	fun draw(context: DrawContext) {
		val sx = if (owner.isHorizontallyMirrored) -1.0 else 1.0
		val sy = if (owner.isVerticallyMirrored) -1.0 else 1.0
		val clip = context.modelClip
		context.modelClip = null
		context.g.save()
		context.g.scale(sx, sy)

		try {
			val borderStyle = owner.styleProvider.getStyle(GraphStyleType.SUBSYSTEM)
			context.g.color = context.chooseBackground(borderStyle.color.backgroundColor)
			context.g.fill(Rectangle2D(0.0, 0.0, owner.graphWidth, owner.graphHeight))
			val previousStroke = context.g.stroke
			context.g.color = context.chooseForeground(borderStyle.color.foregroundColor)
			context.g.stroke = borderStyle.stroke
			context.g.draw(Rectangle2D(0.0, 0.0, owner.graphWidth, owner.graphHeight))
			context.g.stroke = previousStroke
			updateTitle()
			title.draw(context)
			if (DRAW_CONNECTING_LINES) drawLeaders(context)
			val oldUseContextColors = context.useContextColors
			val oldColor = context.color
			context.useContextColors = false
			context.color = owner.styleProvider.getStyle(GraphStyleType.EDGE).color
			try {
				context.g.save()
				try {
					context.g.translate(offset.x, offset.y)
					context.g.scale(zoom, zoom)
					graph.draw(context)
				} finally { context.g.restore() }
			} finally {
				context.useContextColors = oldUseContextColors
				context.color = oldColor
			}
		} finally {
			context.g.restore()
			context.modelClip = clip
		}
	}

	private fun translated(context: ActorInteractionContext): ActorInteractionContext =
		toInner(owner.localPoint(context.location)).let { context.withXY(it.x, it.y) }

	private var target: ActorInteractionHandler? = null

	private fun containsInteraction(context: ActorInteractionContext): Boolean =
		Rectangle2D(0.0, 0.0, owner.graphWidth, owner.graphHeight).contains(owner.localPoint(context.location))

	private fun handlerAt(context: ActorInteractionContext): ActorInteractionHandler? {
		if (!containsInteraction(context)) return null
		val local = translated(context)
		val actor = graph.getDrawableAt(local.x, local.y) { it is ActorView } as? ActorView
		return actor?.getActorInteractionHandler(local)
	}

	val interactionHandler: ActorInteractionHandler = object : InputEventHandlerAdapter<ActorInteractionContext>() {
		/** Keep the coordinate adapter only while the inner handler requests capture. */
		private fun remember(next: ActorInteractionHandler?): ActorInteractionHandler? {
			target = next
			return if (next != null) this else null
		}

		override fun keyPressed(context: ActorInteractionContext): ActorInteractionHandler? =
			remember(target?.keyPressed(translated(context)))

		override fun keyReleased(context: ActorInteractionContext): ActorInteractionHandler? =
			remember(target?.keyReleased(translated(context)))

		override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? =
			remember(handlerAt(context)?.mouseMoved(translated(context)))

		override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? =
			remember(handlerAt(context)?.mousePressed(translated(context)))

		// An active drag must still receive its release when the pointer leaves the frame.
		override fun mouseDragged(context: ActorInteractionContext): ActorInteractionHandler? =
			remember(target?.mouseDragged(translated(context)))

		override fun mouseReleased(context: ActorInteractionContext): ActorInteractionHandler? =
			remember(target?.mouseReleased(translated(context)))

		override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler? {
			if (!containsInteraction(context)) return remember(null)
			val next = (target ?: handlerAt(context))?.mouseClicked(translated(context))
			if (context.mouseEvent?.isEventConsumed() != true) owner.handleGraphBackgroundClick(context)
			return remember(next)
		}
	}

	fun <T : InputEventContext> tooltip(context: T): Tooltip? {
		val point = toInner(owner.localPoint(context.location))
		val actor = graph.getDrawableAt(point.x, point.y) { it is ActorView } as? ActorView ?: return null
		val tip = actor.getExecutionTooltip(context.withXY(point.x, point.y)) ?: return null
		val rect = tip.sourceRect
		val result = Rectangle2D()
		for (corner in listOf(rect.topLeft, rect.topRight, rect.bottomLeft, rect.bottomRight)) {
			result.add(owner.handleToWorld(fromInner(corner).add(owner.location)))
		}
		return tip.copy(sourceRect = result)
	}
}
