package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.collection.Pair
import ch.scorpion.jabbah.edit.SnapResult
import ch.scorpion.jabbah.edit.Snapper
import ch.scorpion.jabbah.edit.model.polyline.OrthoPolyline
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.base.logger

/**
 * Layout algorithm for [Layout.ORTHOGONAL].
 */
class OrthoEdgeViewLayout : EdgeViewLayout {

    companion object {
        val LOG by logger()
        // TODO Make configurable in order to align with GridImpl width
        val END_LENGTH = 14
    }

    /** ---- [EdgeViewLayout] */

    override fun layout(edgeView: EdgeView<*>?, graphView: GraphView<*>, begin: LayoutBoundary, end: LayoutBoundary): List<Point2D> {
        if (begin.point == end.point) {
            return listOf(begin.point, end.point)
        }

        // Holds all generated solutions
        val solutions = mutableListOf<Solution>()

        // Create possible solutions by first creating a Point list that only contains the first and last segments,
        // and by then completing these list in all possible ways, which yields the different solutions.
        createSolution(solutions, begin, end, {a,b -> createD(a, b, graphView.snapper)})
        createSolution(solutions, begin, end, {a,b -> createC(a, b, graphView.snapper)})
        createSolution(solutions, begin, end, {a,b -> createB(a, b)})
        createSolution(solutions, begin, end, {a,b -> createA(a, b)})

        if (LOG.isDebugEnabled()) {
            LOG.debug("OrthoEdgeViewLayout solutions:")
            solutions.forEach { LOG.debug("- ${it.polyline}") }
        }

        if (solutions.size == 0) {
            // begin and end must both be collinear and counter-directive
            return createFallbackSolution(begin.point, end.point)
        }
        if (solutions.size == 1) {
            return solutions[0].polyline.points
        }

        solutions.sortWith(SolutionEvaluator(edgeView))
        val minIndex = 0

        LOG.debug("Choosing solution with ${solutions[minIndex].polyline.size} points")

        return solutions[minIndex].polyline.points
    }

    /** ---- [OrthoEdgeViewLayout] */

    private fun createFallbackSolution(begin: Point2D, end: Point2D): List<Point2D> {
        return listOf(begin, end)
    }

    /** Compares two [Solution]s in respect of the original [EdgeView] for which they solve the layout problem.*/
    private class SolutionEvaluator(private val edgeView: EdgeView<*>?) : Comparator<Solution> {

        override fun compare(s1: Solution, s2: Solution): Int {
            // First priority: Always prefer non-counter-directive solutions
            if (s1.isCounterDirective != s2.isCounterDirective) {
                return s1.isCounterDirective.compareTo(s2.isCounterDirective)
            }

            // Second priority: Smaller number of points
            val sizeCompare = s1.polyline.size.compareTo(s2.polyline.size)
            if (sizeCompare != 0) {
                return sizeCompare
            }
            if (edgeView == null) {
                return 0
            }
            // Third priority: Smaller number of direction differences compared to original EdgeView
            return countDirectionDiff(s1.polyline).compareTo(countDirectionDiff(s2.polyline))
        }

        private fun countDirectionDiff(polyline: OrthoPolyline): Int {
            var directionDiff = 0
            val origDir = edgeView!!.getSegmentDirection(0)
            if (origDir != null) {
                if (polyline.getSegmentDirection(0) != origDir) {
                    directionDiff++
                }
            }
            val destDir = edgeView.getSegmentDirection(edgeView.segmentPointCount - 2)
            if (destDir != null) {
                if (polyline.getSegmentDirection(polyline.size - 2) != destDir) {
                    directionDiff++
                }
            }

            return directionDiff
        }
    }

    private fun createSolution(
        solutions: MutableList<Solution>,
        begin: LayoutBoundary,
        end: LayoutBoundary,
        layoutSupplier: (Point2D,Point2D) -> List<Point2D>
    ) {
        val product = Pair.cartesianProduct(begin.directions, end.directions)
        for (pair in product) {
            val pointList = createPointList(begin.point, pair.first, begin.isPort, end.point, pair.second, end.isPort)
            pointList.addAll(2, layoutSupplier.invoke(pointList[1], pointList[2]))
            val solution = Solution(OrthoPolyline(pointList), pair.first, begin.isPort, pair.second, end.isPort)
            solution.polyline.compact()
            solutions.add(solution)
        }
    }

    private data class Solution(
            val polyline: OrthoPolyline,
            val beginDir: Direction,
            val isBeginPort: Boolean,
            val endDir: Direction,
            val isEndPort: Boolean
    ) {
        val isCounterDirective: Boolean get() {
            if (isBeginPort && polyline.getSegmentDirection(0) != beginDir) {
                return true
            }
            if (isEndPort && polyline.getSegmentDirection(polyline.size - 2) != endDir) {
                return true
            }
            return false
        }
    }

    private fun createA(p1: Point2D, p2: Point2D): List<Point2D> {
        return listOf(Point2D(p1.x, p2.y))
    }

    private fun createB(p1: Point2D, p2: Point2D): List<Point2D> {
        return listOf(Point2D(p2.x, p1.y))
    }

    private fun createC(p1: Point2D, p2: Point2D, snapper: Snapper?): List<Point2D> {
        val list = mutableListOf<Point2D>()
        val dy = p2.y - p1.y
        list.add(snapY(snapper, Point2D(p1.x, p1.y + 0.5 * dy)))
        list.add(snapY(snapper, Point2D(p2.x, p1.y + 0.5 * dy)))
        return list
    }

    private fun createD(p1: Point2D, p2: Point2D, snapper: Snapper?): List<Point2D> {
        val list = mutableListOf<Point2D>()
        val dx = p2.x - p1.x
        list.add(snapX(snapper, Point2D(p1.x + 0.5 * dx, p1.y)))
        list.add(snapX(snapper, Point2D(p1.x + 0.5 * dx, p2.y)))
        return list
    }

    private fun snapX(snapper: Snapper?, point: Point2D): Point2D {
        if (snapper != null) {
            val result = SnapResult()
            snapper.snapX(point.x, result)
            return point.add(result.dx, 0.0)
        }
        return point
    }

    private fun snapY(snapper: Snapper?, point: Point2D): Point2D {
        if (snapper != null) {
            val result = SnapResult()
            snapper.snapY(point.y, result)
            return point.add(0.0, result.dy)
        }
        return point
    }

    private fun createPointList(
        beginPoint: Point2D, beginDir: Direction, isBeginPort: Boolean,
        endPoint: Point2D, endDir: Direction, isEndPort: Boolean
    ): MutableList<Point2D> {
        val list = mutableListOf<Point2D>()
        list.add(beginPoint)
        list.add(Point2D(
            beginPoint.x + beginDir.dx * (if (isBeginPort) END_LENGTH.toDouble() else 0.0),
            beginPoint.y + beginDir.dy * (if (isBeginPort) END_LENGTH.toDouble() else 0.0)))
        list.add(Point2D(
            endPoint.x - endDir.dx * (if (isEndPort) END_LENGTH.toDouble() else 0.0),
            endPoint.y - endDir.dy * (if (isEndPort) END_LENGTH.toDouble() else 0.0)))
        list.add(endPoint)
        return list
    }
}