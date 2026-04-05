package io.antarescircuit.antares.view.inout

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Path
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.graph.view.VerticeView


/**
 * Represents the arrow shape of multi-bit input and output [VerticeView]s.
 * The (0,0) origin is located at the head of the arrow shape.
 *
 * @property path the outline path of this [ArrowPath]
 * @property tailLocation The location of the arrow's tail in relation to the arrowhead, which is at (0,0)
 * @property contentLocation the upper-left location of the content of this [ArrowPath]
 */
class ArrowPath(
	val path: Path,
	val tailLocation: Point2D,
	val contentLocation: Point2D
) {

	companion object {

		const val H_INSET = 4
		const val V_INSET = 4
		const val ARROW_SIZE = 14.0

		class Builder(val orientation: Direction, val contentDimension: Dimension2D) {

			private var path = System.createPath()
			private var contentLocation: Point2D = Point2D.ZERO
			private var tailLocation: Point2D = Point2D.ZERO

			private val contentHeight = contentDimension.height.coerceAtLeast(2 * ARROW_SIZE - 2 * V_INSET)

			fun build(inout: Boolean): ArrowPath {
				when (orientation) {
					Direction.EAST -> buildEast(inout)
					Direction.SOUTH -> buildSouth(inout)
					Direction.WEST -> buildWest(inout)
					Direction.NORTH -> buildNorth(inout)
				}
				return ArrowPath(path, tailLocation, contentLocation)
			}

			private fun buildEast(inout: Boolean) {
				val remH = calculateRemainingHeight()

				path.moveTo(0, 0)
				path.lineTo(-ARROW_SIZE, -ARROW_SIZE)
				path.lineTo(-ARROW_SIZE, -ARROW_SIZE - remH)
				path.lineTo(-ARROW_SIZE - 2 * H_INSET - contentDimension.width, -ARROW_SIZE - remH)
				if (inout) {
					path.lineTo(-ARROW_SIZE - 2 * H_INSET - contentDimension.width, -ARROW_SIZE)
					path.lineTo(-ARROW_SIZE - 2 * H_INSET - contentDimension.width - ARROW_SIZE, 0.0)
					path.lineTo(-ARROW_SIZE - 2 * H_INSET - contentDimension.width, +ARROW_SIZE)
					path.lineTo(-ARROW_SIZE - 2 * H_INSET - contentDimension.width, +ARROW_SIZE + remH)
				} else {
					path.lineTo(-ARROW_SIZE - 2 * H_INSET - contentDimension.width, +ARROW_SIZE + remH)
				}
				path.lineTo(-ARROW_SIZE, +ARROW_SIZE + remH)
				path.lineTo(-ARROW_SIZE, +ARROW_SIZE)
				path.close()

				contentLocation = Point2D(
					-ARROW_SIZE - H_INSET - contentDimension.width,
					-ARROW_SIZE - remH + V_INSET)

				tailLocation = if (inout) {
					Point2D(-ARROW_SIZE - 2 * H_INSET - contentDimension.width - ARROW_SIZE, 0.0)
				} else {
					Point2D(-ARROW_SIZE - 2 * H_INSET - contentDimension.width, 0.0)
				}
			}

			private fun buildWest(inout: Boolean) {
				val remH = calculateRemainingHeight()

				path.moveTo(0, 0)
				path.lineTo(ARROW_SIZE, ARROW_SIZE)
				path.lineTo(ARROW_SIZE, ARROW_SIZE + remH)
				path.lineTo(ARROW_SIZE + 2 * H_INSET + contentDimension.width, ARROW_SIZE + remH)
				if (inout) {
					path.lineTo(ARROW_SIZE + 2 * H_INSET + contentDimension.width, ARROW_SIZE)
					path.lineTo(ARROW_SIZE + 2 * H_INSET + contentDimension.width + ARROW_SIZE, 0.0)
					path.lineTo(ARROW_SIZE + 2 * H_INSET + contentDimension.width, -ARROW_SIZE)
					path.lineTo(ARROW_SIZE + 2 * H_INSET + contentDimension.width, -ARROW_SIZE - remH)
				} else {
					path.lineTo(ARROW_SIZE + 2 * H_INSET + contentDimension.width, -ARROW_SIZE - remH)
				}
				path.lineTo(ARROW_SIZE, -ARROW_SIZE - remH)
				path.lineTo(ARROW_SIZE, -ARROW_SIZE)
				path.close()

				contentLocation = Point2D(
					ARROW_SIZE + H_INSET,
					-ARROW_SIZE - remH + V_INSET)

				tailLocation = if (inout) {
					Point2D(+ARROW_SIZE + 2 * H_INSET + contentDimension.width + ARROW_SIZE, 0.0)
				} else {
					Point2D(+ARROW_SIZE + 2 * H_INSET + contentDimension.width, 0.0)
				}
			}

			private fun buildSouth(inout: Boolean) {
				val remW = calculateRemainingWidth()

				path.moveTo(0, 0)
				path.lineTo(ARROW_SIZE, -ARROW_SIZE)
				path.lineTo(ARROW_SIZE + remW, -ARROW_SIZE)
				path.lineTo(ARROW_SIZE + remW, -ARROW_SIZE - 2 * V_INSET - contentHeight)
				if (inout) {
					path.lineTo(ARROW_SIZE, -ARROW_SIZE - 2 * V_INSET - contentHeight)
					path.lineTo(0.0, -ARROW_SIZE - 2 * V_INSET - contentHeight - ARROW_SIZE)
					path.lineTo(-ARROW_SIZE, -ARROW_SIZE - 2 * V_INSET - contentHeight)
					path.lineTo(-ARROW_SIZE - remW, -ARROW_SIZE - 2 * V_INSET - contentHeight)
				} else {
					path.lineTo(-ARROW_SIZE - remW, -ARROW_SIZE - 2 * V_INSET - contentHeight)
				}
				path.lineTo(-ARROW_SIZE - remW, -ARROW_SIZE)
				path.lineTo(-ARROW_SIZE, -ARROW_SIZE)
				path.close()

				contentLocation = Point2D(
					-ARROW_SIZE - remW + H_INSET,
					-ARROW_SIZE - contentHeight - V_INSET)

				tailLocation = if (inout) {
					Point2D(0.0, -ARROW_SIZE - 2 * V_INSET - contentHeight - ARROW_SIZE)
				} else {
					Point2D(0.0, -ARROW_SIZE - 2 * V_INSET - contentHeight)
				}
			}

			private fun buildNorth(inout: Boolean) {
				val remW = calculateRemainingWidth()

				path.moveTo(0, 0)
				path.lineTo(-ARROW_SIZE, ARROW_SIZE)
				path.lineTo(-ARROW_SIZE - remW, ARROW_SIZE)
				path.lineTo(-ARROW_SIZE - remW, ARROW_SIZE + 2 * V_INSET + contentHeight)
				if (inout) {
					path.lineTo(-ARROW_SIZE, ARROW_SIZE + 2 * V_INSET + contentHeight)
					path.lineTo(0.0, ARROW_SIZE + 2 * V_INSET + contentHeight + ARROW_SIZE)
					path.lineTo(+ARROW_SIZE, ARROW_SIZE + 2 * V_INSET + contentHeight)
					path.lineTo(ARROW_SIZE + remW, ARROW_SIZE + 2 * V_INSET + contentHeight)
				} else {
					path.lineTo(ARROW_SIZE + remW, ARROW_SIZE + 2 * V_INSET + contentHeight)
				}
				path.lineTo(ARROW_SIZE + remW, ARROW_SIZE)
				path.lineTo(ARROW_SIZE, ARROW_SIZE)
				path.close()

				contentLocation = Point2D(
					-ARROW_SIZE - remW + H_INSET,
					ARROW_SIZE + V_INSET)

				tailLocation = if (inout) {
					Point2D(0.0, ARROW_SIZE + 2 * V_INSET + contentHeight + ARROW_SIZE)
				} else {
					Point2D(0.0, ARROW_SIZE + 2 * V_INSET + contentHeight)
				}
			}

			/**
			 * Calculates the height of a horizontal [ArrowPath] that extends the arrowhead height.
			 */
			private fun calculateRemainingHeight(): Double =
				(contentHeight + 2 * V_INSET - 2 * ARROW_SIZE) / 2

			/**
			 * Calculates the width of a vertical [ArrowPath] that extends the arrowhead width.
			 */
			private fun calculateRemainingWidth(): Double =
				(contentDimension.width + 2 * H_INSET - 2 * ARROW_SIZE) / 2
		}
	}
}
