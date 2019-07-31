package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.animation.*
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.drawable.MoveLocatableAnimation
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewPointSequence
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.actor.ActorListener

/**
 * Organizes consecutive animations of bits flowing through a net of [DigitalEdgeView]s.
 *
 * Creates an [AnimationTask] that drives a [DigitalEdgeAnimationView] along the specified [DigitalEdgeView].
 * The animation is not started right away. Start the animation by calling [start], which returns the created
 * [AnimationTask] to allow the client to control the animation, such as stopping it.
 *
 * This [DigitalEdgeViewNetAnimation] is able to orchestrate [DigitalEdgeView]s that are split at a [DigitalNodeView].
 * When the [DigitalEdgeAnimationView] runs into a [DigitalNodeView], the animation is split into an new, individual
 * animation for every outgoing [DigitalEdgeView]. The speed of these individual animations is controlled so that
 * all animations end at the same time, even if some of them have to travel along a shorter path than others.
 * Therefore, animations on longer paths travel faster, while animations on shorter path travel slower.
 */
class DigitalEdgeViewNetAnimation(
	private val actorListener: ActorListener,
	private val actorData: ActorData,
	val startEdgeView: DigitalEdgeView,
	val originPort: DigitalPort,
	val drawingView: DrawingView<GraphView<GraphElementView<*>>>,
	val animator: Animator = AnimationModule.animator,
	val scheduler: Scheduler = ExecutionModule.scheduler,
	val styleProvider: StyleProvider = DrawStyleModule.styleProvider
) {

	companion object {

		private val LOG by logger(DigitalEdgeViewNetAnimation::class)

		// Note that the effective duration of an Animation already depends on [SystemSpeed] as implemented by [Animator].
		// Additionally, as a [DigitalEdgeViewNetAnimation] is only used for [SystemSpeedCategory.Use],
		// (which is defined below 33% of maximum [SystemSpeed]), the duration here represents 3 times the effective time.
		private const val DURATION_MS = 300.0

		/** Returns 1 for maximum speed, 0 for halted.*/
		fun normalizedSpeed(speed: Int): Double {
			return Math.min(speed, SystemSpeedCategory.Explore.speedRange.last) / SystemSpeedCategory.Explore.speedRange.last.toDouble()
		}
	}

	/**
	 * Contains management information associated with every [AnimationTask].
	 * @property animationTask the [AnimationTask] the information belongs to
	 * @property overallLength the overall length of the entire net to travers
	 * @property remainingLength the length of the remaining net to travers
	 * @property visitedLength the added length of all visited [DigitalEdgeView]s
	 */
	private data class AnimationInfo(
		val animationTask: AnimationTask?,
		val overallLength: Double,
		val remainingLength: Double,
		val visitedLength: Double)

	/** Maps a [DigitalEdgeView] to the [AnimationInfo] of its predecessor [DigitalEdgeView]. */
	private val predecessorMap = mutableMapOf<DigitalEdgeView, AnimationInfo>()

	private val terminatedAnimationViews = mutableListOf<DigitalEdgeAnimationView>()

	private val animationSplitter = AnimationSplitter()

	init {
		setupEdgeAnimation(null, startEdgeView, startEdgeView.getConnectableView(originPort)!!)
	}

	/**
	 * TODO What is the purpose of returning an [AnimationTask]? This task cannot be used for stopping the
	 * animation, because if the animation has been spit at a [DigitalNodeView], there are multiple additional tasks.
	 * It would be better to return nothing and to provide a stop() method that stops all running tasks.
	 */
	fun start(): AnimationTask {
		val animationInfo = predecessorMap[startEdgeView]!!
		animationInfo.animationTask!!.start()
		return animationInfo.animationTask
	}

	/**
	 * Creates a new animation for every outgoing [DigitalEdgeView] of the specified [NodeView], and registers
	 * them in [predecessorMap].
	 */
	private fun processNode(predecessor: DigitalEdgeView, nodeView: NodeView<*>) {
		nodeView.getEdgeViews()
			.filter { it != predecessor }
			.map { it as DigitalEdgeView }
			.forEach { setupEdgeAnimation(predecessor, it, nodeView) }
	}

	private fun setupEdgeAnimation(predecessor: DigitalEdgeView?, edgeView: DigitalEdgeView, originConnectable: ConnectableView) {
		LOG.trace("Setup EdgeView animation for output of ${originConnectable::class.simpleName}")

		val isReverse = originConnectable === edgeView.destination
		val animationView = DigitalEdgeAnimationView(
			edgeView,
			startEdgeView.model!!.signalBuffer as DigitalSignal,
			originPort.signalRepresentation,
			isReverse,
			styleProvider
		)
		val predecessorInfo: AnimationInfo? = if (predecessor != null) predecessorMap[predecessor] else null

		val sequence: Sequence<Point2D> = if (isReverse) {
			EdgeViewPointSequence.reverseOf(edgeView)
		} else {
			EdgeViewPointSequence.of(edgeView)
		}

		val overallLength: Double = predecessorInfo?.overallLength ?: sequence.size
		val oldVisitedLength = predecessorInfo?.visitedLength ?: 0.0
		val remainingTime = (overallLength - oldVisitedLength) / overallLength * DURATION_MS

		val bitAnimationTask: AnimationTask = MoveLocatableAnimation(animationView, sequence, remainingTime)
		bitAnimationTask.addListener(animationSplitter)

		val animationInfo = AnimationInfo(
			animationTask = bitAnimationTask,
			overallLength = overallLength,
			remainingLength = sequence.size,
			visitedLength = oldVisitedLength + edgeView.polyline.length
		)

		if (isReverse) {
			animationView.location = edgeView.getSegmentPoint(edgeView.segmentPointCount - 1)
		} else {
			animationView.location = edgeView.getSegmentPoint(0)
		}

		drawingView.animationContainer.add(animationView)
		animationView.validate()

		predecessorMap[edgeView] = animationInfo
		animator.schedule(bitAnimationTask)
	}

	/**
	 * Splits an animation at a [NodeView] by removing the animation of the incoming [DigitalEdgeView]
	 * and starting a new animation for every outgoing [DigitalEdgeView].
	 */
	private inner class AnimationSplitter : AnimationTaskAdapter() {

		override fun ended(task: AnimationTask) {
			task.removeListener(this)
			val animationView = task.target as DigitalEdgeAnimationView

			if (animationView.reverseDirection) {
				if (animationView.edgeView.origin is NodeView<*>) {
					processNode(animationView.edgeView, animationView.edgeView.origin as NodeView<*>)
				}
			} else {
				if (animationView.edgeView.destination is NodeView<*>) {
					processNode(animationView.edgeView, animationView.edgeView.destination as NodeView<*>)
				}
			}

			terminatedAnimationViews.add(animationView)
			animationView.drawSignalView = false
			predecessorMap.remove(animationView.edgeView)

			if (predecessorMap.isEmpty()) {
				startEdgeView.model!!.actingVisualized(scheduler, actorListener, actorData)
				for (terminatedAnimationView in terminatedAnimationViews) {
					drawingView.animationContainer.remove(terminatedAnimationView)
				}
			} else {
				predecessorMap.values.forEach {
					it.animationTask!!.start()
				}
			}
		}
	}
}