package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.collection.Pair
import ch.scorpion.jabbah.edit.SnapResult
import ch.scorpion.jabbah.edit.Snapper
import ch.scorpion.jabbah.edit.model.polyline.CompactablePolyline
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.base.logger

/**
 * Layout algorithm for [LayoutType.ORTHOGONAL].
 */
object OrthoEdgeViewLayouter : EdgeViewLayouter {

	private val LOG by logger(OrthoEdgeViewLayouter::class)

	// TODO Make configurable in order to align with GridImpl width
	private const val END_LENGTH = 14

	/** ---- [EdgeViewLayouter] */

	fun layout(edgeView: EdgeView<*>?, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary): List<Point2D> {
		if (begin.point == end.point) {
			return listOf(begin.point, end.point)
		}

		// Holds all generated solutions
		val solutions = mutableListOf<Solution>()

		// Create possible solutions by first creating a Point list that only contains the first and last segments,
		// and by then completing these list in all possible ways, which yields the different solutions.
		createSolutions(solutions, begin, end) { a, b -> createD(a, b, graphView.snapper) }
		createSolutions(solutions, begin, end) { a, b -> createC(a, b, graphView.snapper) }
		createSolutions(solutions, begin, end) { a, b -> createB(a, b) }
		createSolutions(solutions, begin, end) { a, b -> createA(a, b) }

		if (LOG.isTraceEnabled()) {
			LOG.trace("solutions:")
			solutions.forEach { LOG.trace("- ${it.polyline}") }
		}

		if (solutions.size == 0) {
			// begin and end must both be collinear and counter-directive
			LOG.trace("using fallback solution")
			return createFallbackSolution(begin.point, end.point)
		}
		if (solutions.size == 1) {
			return solutions[0].polyline.points
		}

		solutions.sortWith(SolutionEvaluator(edgeView))
		val minIndex = 0

		if (LOG.isTraceEnabled()) {
			LOG.trace("Choosing solution with ${solutions[minIndex].polyline.size} points")
		}

		return solutions[minIndex].polyline.points
	}

	override fun layoutOrigin(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary, destPointIndex: Int, compact: Boolean) {
		val points = mutableListOf<Point2D>()
		points.addAll(layout(edgeView, graphView, begin, end))
		points.addAll(edgeView.polyline.getPoints(destPointIndex + 1, edgeView.polyline.pointsCount))
		edgeView.setLaidOutPoints(points, compact)
	}

	override fun layoutDestination(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary, origPointIndex: Int, compact: Boolean) {
		val points = mutableListOf<Point2D>()
		points.addAll(layout(edgeView, graphView, begin, end))
		points.addAll(0, edgeView.polyline.getPoints(0, origPointIndex))
		edgeView.setLaidOutPoints(points, compact)
	}

	override fun layoutAll(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary) {
		edgeView.setLaidOutPoints(layout(edgeView, graphView, begin, end), compact = true)
	}

	/** ---- [OrthoEdgeViewLayouter] */

	private fun createFallbackSolution(begin: Point2D, end: Point2D): List<Point2D> = listOf(begin, end)

	/** Compares two [Solution]s in respect of the original [EdgeView] for which they solve the layout problem.*/
	private class SolutionEvaluator(private val edgeView: EdgeView<*>?) : Comparator<Solution> {

		override fun compare(a: Solution, b: Solution): Int {
			// First priority: Always prefer non-counter-directive solutions
			if (a.isCounterDirective != b.isCounterDirective) {
				// Workaround for Kotlin Bug KT-19177
				//return s1.isCounterDirective.compareTo(s2.isCounterDirective)
				return if (!a.isCounterDirective && b.isCounterDirective) -1
				else if (a.isCounterDirective && !b.isCounterDirective) 1 else 0
			}

			// Second priority: Smaller number of points
			val sizeCompare = a.polyline.size.compareTo(b.polyline.size)
			if (sizeCompare != 0) {
				return sizeCompare
			}
			if (edgeView == null) {
				return 0
			}
			// Third priority: Smaller number of direction differences compared to original EdgeView
			return countDirectionDiff(a.polyline).compareTo(countDirectionDiff(b.polyline))
		}

		private fun countDirectionDiff(polyline: CompactablePolyline): Int {
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

	private fun createSolutions(
		solutions: MutableList<Solution>,
		begin: LayoutBoundary,
		end: LayoutBoundary,
		layoutSupplier: (Point2D, Point2D) -> List<Point2D>
	) {
		val product = Pair.cartesianProduct(begin.directions.ifEmpty { Direction.ALL }, end.directions.ifEmpty { Direction.ALL })
		for (pair in product) {
			val pointList = createPointList(begin.point, pair.first, begin.isPort, end.point, pair.second, end.isPort)
			pointList.addAll(2, layoutSupplier.invoke(pointList[1], pointList[2]))

			val polyline = CompactablePolyline(pointList)
			polyline.compact()

			if (polyline.isOrthogonal) {
				solutions.add(Solution(polyline, pair.first, begin.isPort, pair.second, end.isPort))
			}
		}
	}

	private data class Solution(
		val polyline: CompactablePolyline,
		val beginDir: Direction,
		val isBeginPort: Boolean,
		val endDir: Direction,
		val isEndPort: Boolean
	) {
		val isCounterDirective: Boolean
			get() {
				if (isBeginPort && polyline.getSegmentDirection(0) != beginDir) {
					return true
				}
				if (isEndPort && polyline.getSegmentDirection(polyline.size - 2) != endDir) {
					return true
				}
				return false
			}
	}

	/** Lower left corner. */
	private fun createA(p1: Point2D, p2: Point2D): List<Point2D> = listOf(Point2D(p1.x, p2.y))

	/** Upper right corner. */
	private fun createB(p1: Point2D, p2: Point2D): List<Point2D> = listOf(Point2D(p2.x, p1.y))

	/** Horizontally in the middle of the two points. */
	private fun createC(p1: Point2D, p2: Point2D, snapper: Snapper?): List<Point2D> {
		val list = mutableListOf<Point2D>()
		val dy = p2.y - p1.y
		list.add(snapY(snapper, Point2D(p1.x, p1.y + 0.5 * dy)))
		list.add(snapY(snapper, Point2D(p2.x, p1.y + 0.5 * dy)))
		return list
	}

	/** Vertically in the middle of the two points.*/
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