package io.antarescircuit.antares.view.net

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.net.DigitalNet
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.model.signal.DigitalSignalRepresentation
import io.antarescircuit.jabbah.animation.AnimatorImpl
import io.antarescircuit.jabbah.base.time.ControlledTimeService
import io.antarescircuit.jabbah.base.time.ControlledTimer
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.container.DrawableContainerImpl
import io.antarescircuit.jabbah.draw.polyline.PolylineShape
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.execution.actor.ActorData
import io.antarescircuit.jabbah.execution.actor.ActorListener
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.graph.view.ConnectableView
import io.antarescircuit.jabbah.graph.view.Connection
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.net.netview.NetViewImpl
import io.antarescircuit.jabbah.graph.view.net.node.NodeView
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DigitalEdgeViewNetAnimationFlowTest {

	private lateinit var animator: AnimatorImpl
	private lateinit var time: ControlledTimeService
	private lateinit var drawingView: DrawingView<GraphElementView<*>, GraphView>
	private lateinit var container: DrawableContainerImpl<Drawable>
	private lateinit var scheduler: Scheduler
	private lateinit var actorListener: ActorListener
	private lateinit var actorData: ActorData
	private lateinit var net: DigitalNet
	private lateinit var netView: NetViewImpl<DigitalSignal>
	private lateinit var port: DigitalPort
	private lateinit var source: ConnectableView
	private var savedSpeed = 0

	@BeforeTest
	fun setup() {
		AntaresTestRule.configure()
		savedSpeed = SystemSpeed().speed
		time = ControlledTimeService()
		animator = AnimatorImpl(SystemSpeed(speed = 100), ControlledTimer(time))
		container = DrawableContainerImpl()
		drawingView = mock(MockMode.autofill)
		every { drawingView.animationContainer } returns container
		scheduler = mock(MockMode.autofill)
		actorListener = mock(MockMode.autofill)
		actorData = mock(MockMode.autofill)
		net = DigitalNet().apply { signalBuffer = DigitalSignalFactory.of(true) }
		netView = NetViewImpl(net)
		port = mock(MockMode.autofill)
		every { port.signalRepresentation } returns DigitalSignalRepresentation.HEXADECIMAL
		source = mock(MockMode.autofill)
		every { source.getPortView(port) } returns null
	}

	@AfterTest
	fun teardown() {
		animator.cancelAllTasks()
		animator.systemSpeed.speed = savedSpeed
		animator.dispose()
	}

	@Test
	fun shouldVisitCornersAndNotifySimulationOnlyAfterFinishing() {
		val edge = createEdge(Point2D(0, 0), Point2D(10, 0), Point2D(10, 20))
		edge.connectToOrigin(Connection(source, port))
		val task = createAnimation(edge).start()
		val view = task.target as DigitalEdgeAnimationView

		for (point in listOf(Point2D(0, 0), Point2D(10, 0), Point2D(10, 20))) {
			task.animate(50.0)
			assertEquals(point, view.location)
			verify(exactly(0)) { scheduler.actingDone(net, actorData) }
		}
		task.animate(50.0)

		verify(exactly(1)) { scheduler.actingDone(net, actorData) }
		assertEquals(0, animator.taskCount)
		assertTrue(container.drawables.isEmpty())
	}

	@Test
	fun shouldSplitIntoUnequalBranchesWithCachedLengthsAndRemainder() {
		val root = createEdge(Point2D(0, 0), Point2D(10, 0))
		val short = createEdge(Point2D(10, 0), Point2D(30, 0))
		// Deliberately store the longer branch in reverse direction.
		val long = createEdge(Point2D(10, 40), Point2D(10, 0))
		val node = mock<NodeView<DigitalSignal>>(MockMode.autofill)
		every { node.location } returns Point2D(10, 0)
		every { node.boundingBox } returns Rectangle2D(10, 0, 1, 1)
		every { node.getEdgeViews() } returns listOf(root, short, long)
		root.connectToOrigin(Connection(source, port))
		root.connectToDestination(Connection(node))
		short.connectToOrigin(Connection(node))
		long.connectToDestination(Connection(node))
		val lengths = listOf(root, short, long).map { edge ->
			CountingShape(edge.polyline).also { edge.polyline = it }
		}
		val task = createAnimation(root).start()
		assertEquals(50.0, task.size)
		assertEquals(300.0, task.duration)
		assertEquals(listOf(1, 1, 1), lengths.map { it.lengthReads })
		// Visit 0, 7, 10 and exhaust the root, handing a remainder of 7 to both branches.
		repeat(4) { task.animate(7.0) }
		val branchViews = container.drawables.filterIsInstance<DigitalEdgeAnimationView>()
		val shortView = branchViews.single { it.edgeView === short }
		val longView = branchViews.single { it.edgeView === long }
		val shortTask = animator.getTasksForTarget(shortView).single()
		val longTask = animator.getTasksForTarget(longView).single()
		assertEquals(20.0, shortTask.size)
		assertEquals(40.0, longTask.size)
		assertEquals(240.0, shortTask.duration)
		assertEquals(shortTask.duration, longTask.duration)
		assertTrue(longView.reverseDirection)
		assertFalse((task.target as DigitalEdgeAnimationView).drawSignalView)
		assertEquals(listOf(1, 1, 1), lengths.map { it.lengthReads })

		shortTask.animate(5.0)
		longTask.animate(5.0)
		assertEquals(Point2D(17, 0), shortView.location)
		assertEquals(Point2D(10, 7), longView.location)
		repeat(3) { shortTask.animate(50.0) }
		verify(exactly(0)) { scheduler.actingDone(net, actorData) }
		repeat(3) { longTask.animate(50.0) }
		verify(exactly(1)) { scheduler.actingDone(net, actorData) }
		assertTrue(container.drawables.isEmpty())
		assertEquals(0, animator.taskCount)

		// A later signal propagation gets a fresh geometry cache.
		long.polyline.setPointAt(0, 10.0, 80.0)
		assertEquals(90.0, createAnimation(root).start().size)
		assertEquals(listOf(2, 2, 2), lengths.map { it.lengthReads })
	}

	@Test
	fun shouldHonorSpeedChangesAndPauseDuringReverseTraversal() {
		val edge = createEdge(Point2D(100, 0), Point2D(0, 0))
		edge.connectToDestination(Connection(source, port))
		val task = createAnimation(edge).start()
		val view = task.target as DigitalEdgeAnimationView
		time.setTimeMillis(20)
		time.setTimeMillis(40)
		assertEquals(100.0 / 15.0, view.location.x, 1E-10)
		animator.systemSpeed.speed = 50
		time.setTimeMillis(60)
		assertEquals(200.0 / 15.0, view.location.x, 1E-10)
		animator.systemSpeed.pause()
		time.setTimeMillis(80)
		assertEquals(200.0 / 15.0, view.location.x, 1E-10)
		animator.systemSpeed.resume()
		time.setTimeMillis(100)
		assertEquals(250.0 / 15.0, view.location.x, 1E-10)
	}

	@Test
	fun shouldCancelWithoutNotifyingSimulationOrStartingBranches() {
		val edge = createEdge(Point2D(0, 0), Point2D(100, 0))
		val child = createEdge(Point2D(100, 0), Point2D(100, 50))
		val node = mock<NodeView<DigitalSignal>>(MockMode.autofill)
		every { node.location } returns Point2D(100, 0)
		every { node.boundingBox } returns Rectangle2D(100, 0, 1, 1)
		every { node.getEdgeViews() } returns listOf(edge, child)
		edge.connectToOrigin(Connection(source, port))
		edge.connectToDestination(Connection(node))
		child.connectToOrigin(Connection(node))
		val task = createAnimation(edge).start()
		task.animate(10.0)

		task.cancel()

		verify(exactly(0)) { scheduler.actingDone(net, actorData) }
		assertTrue(container.drawables.isEmpty())
		assertEquals(0, animator.taskCount)
	}

	private fun createEdge(vararg points: Point2D): DigitalEdgeView = DigitalEdgeView(net = net).apply {
		this@DigitalEdgeViewNetAnimationFlowTest.netView.add(this)
		points.forEach { addSegmentPoint(it) }
	}

	private fun createAnimation(edge: DigitalEdgeView) = DigitalEdgeViewNetAnimation(
		actorListener, actorData, edge, port, drawingView, animator, scheduler,
	)

	private class CountingShape(private val shape: PolylineShape) : PolylineShape by shape {
		var lengthReads = 0
		override val length: Double get() {
			lengthReads++
			return shape.length
		}
	}
}
