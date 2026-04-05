package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.geom.*
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.net.netview.NetViewStyle
import io.antarescircuit.jabbah.graph.view.net.node.NodeView
import kotlin.math.max
import kotlin.math.min

/** Draws an [EdgeView] in [NetViewStyle.BLOCK]. */
class EdgeViewBlockStyling(private val edgeView: EdgeView<*>) : EdgeViewStyling {

	private companion object {
		const val HALF_WIDTH = NetViewStyle.BLOCK_HW
		const val ARROW_LENGTH = 10
		const val ARROW_WIDTH = 5
		const val ARROW_DIST = 5
		const val REVERSE_ARROW_LENGTH = 4
		const val DOUBLE_ARROW_LENGTH = ARROW_LENGTH + ARROW_DIST + REVERSE_ARROW_LENGTH

		val ARROW_PATH = buildSingleArrowPath(System.createPath().moveTo(-ARROW_LENGTH, -HALF_WIDTH))

		val DOUBLE_ARROW_PATH = buildDoubleArrowPath(System.createPath().moveTo(-DOUBLE_ARROW_LENGTH, -HALF_WIDTH))

		private fun buildSingleArrowPath(path: Path): Path {
			path
				.lineTo(-ARROW_LENGTH, -HALF_WIDTH - ARROW_WIDTH)
				.lineTo(0, 0)
				.lineTo(-ARROW_LENGTH, HALF_WIDTH + ARROW_WIDTH)
				.lineTo(-ARROW_LENGTH, HALF_WIDTH)
			return path
		}

		private fun buildDoubleArrowPath(path: Path): Path {
			path
				.lineTo(-DOUBLE_ARROW_LENGTH + REVERSE_ARROW_LENGTH, -HALF_WIDTH - ARROW_WIDTH)
				.lineTo(-DOUBLE_ARROW_LENGTH + REVERSE_ARROW_LENGTH, -HALF_WIDTH)
				.lineTo(-ARROW_LENGTH, -HALF_WIDTH)

			buildSingleArrowPath(path)

			path
				.lineTo(-DOUBLE_ARROW_LENGTH + REVERSE_ARROW_LENGTH, HALF_WIDTH)
				.lineTo(-DOUBLE_ARROW_LENGTH + REVERSE_ARROW_LENGTH, HALF_WIDTH + ARROW_WIDTH)
				.lineTo(-DOUBLE_ARROW_LENGTH, HALF_WIDTH)

			return path
		}
	}

	/** ---- [EdgeViewStyling] */

	override val width: Int get() = 2 * HALF_WIDTH

	private val _boundingBox = Rectangle2D()
	override val boundingBox: RectangularShape get() = _boundingBox

	override val isArea: Boolean get() = true

	override fun draw(context: DrawContext) {
		val oldColor = context.g.color
		val oldStroke = context.g.stroke

		// Draw the filled content
		context.g.color = context.color!!.backgroundColor
		draw(object : Consumer {
			override fun consume(beginL: Point2D, beginR: Point2D, endR: Point2D, endL: Point2D) {
				val minX = min(min(beginL.x, beginR.x), min(endL.x, endR.x))
				val minY = min(min(beginL.y, beginR.y), min(endL.y, endR.y))
				val maxX = max(max(beginL.x, beginR.x), max(endL.x, endR.x))
				val maxY = max(max(beginL.y, beginR.y), max(endL.y, endR.y))

				context.g.fillRect(minX.toInt(), minY.toInt(), (maxX - minX).toInt(), (maxY - minY).toInt())

				if (getOriginArrowOverallLength() > 0) {
					drawArrow(context, EdgeViewEndpointType.ORIGIN, true)
				}
				if (getDestinationArrowOverallLength() > 0) {
					drawArrow(context, EdgeViewEndpointType.DESTINATION, true)
				}
			}
		})

		// Draw the border
		context.g.color = context.color!!.foregroundColor
		context.g.stroke = NetViewStyle.BLOCK_BORDER_STROKE
		draw(object : Consumer {
			override fun consume(beginL: Point2D, beginR: Point2D, endR: Point2D, endL: Point2D) {
				context.g.drawLine(beginL.x.toInt(), beginL.y.toInt(), endL.x.toInt(), endL.y.toInt())
				context.g.drawLine(beginR.x.toInt(), beginR.y.toInt(), endR.x.toInt(), endR.y.toInt())

				if (getOriginArrowOverallLength() > 0) {
					drawArrow(context, EdgeViewEndpointType.ORIGIN, false)
				}
				if (getDestinationArrowOverallLength() > 0) {
					drawArrow(context, EdgeViewEndpointType.DESTINATION, false)
				}
			}
		})

		context.g.color = oldColor
		context.g.stroke = oldStroke
	}

	override fun updateBoundingBox() {
		if (edgeView.polyline.pointsCount > 0) {
			_boundingBox.setFrame(edgeView.polyline.getPointAt(0).x, edgeView.polyline.getPointAt(0).y, 0.0, 0.0)
		}
		_boundingBox.add(edgeView.polyline.boundingBox)

		_boundingBox.setFrame(
			boundingBox.x - HALF_WIDTH,
			boundingBox.y - HALF_WIDTH - NetViewStyle.BLOCK_BORDER_STROKE.width,
			boundingBox.width + 2 * HALF_WIDTH,
			boundingBox.height + 2 * HALF_WIDTH + NetViewStyle.BLOCK_BORDER_STROKE.width)

		if (getDestinationArrowOverallLength() > 0) {
			_boundingBox.setFrame(
				boundingBox.x - ARROW_WIDTH, boundingBox.y - ARROW_WIDTH,
				boundingBox.width + 2 * ARROW_WIDTH, boundingBox.height + 2 * ARROW_WIDTH)
		}
	}

	/** ---- [EdgeViewBlockStyling] */

	private interface Consumer {
		fun consume(beginL: Point2D, beginR: Point2D, endR: Point2D, endL: Point2D)
	}

	private fun draw(consumer: Consumer) {
		for (i in 0..edgeView.segmentPointCount - 2) {

			val dir = edgeView.getSegmentDirection(i) ?: return

			var incomingTurn = Turn.NONE
			var outgoingTurn = Turn.NONE

			if (i > 0) {
				incomingTurn = edgeView.getSegmentDirection(i - 1)!!.determineTurn(dir)
			}
			if (i < edgeView.segmentPointCount - 2) {
				outgoingTurn = dir.determineTurn(edgeView.getSegmentDirection(i + 1)!!)
			}

			// Calculate begin points

			val begin = edgeView.getSegmentPoint(i)
			var beginL = begin
			var beginR = begin

			val originArrowOverallLength = getOriginArrowOverallLength()
			val destinationArrowOverallLength = getDestinationArrowOverallLength()
			when (incomingTurn) {
				Turn.AROUND,
					// fallthrough
				Turn.NONE -> when {
					originArrowOverallLength > 0 -> {
						beginL = Point2D(
							begin.x + dir.next().dx * HALF_WIDTH + dir.dx * originArrowOverallLength,
							begin.y + dir.next().dy * HALF_WIDTH + dir.dy * originArrowOverallLength)
						beginR = Point2D(
							begin.x + dir.previous().dx * HALF_WIDTH + dir.dx * originArrowOverallLength,
							begin.y + dir.previous().dy * HALF_WIDTH + dir.dy * originArrowOverallLength)
					}
					edgeView.origin?.connectableView is NodeView<*> -> {
						beginL = Point2D(
							begin.x + (dir.next().dx + dir.dx) * HALF_WIDTH,
							begin.y + (dir.next().dy + dir.dy) * HALF_WIDTH)
						beginR = Point2D(
							begin.x + (dir.previous().dx + dir.dx) * HALF_WIDTH,
							begin.y + (dir.previous().dy + dir.dy) * HALF_WIDTH)
					}
					else -> {
						beginL = Point2D(
							begin.x + dir.next().dx * HALF_WIDTH,
							begin.y + dir.next().dy * HALF_WIDTH)
						beginR = Point2D(
							begin.x + dir.previous().dx * HALF_WIDTH,
							begin.y + dir.previous().dy * HALF_WIDTH)
					}
				}
				Turn.LEFT -> {
					beginL = Point2D(
						begin.x + (dir.dx + dir.next().dx) * HALF_WIDTH,
						begin.y + (dir.dy + dir.next().dy) * HALF_WIDTH)
					beginR = Point2D(
						begin.x + (dir.opposite().dx + dir.previous().dx) * HALF_WIDTH,
						begin.y + (dir.opposite().dy + dir.previous().dy) * HALF_WIDTH)
				}
				Turn.RIGHT -> {
					beginL = Point2D(
						begin.x + (dir.opposite().dx + dir.next().dx) * HALF_WIDTH,
						begin.y + (dir.opposite().dy + dir.next().dy) * HALF_WIDTH)
					beginR = Point2D(
						begin.x + (dir.dx + dir.previous().dx) * HALF_WIDTH,
						begin.y + (dir.dy + dir.previous().dy) * HALF_WIDTH)
				}
            }

			// Calculate end points

			val end = edgeView.getSegmentPoint(i + 1)
			var endL = end
			var endR = end

			when (outgoingTurn) {
				Turn.AROUND,
					// fallthrough
				Turn.NONE -> when {
					destinationArrowOverallLength > 0 -> {
						endL = Point2D(
							end.x + dir.next().dx * HALF_WIDTH - dir.dx * destinationArrowOverallLength,
							end.y + dir.next().dy * HALF_WIDTH - dir.dy * destinationArrowOverallLength)
						endR = Point2D(
							end.x + dir.previous().dx * HALF_WIDTH - dir.dx * destinationArrowOverallLength,
							end.y + dir.previous().dy * HALF_WIDTH - dir.dy * destinationArrowOverallLength)
					}
					edgeView.destination?.connectableView is NodeView<*> -> {
						endL = Point2D(
							end.x + (dir.next().dx + dir.opposite().dx) * HALF_WIDTH,
							end.y + (dir.next().dy + dir.opposite().dy) * HALF_WIDTH)
						endR = Point2D(
							end.x + (dir.previous().dx + dir.opposite().dx) * HALF_WIDTH,
							end.y + (dir.previous().dy + dir.opposite().dy) * HALF_WIDTH)
					}
					else -> {
						endL = Point2D(
							end.x + dir.next().dx * HALF_WIDTH,
							end.y + dir.next().dy * HALF_WIDTH)
						endR = Point2D(
							end.x + dir.previous().dx * HALF_WIDTH,
							end.y + dir.previous().dy * HALF_WIDTH)
					}
				}
				Turn.LEFT -> {
					endL = Point2D(
						end.x + (dir.opposite().dx + dir.next().dx) * HALF_WIDTH,
						end.y + (dir.opposite().dy + dir.next().dy) * HALF_WIDTH)
					endR = Point2D(
						end.x + (dir.dx + dir.previous().dx) * HALF_WIDTH,
						end.y + (dir.dy + dir.previous().dy) * HALF_WIDTH)
				}
				Turn.RIGHT -> {
					endL = Point2D(
						end.x + (dir.dx + dir.next().dx) * HALF_WIDTH,
						end.y + (dir.dy + dir.next().dy) * HALF_WIDTH)
					endR = Point2D(
						end.x + (dir.opposite().dx + dir.previous().dx) * HALF_WIDTH,
						end.y + (dir.opposite().dy + dir.previous().dy) * HALF_WIDTH)
				}
            }

			consumer.consume(beginL, beginR, endR, endL)
		}
	}

	private fun drawArrow(context: DrawContext, endpointType: EdgeViewEndpointType, fill: Boolean) {
		val location = endpointType.getLocation(edgeView)
		val angle = when (endpointType) {
			EdgeViewEndpointType.ORIGIN -> endpointType.getDirection(edgeView)!!.opposite().rotation.angle
			EdgeViewEndpointType.DESTINATION -> endpointType.getDirection(edgeView)!!.rotation.angle
		}

		context.translatedAndRotated(location, angle) {
			if (fill) {
				it.g.fill(getArrowPath(endpointType))
			} else {
				it.g.draw(getArrowPath(endpointType))
			}
		}
	}

	private fun getOriginArrowOverallLength(): Int {
		if (edgeView.isArrow && edgeView.origin?.port != null) {
			return when (edgeView.origin!!.port!!.portType) {
				PortType.INPUT -> ARROW_LENGTH
				PortType.INOUT -> DOUBLE_ARROW_LENGTH
				PortType.OUTPUT -> 0
			}
		}
		return 0
	}

	private fun getOriginArrow(): Path? {
		if (edgeView.isArrow && edgeView.origin?.port != null) {
			return when (edgeView.origin!!.port!!.portType) {
				PortType.INPUT -> ARROW_PATH
				PortType.INOUT -> DOUBLE_ARROW_PATH
				PortType.OUTPUT -> null
			}
		}
		return null
	}

	private fun getDestinationArrowOverallLength(): Int {
		if (edgeView.isArrow && edgeView.destination?.port != null) {
			return when (edgeView.destination!!.port!!.portType) {
				PortType.INPUT -> ARROW_LENGTH
				PortType.INOUT -> DOUBLE_ARROW_LENGTH
				PortType.OUTPUT -> 0
			}
		}
		return 0
	}

	private fun getDestinationArrow(): Path? {
		if (edgeView.isArrow && edgeView.destination?.port != null) {
			return when (edgeView.destination!!.port!!.portType) {
				PortType.INPUT -> ARROW_PATH
				PortType.INOUT -> DOUBLE_ARROW_PATH
				PortType.OUTPUT -> null
			}
		}
		return null
	}

	private fun getArrowPath(endpointType: EdgeViewEndpointType): Path {
		return when (endpointType) {
			EdgeViewEndpointType.ORIGIN -> getOriginArrow()!!
			EdgeViewEndpointType.DESTINATION -> getDestinationArrow()!!
		}
	}
}