package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.animation.AnimationTask
import ch.scorpion.jabbah.animation.AnimationTaskAdapter
import ch.scorpion.jabbah.animation.Animator
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentAnimation
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.actor.ActorListener
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.Vertice

/**
 * An animation of a signal flowing along an [EdgeView].
 */
interface EdgeViewNetAnimation {
	fun start(): AnimationTask
}

/**
 * Creates animations to be played by [GraphViewExecutionAnimator] during the execution
 * of a [GraphView]'s [Graph].
 */
interface GraphViewExecutionAnimationFactory {

	companion object {
		const val VERTICE_VIEW_ACTING_KEY = "VERTICE_VIEW_ACTING"
	}

	/** Creates an [EdgeViewNetAnimation] to visualize a signal flowing along an [EdgeView]. */
	fun createEdgeViewNetAnimation(
		actorListener: ActorListener,
		actorData: ActorData,
		startEdgeView: EdgeView<*>,
		startPort: Port<*>,
		drawingView: DrawingView<GraphView>,
		scheduler: Scheduler,
		animator: Animator = AnimationModule.animator,
		styleProvider: StyleProvider = DrawStyleModule.styleProvider
	): EdgeViewNetAnimation

	/**
	 * Creates an [AnimationTask] to be played while a [VerticeView]'s [Vertice] is acting.
	 * @return `null` if [verticeView] can't be animated
	 */
	fun createVerticeViewActingAnimation(verticeView: VerticeView<*>, key: String? = VERTICE_VIEW_ACTING_KEY): AnimationTask?
}

class UndefinedGraphViewExecutionAnimationFactory : GraphViewExecutionAnimationFactory {

	override fun createEdgeViewNetAnimation(actorListener: ActorListener, actorData: ActorData, startEdgeView: EdgeView<*>, startPort: Port<*>, drawingView: DrawingView<GraphView>, scheduler: Scheduler, animator: Animator, styleProvider: StyleProvider): EdgeViewNetAnimation {
		throw UnsupportedOperationException("not implemented")
	}

	override fun createVerticeViewActingAnimation(verticeView: VerticeView<*>, key: String?): AnimationTask? {
		throw UnsupportedOperationException("not implemented")
	}
}

/**
 * Provides default implementations for the various animations.
 */
abstract class AbstractGraphViewExecutionAnimationFactory : GraphViewExecutionAnimationFactory {

	/**
	 * Creates a [TransparentAnimation] that produces a "glow" effect.
	 * @return `null` if [verticeView] is not [Transparent]
	 */
	override fun createVerticeViewActingAnimation(verticeView: VerticeView<*>, key: String?): AnimationTask? {
		if (verticeView is Transparent) {
			val glowAnimation = TransparentAnimation.glow(verticeView, key = key)
			glowAnimation.addListener(object : AnimationTaskAdapter() {
				override fun ended(task: AnimationTask) {
					verticeView.transparency = Transparent.FULLY_OPAQUE
					verticeView.validate()
				}
			})
			return glowAnimation
		}
		return null
	}
}
