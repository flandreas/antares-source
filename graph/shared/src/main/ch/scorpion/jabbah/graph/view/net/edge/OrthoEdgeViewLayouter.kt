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
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.polyline.PolylineInterference
import ch.scorpion.jabbah.draw.polyline.PolylineShape
import ch.scorpion.jabbah.edit.Look
import kotlin.math.abs
import kotlin.math.sign

/**
 * Layout algorithm for [LayoutType.ORTHOGONAL].
 */
object OrthoEdgeViewLayouter : EdgeViewLayouter {

	/** The name of the [Boolean] property that controls whether advanced layout is to be applied. */
	const val PROP_ADVANCED_LAYOUT = "graph.advancedEdgeViewLayout"

	private val LOG by logger(OrthoEdgeViewLayouter::class)

	private const val END_LENGTH = 2 * Look.GRID

	private val useAdvancedLayout: Boolean by lazy { BaseModule.properties.getBoolean(PROP_ADVANCED_LAYOUT) }

	/**
	 * The distance to be applied when displacing segments in order to avoid overlapping.
	 */
	private const val DISPLACEMENT = Look.GRID

	private fun checkDirections(boundary: LayoutBoundary, directions: Set<Direction>): Boolean {
		return boundary.isPort
			|| boundary.directions.isEmpty()
			|| boundary.directions.any { directions.contains(it) }
	}

	fun layout(edgeView: EdgeView<*>?, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary): List<Point2D> {
		if (begin.point == end.point) {
			return listOf(begin.point, end.point)
		}

		// Holds all generated solutions
		val solutions = mutableListOf<Solution>()

		val otherEdgeViews = graphView.getEdgeViews().filter { edgeView == null || it !== edgeView }

		// Create possible solutions by first creating a Point list that only contains the first and last segments,
		// and by then completing these lists in all possible ways, which yields the different solutions.

		if (checkDirections(begin, beginDirectionsD) && checkDirections(end, endDirectionsD)) {
			createSolutions(solutions, begin, end) { a, b -> createD(a, b, graphView, otherEdgeViews) }
		}
		if (checkDirections(begin, beginDirectionsC) && checkDirections(end, endDirectionsC)) {
			createSolutions(solutions, begin, end) { a, b -> createC(a, b, graphView, otherEdgeViews) }
		}
		if (checkDirections(begin, beginDirectionsB) && checkDirections(end, endDirectionsB)) {
			createSolutions(solutions, begin, end) { a, b -> createB(a, b) }
		}
		if (checkDirections(begin, beginDirectionsA) && checkDirections(end, endDirectionsA)) {
			createSolutions(solutions, begin, end) { a, b -> createA(a, b) }
		}

		if (LOG.isTraceEnabled()) {
			LOG.trace("solutions:")
			solutions.forEach { LOG.trace("- ${it.polyline}") }
		}

		if (solutions.isEmpty()) {
			// begin and end must both be collinear and counter-directive
			LOG.trace("using fallback solution")
			return createFallbackSolution(begin.point, end.point)
		}
		if (solutions.size == 1) {
			return solutions[0].polyline.points
		}

		if (useAdvancedLayout) {
			val otherPoints = otherEdgeViews.map { it.polyline.getPointList() }
			for (solution in solutions) {
				solution.interference = PolylineShape.calculateInterference(solution.polyline.points, otherPoints)
			}
		}

		solutions.sortWith(SolutionEvaluator(edgeView))
		val minIndex = 0

		if (LOG.isTraceEnabled()) {
			LOG.trace("Choosing solution with ${solutions[minIndex].polyline.size} points")
		}

		return solutions[minIndex].polyline.points
	}

	/** ---- [EdgeViewLayouter] */

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

			// Smaller number of points
			val sizeCompare = a.polyline.size.compareTo(b.polyline.size)
			if (sizeCompare != 0) {
				return sizeCompare
			}

			if (a.interference.overlappingCount != b.interference.overlappingCount) {
				return a.interference.overlappingCount.compareTo(b.interference.overlappingCount)
			}

			if (a.interference.intersectionCount != b.interference.intersectionCount) {
				return a.interference.intersectionCount.compareTo(b.interference.intersectionCount)
			}

			if (edgeView == null) {
				return 0
			}
			// Smaller number of direction differences compared to original EdgeView
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
		layoutSupplier: (Point2D, Point2D) -> Collection<List<Point2D>>
	) {
		val product = Pair.cartesianProduct(begin.directions.ifEmpty { Direction.ALL }, end.directions.ifEmpty { Direction.ALL })
		for (pair in product) {
			val pointList = createPointList(begin.point, pair.first, begin.isPort, end.point, pair.second, end.isPort)

			val layouts = layoutSupplier(pointList[1], pointList[2])
			layouts.forEach { layout ->
				val pl = pointList.toMutableList()
				pl.addAll(2, layout)
				val polyline = CompactablePolyline(pl)
				polyline.compact()
				if (polyline.isOrthogonal) {
					solutions.add(Solution(polyline, pair.first, begin.isPort, pair.second, end.isPort))
				}
			}
		}
	}

	private data class Solution(
		val polyline: CompactablePolyline,
		val beginDir: Direction,
		val isBeginPort: Boolean,
		val endDir: Direction,
		val isEndPort: Boolean,
		var interference: PolylineInterference = PolylineInterference.ZERO
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

	private val beginDirectionsA: Set<Direction> get() = Direction.VERTICAL
	private val endDirectionsA: Set<Direction> get() = Direction.HORIZONTAL

	/** Lower left corner. */
	private fun createA(p1: Point2D, p2: Point2D): Collection<List<Point2D>> = listOf(listOf(Point2D(p1.x, p2.y)))

	private val beginDirectionsB: Set<Direction> get() = Direction.HORIZONTAL
	private val endDirectionsB: Set<Direction> get() = Direction.VERTICAL

	/** Upper right corner. */
	private fun createB(p1: Point2D, p2: Point2D): Collection<List<Point2D>> = listOf(listOf(Point2D(p2.x, p1.y)))

	private val beginDirectionsC: Set<Direction> get() = Direction.VERTICAL
	private val endDirectionsC: Set<Direction> get() = Direction.VERTICAL

	/** Horizontally in the middle of the two points. */
	private fun createC(p1: Point2D, p2: Point2D, graphView: GraphView, otherEdgeViews: List<EdgeView<*>>): Collection<List<Point2D>> {
		val middleDy = 0.5 * (p2.y - p1.y)
		val simpleSolution = createCImpl(p1, p2, middleDy, graphView.snapper)
		val displacementSpaceHalf = (abs(p2.y - p1.y) - 2 * END_LENGTH).coerceAtLeast(0.0) / 2

		if (displacementSpaceHalf < DISPLACEMENT) {
			// Not enough space for displacement
			return listOf(simpleSolution)
		}

		if (!otherEdgeViews.any { it.polyline.overlapsOrthogonallyWith(0, simpleSolution) }) {
			// No displacement necessary
			return listOf(simpleSolution)
		}

		if (useAdvancedLayout) {

			val direction = middleDy.sign

			// Find the first non-overlapping displacement on one side
			var displacedLayout1: List<Point2D>? = null
			var dy = direction * DISPLACEMENT
			while (displacedLayout1 == null && abs(dy) <= displacementSpaceHalf) {
				val layout = createCImpl(p1, p2, middleDy + dy, graphView.snapper)
				if (!otherEdgeViews.any { it.polyline.overlapsOrthogonallyWith(0, layout) }) {
					displacedLayout1 = layout
				}
				dy += direction * DISPLACEMENT
			}

			// Find the first non-overlapping displacement on other side
			var displacedLayout2: List<Point2D>? = null
			dy = -direction * DISPLACEMENT
			while (displacedLayout2 == null && abs(dy) <= displacementSpaceHalf) {
				val layout = createCImpl(p1, p2, middleDy + dy, graphView.snapper)
				if (!otherEdgeViews.any { it.polyline.overlapsOrthogonallyWith(0, layout) }) {
					displacedLayout2 = layout
				}
				dy -= direction * DISPLACEMENT
			}

			if (displacedLayout1 != null || displacedLayout2 != null) {
				return mutableListOf<List<Point2D>>().also { solutions ->
					displacedLayout1?.let { solutions.add(it) }
					displacedLayout2?.let { solutions.add(it) }
				}
			}
		}

		return listOf(simpleSolution)
	}

	private fun createCImpl(p1: Point2D, p2: Point2D, dy: Double, snapper: Snapper?): List<Point2D> =
		mutableListOf<Point2D>().apply {
			add(snapY(snapper, Point2D(p1.x, p1.y + dy)))
			add(snapY(snapper, Point2D(p2.x, p1.y + dy)))
		}

	private val beginDirectionsD: Set<Direction> get() = Direction.HORIZONTAL
	private val endDirectionsD: Set<Direction> get() = Direction.HORIZONTAL

	/** Vertically in the middle of the two points.*/
	private fun createD(p1: Point2D, p2: Point2D, graphView: GraphView, otherEdgeViews: List<EdgeView<*>>): Collection<List<Point2D>> {
		val middleDx = 0.5 * (p2.x - p1.x)
		val simpleSolution = createDImpl(p1, p2, middleDx, graphView.snapper)
		val displacementSpaceHalf = (abs(p2.x - p1.x) - 2 * END_LENGTH).coerceAtLeast(0.0) / 2

		if (displacementSpaceHalf < DISPLACEMENT) {
			// Not enough space for displacement
			return listOf(simpleSolution)
		}

		if (!otherEdgeViews.any { it.polyline.overlapsOrthogonallyWith(0, simpleSolution) }) {
			// No displacement necessary
			return listOf(simpleSolution)
		}

		if (useAdvancedLayout) {

			val direction = middleDx.sign

			// Find the first non-overlapping displacement on one side
			var displacedLayout1: List<Point2D>? = null
			var dx = direction * DISPLACEMENT
			while (displacedLayout1 == null && abs(dx) <= displacementSpaceHalf) {
				val layout = createDImpl(p1, p2, middleDx + dx, graphView.snapper)
				if (!otherEdgeViews.any { it.polyline.overlapsOrthogonallyWith(0, layout) }) {
					displacedLayout1 = layout
				}
				dx += direction * DISPLACEMENT
			}

			// Find the first non-overlapping displacement on other side
			var displacedLayout2: List<Point2D>? = null
			dx = -direction * DISPLACEMENT
			while (displacedLayout2 == null && abs(dx) <= displacementSpaceHalf) {
				val layout = createDImpl(p1, p2, middleDx + dx, graphView.snapper)
				if (!otherEdgeViews.any { it.polyline.overlapsOrthogonallyWith(0, layout) }) {
					displacedLayout2 = layout
				}
				dx -= direction * DISPLACEMENT
			}

			if (displacedLayout1 != null || displacedLayout2 != null) {
				return mutableListOf<List<Point2D>>().also { solutions ->
					displacedLayout1?.let { solutions.add(it) }
					displacedLayout2?.let { solutions.add(it) }
				}
			}
		}

		return listOf(simpleSolution)
	}

	private fun createDImpl(p1: Point2D, p2: Point2D, dx: Double, snapper: Snapper?): List<Point2D> =
		mutableListOf<Point2D>().apply {
			add(snapX(snapper, Point2D(p1.x + dx, p1.y)))
			add(snapX(snapper, Point2D(p1.x + dx, p2.y)))
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