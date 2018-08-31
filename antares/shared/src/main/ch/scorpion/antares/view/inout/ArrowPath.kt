package ch.scorpion.antares.view.inout

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.graph.view.VerticeView


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

        val H_INSET = 4
        val V_INSET = 4
        val ARROW_SIZE = 14.0

        class Builder(val orientation: Direction, val contentDimension: Dimension2D) {
            private var path = System.get().createPath()
            private var contentLocation: Point2D = Point2D.ZERO
            private var tailLocation: Point2D = Point2D.ZERO

            fun build(inout: Boolean): ArrowPath {
                when(orientation) {
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

                if (inout) {
                    tailLocation = Point2D(-ARROW_SIZE - 2 * H_INSET - contentDimension.width - ARROW_SIZE, 0.0)
                } else {
                    tailLocation = Point2D(-ARROW_SIZE - 2 * H_INSET - contentDimension.width, 0.0)
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

                if (inout) {
                    tailLocation = Point2D(+ARROW_SIZE + 2 * H_INSET + contentDimension.width + ARROW_SIZE, 0.0)
                } else {
                    tailLocation = Point2D(+ARROW_SIZE + 2 * H_INSET + contentDimension.width, 0.0)
                }
            }

            private fun buildSouth(inout: Boolean) {
                val remW = calculateRemainingWidth()

                path.moveTo(0, 0)
                path.lineTo(ARROW_SIZE, -ARROW_SIZE)
                path.lineTo(ARROW_SIZE + remW, -ARROW_SIZE)
                path.lineTo(ARROW_SIZE + remW, -ARROW_SIZE - 2 * V_INSET - contentDimension.height)
                if (inout) {
                    path.lineTo(ARROW_SIZE, -ARROW_SIZE - 2 * V_INSET - contentDimension.height)
                    path.lineTo(0.0, -ARROW_SIZE - 2 * V_INSET - contentDimension.height - ARROW_SIZE)
                    path.lineTo(-ARROW_SIZE, -ARROW_SIZE - 2 * V_INSET - contentDimension.height)
                    path.lineTo(-ARROW_SIZE - remW, -ARROW_SIZE - 2 * V_INSET - contentDimension.height)
                } else {
                    path.lineTo(-ARROW_SIZE - remW, -ARROW_SIZE - 2 * V_INSET - contentDimension.height)
                }
                path.lineTo(-ARROW_SIZE - remW, -ARROW_SIZE)
                path.lineTo(-ARROW_SIZE, -ARROW_SIZE)
                path.close()

                contentLocation = Point2D(
                        -ARROW_SIZE - remW + H_INSET,
                        -ARROW_SIZE - contentDimension.height - V_INSET)

                if (inout) {
                    tailLocation = Point2D(0.0, -ARROW_SIZE - 2 * V_INSET - contentDimension.height - ARROW_SIZE)
                } else {
                    tailLocation = Point2D(0.0, -ARROW_SIZE - 2 * V_INSET - contentDimension.height)
                }
            }

            private fun buildNorth(inout: Boolean) {
                val remW = calculateRemainingWidth()

                path.moveTo(0, 0)
                path.lineTo(-ARROW_SIZE, ARROW_SIZE)
                path.lineTo(-ARROW_SIZE - remW, ARROW_SIZE)
                path.lineTo(-ARROW_SIZE - remW, ARROW_SIZE + 2 * V_INSET + contentDimension.height)
                if (inout) {
                    path.lineTo(-ARROW_SIZE, ARROW_SIZE + 2 * V_INSET + contentDimension.height)
                    path.lineTo(0.0, ARROW_SIZE + 2 * V_INSET + contentDimension.height + ARROW_SIZE)
                    path.lineTo(+ARROW_SIZE, ARROW_SIZE + 2 * V_INSET + contentDimension.height)
                    path.lineTo(ARROW_SIZE + remW, ARROW_SIZE + 2 * V_INSET + contentDimension.height)
                } else {
                    path.lineTo(ARROW_SIZE + remW, ARROW_SIZE + 2 * V_INSET + contentDimension.height)
                }
                path.lineTo(ARROW_SIZE + remW, ARROW_SIZE)
                path.lineTo(ARROW_SIZE, ARROW_SIZE)
                path.close()

                contentLocation = Point2D(
                        -ARROW_SIZE - remW + H_INSET,
                        ARROW_SIZE + V_INSET)

                if (inout) {
                    tailLocation = Point2D(0.0, ARROW_SIZE + 2 * V_INSET + contentDimension.height + ARROW_SIZE)
                } else {
                    tailLocation = Point2D(0.0, ARROW_SIZE + 2 * V_INSET + contentDimension.height)
                }
            }

            /**
             * Calculates the height of a horizontal [ArrowPath] that extends the arrowhead height.
             * @return the height of a horizontal [ArrowPath] that extends the arrowhead height.
             */
            private fun calculateRemainingHeight(): Double {
                return (contentDimension.height + 2 * V_INSET - 2 * ARROW_SIZE) / 2
            }

            /**
             * Calculates the width of a vertical [ArrowPath] that extends the arrowhead width.
             * @return the width of a vertical [ArrowPath] that extends the arrowhead width.
             */
            private fun calculateRemainingWidth(): Double {
                return (contentDimension.width + 2 * H_INSET - 2 * ARROW_SIZE) / 2
            }
        }
    }
}
