package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.animation.*
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeed
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
import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.logger

/**
 * Organizes individual animations of bits flowing through a net of [DigitalEdgeView]s.
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
        val startEdgeView: DigitalEdgeView,
        val originPort: DigitalPort,
        val drawingView: DrawingView<GraphView<GraphElementView<*>>>,
        val animator: Animator = AnimationModule.animator,
        val scheduler: Scheduler = ExecutionModule.scheduler,
        val systemSpeed: SystemSpeed = BaseModule.systemSpeed,
        val styleProvider: StyleProvider = DrawStyleModule.styleProvider
) {

    companion object {
        // Note that the effective duration of an Animation already depends on [SystemSpeed] as implemented by [Animator].
        // Additionally, as a [DigitalEdgeViewNetAnimation] is only used for [SystemSpeedCategory.Use],
        // (which is defined below 33% of maximum [SystemSpeed]), the durations here represent 3 times the effective time.
        private val MIN_DURATION_MS = 50
        private val MAX_DURATION_MS = 300

        /** Returns 1 for maximum speed, 0 for halted.*/
        fun normalizedSpeed(speed: Int): Double {
            return Math.min(speed, SystemSpeedCategory.Explore.speedRange.last) / SystemSpeedCategory.Explore.speedRange.last.toDouble()
        }
    }

    private val LOG by logger(DigitalEdgeViewNetAnimation::class)

    private data class AnimationInfo(val animationTask: AnimationTask?, var startTime: Long, val remainingTime: Double)

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
        animationInfo.startTime = System.get().currentTimeMillis()
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
        val signalView = DigitalEdgeAnimationView(
            edgeView,
            startEdgeView.model!!.signalBuffer as DigitalSignal,
            originPort.signalRepresentation,
            isReverse,
            styleProvider
        )
        val predecessorInfo: AnimationInfo? = if (predecessor != null) predecessorMap[predecessor] else null
        val remainingTime: Double = Math.max(1.0, if (predecessorInfo == null) {
            totalEdgeViewNetAnimationTime()
        } else {
            predecessorInfo.remainingTime - normalizedSpeed(systemSpeed.speed) * (System.get().currentTimeMillis() - predecessorInfo.startTime)
        })
        val sequence: Sequence<Point2D> = if (isReverse) {
            EdgeViewPointSequence.reverseOf(edgeView)
        } else {
            EdgeViewPointSequence.of(edgeView)
        }

        val bitAnimationTask: AnimationTask = MoveLocatableAnimation(signalView, sequence, remainingTime)
        bitAnimationTask.addListener(animationSplitter)

        val animationInfo = AnimationInfo(
            animationTask = bitAnimationTask,
            startTime = System.get().currentTimeMillis(),
            remainingTime = remainingTime)

        if (isReverse) {
            signalView.location = edgeView.getSegmentPoint(edgeView.segmentPointCount - 1)
        } else {
            signalView.location = edgeView.getSegmentPoint(0)
        }

        drawingView.animationContainer.add(signalView)
        signalView.validate()

        predecessorMap.put(edgeView, animationInfo)
        animator.schedule(bitAnimationTask)
    }

    private fun totalEdgeViewNetAnimationTime(): Double {
        val normalizedSpeed = normalizedSpeed(systemSpeed.speed)
        if (normalizedSpeed == 0.0) {
            return MAX_DURATION_MS.toDouble()
        } else {
            return MIN_DURATION_MS + (MAX_DURATION_MS - MIN_DURATION_MS) / normalizedSpeed
        }
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
                scheduler.signalHandler.actingDone(startEdgeView.model!!)
                for (terminatedAnimationView in terminatedAnimationViews) {
                    drawingView.animationContainer.remove(terminatedAnimationView)
                }
            } else {
                predecessorMap.values.forEach {
                    it.startTime = System.get().currentTimeMillis()
                    it.animationTask!!.start()
                }
            }
        }
    }
}