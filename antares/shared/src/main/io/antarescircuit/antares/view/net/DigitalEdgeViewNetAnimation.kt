package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.animation.AnimationTask
import io.antarescircuit.jabbah.animation.AnimationTaskAdapter
import io.antarescircuit.jabbah.animation.Animator
import io.antarescircuit.jabbah.draw.drawable.MoveLocatableAnimation
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.execution.actor.ActorData
import io.antarescircuit.jabbah.execution.actor.ActorListener
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.execution.speed.SystemSpeedCategory
import io.antarescircuit.jabbah.graph.view.ConnectableView
import io.antarescircuit.jabbah.graph.view.EdgeViewNetAnimation
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewEndpointType
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewPointSequence
import io.antarescircuit.jabbah.graph.view.net.node.NodeView
import kotlin.math.min

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
 * Therefore, animations on longer paths travel faster, while those on shorter paths travel slower.
 */
class DigitalEdgeViewNetAnimation(
	private val actorListener: ActorListener,
	private val actorData: ActorData,
	val startEdgeView: DigitalEdgeView,
	val startPort: DigitalPort,
	val drawingView: DrawingView<GraphElementView<*>, GraphView>,
	val animator: Animator,
	val scheduler: Scheduler,
	val styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : EdgeViewNetAnimation {

	companion object {

		// Note that the effective duration of an Animation already depends on [SystemSpeed] as implemented by [Animator].
		// Additionally, as a [DigitalEdgeViewNetAnimation] is only used for [SystemSpeedCategory.Use],
		// (which is defined below 33% of maximum [SystemSpeed]), the duration here represents 3 times the effective time.
		private const val DURATION_MS = 300.0

		/** Returns 1 for maximum speed, 0 for halted.*/
		fun normalizedSpeed(speed: Int): Double {
			return min(speed, SystemSpeedCategory.Explore.speedRange.last) / SystemSpeedCategory.Explore.speedRange.last.toDouble()
		}
	}

	/**
	 * Contains management information associated with every [AnimationTask].
	 * @property animationTask the [AnimationTask] the information belongs to
	 * @property overallLength the overall length of the entire net to travers
	 * @property visitedLength the added length of all visited [DigitalEdgeView]s
	 */
	private data class AnimationInfo(
		val edgeViewPointSequence: EdgeViewPointSequence,
		val animationTask: AnimationTask?,
		val overallLength: Double,
		val visitedLength: Double)

	/** Maps a [DigitalEdgeView] to the [AnimationInfo] of its predecessor [DigitalEdgeView]. */
	private val predecessorMap = mutableMapOf<DigitalEdgeView, AnimationInfo>()

	private val terminatedAnimationViews = mutableListOf<DigitalEdgeAnimationView>()

	private val animationSplitter = AnimationSplitter()

	init {
		setupEdgeAnimation(null, startEdgeView, startEdgeView.getConnection(startPort)!!.connectableView)
	}

	/**
	 * Starts the animation.
	 * The returned [AnimationTask] can be used to register as listener for when the [AnimationTask] has ended.
	 */
	override fun start(): AnimationTask {
		val animationInfo = predecessorMap[startEdgeView]!!
		animationInfo.animationTask!!.start()
		return animationInfo.animationTask
	}

	override fun stop() {
		predecessorMap[startEdgeView]?.animationTask?.stop()
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

	private fun setupEdgeAnimation(
		predecessor: DigitalEdgeView?,
		edgeView: DigitalEdgeView,
		startConnectable: ConnectableView
	) {
		val isReverse = edgeView.getConnectionEndpointType(startConnectable) != EdgeViewEndpointType.ORIGIN
		val animationView = DigitalEdgeAnimationView(
			edgeView,
			startEdgeView.model.signalBuffer as DigitalSignal,
			startPort.signalRepresentation,
			isReverse,
			styleProvider
		)
		animationView.location = if (isReverse) edgeView.polyline.getLastPoint() else edgeView.polyline.getFirstPoint()

		val predecessorInfo: AnimationInfo? = if (predecessor != null) predecessorMap[predecessor] else null

		val offset: Double = predecessorInfo?.edgeViewPointSequence?.remainder ?: 0.0
		val sequence = EdgeViewPointSequence(edgeView, isReverse, true, offset)

		val overallLength: Double = predecessorInfo?.overallLength ?: sequence.size
		val oldVisitedLength = predecessorInfo?.visitedLength ?: 0.0
		val remainingTime = (overallLength - oldVisitedLength) / overallLength * DURATION_MS

		val bitAnimationTask: AnimationTask = MoveLocatableAnimation(animationView, sequence, remainingTime, isPausable = true)
		bitAnimationTask.addListener(animationSplitter)

		val animationInfo = AnimationInfo(
			edgeViewPointSequence = sequence,
			animationTask = bitAnimationTask,
			overallLength = overallLength,
			visitedLength = oldVisitedLength + edgeView.polyline.length
		)

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

		override fun ended(task: AnimationTask, canceled: Boolean) {
			task.removeListener(this)
			val animationView = task.target as DigitalEdgeAnimationView

			if (canceled) {
				drawingView.animationContainer.remove(animationView)
				return
			}

			if (animationView.reverseDirection) {
				if (animationView.edgeView.origin?.connectableView is NodeView<*>) {
					processNode(animationView.edgeView, animationView.edgeView.origin!!.connectableView as NodeView<*>)
				}
			} else {
				if (animationView.edgeView.destination?.connectableView is NodeView<*>) {
					processNode(animationView.edgeView, animationView.edgeView.destination!!.connectableView as NodeView<*>)
				}
			}

			terminatedAnimationViews.add(animationView)
			animationView.drawSignalView = false
			predecessorMap.remove(animationView.edgeView)

			if (predecessorMap.isEmpty()) {
				startEdgeView.model.actingVisualized(scheduler, actorListener, actorData)
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