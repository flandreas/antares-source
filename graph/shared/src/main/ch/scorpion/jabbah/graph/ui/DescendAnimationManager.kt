package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.animation.*
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.ZoomPan
import ch.scorpion.jabbah.draw.view.ZoomedPointTranslation
import ch.scorpion.jabbah.draw.view.ZoomedPointVoyageAnimation
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Manages the various animations used for animating the descend into a [SubGraphVerticeView] when using the pan
 * functions of a [View].
 */
class DescendAnimationManager(
	val animator: Animator = AnimationModule.constantSpeedAnimator
) {

    companion object {
        private const val ZOOM_DURATION = 700.0
        private const val OUTER_END_ZOOM_FACTOR = 16.0
        private const val INNER_START_ZOOM_FACTOR = 0.3
    }

    /**
     * Starts an asynchronous animation that descends into the specified [SubGraphVerticeView].
     *
     * @param drawingView the [DrawingView] in which the animations take place
     * @param subGraphVerticeView the [SubGraphVerticeView] to descend into
     * @param descender the code to be executed when the outer animation has finished and before
     * the inner animation is started. This is typically code that exchanges the [DrawingView] in a [View]
     * @param terminator the code to be executed when the overall animation has ended
     */
    fun descendInto(
	    drawingView: DrawingView<*>,
        subGraphVerticeView: SubGraphVerticeView<*>,
        descender: (SubGraphVerticeView<*>) -> Unit,
        terminator: () -> Unit
    ) {
        animator
            .schedule(createOuterDescendAnimation(drawingView, subGraphVerticeView, descender, terminator))
            .start()
    }

    private fun createInnerDescendAnimation(drawingView: DrawingView<*>, terminator: () -> Unit): AnimationTask {
	    val animation = ZoomedPointVoyageAnimation(
		    drawingView,
		    ZOOM_DURATION,
		    ZoomedPointTranslation(drawingView.drawing.boundingBox.center, drawingView.center, drawingView.navigator.calculateFixMaxNormalZoomFactor()))

        animation.addListener(object: AnimationTaskAdapter() {
            override fun ended(task: AnimationTask) {
                terminator.invoke()
            }
        })
        return animation
    }

    private fun createOuterDescendAnimation(
        drawingView: DrawingView<*>,
        subGraphVerticeView: SubGraphVerticeView<*>,
        descender: (SubGraphVerticeView<*>) -> Unit,
        terminator: () -> Unit
    ): AnimationTask {
	    val animation = ZoomedPointVoyageAnimation(
		    drawingView,
		    ZOOM_DURATION,
		    ZoomedPointTranslation(subGraphVerticeView.boundingBox.center, drawingView.center, OUTER_END_ZOOM_FACTOR))

        /** Starts the inner animation after the outer animation has ended. */
        animation.addListener(object: AnimationTaskAdapter() {
            override fun ended(task: AnimationTask) {
	            descender.invoke(subGraphVerticeView)
                drawingView.navigator.panCenter(INNER_START_ZOOM_FACTOR)
                val innerAnimation = createInnerDescendAnimation(drawingView, terminator)
                animator.schedule(innerAnimation)
                innerAnimation.start()
            }
        })

        return animation
    }

	/**
	 * Starts an asynchronous animation that ascends from the specified [SubGraphVerticeView].
	 *
	 * @param drawingView the [DrawingView] in which the animations take place
	 * @param subGraphVerticeView the [SubGraphVerticeView] to ascend from
	 * @param ascender the code to be executed when the inner animation has finished and before
	 * the outer animation is started. This is typically code that exchanges the [DrawingView] in a [View].
	 * Returns the destination [ZoomPan] at the end of the outer animation
	 * @param terminator the code to be executed when the overall animation has ended
	 */
	fun ascendFrom(
		drawingView: DrawingView<*>,
		subGraphVerticeView: SubGraphVerticeView<*>,
		ascender: (SubGraphVerticeView<*>) -> ZoomedPointTranslation,
		terminator: () -> Unit
	) {
		animator
			.schedule(createInnerAscendAnimation(drawingView, subGraphVerticeView, ascender, terminator))
			.start()
	}

	private fun createInnerAscendAnimation(
		drawingView: DrawingView<*>,
		subGraphVerticeView: SubGraphVerticeView<*>,
		ascender: (SubGraphVerticeView<*>) -> ZoomedPointTranslation,
		terminator: () -> Unit
	): AnimationTask {
		val animation = ZoomedPointVoyageAnimation(
			drawingView,
			ZOOM_DURATION,
			ZoomedPointTranslation(drawingView.drawing.boundingBox.center, drawingView.center, INNER_START_ZOOM_FACTOR))
		animation.addListener(object: AnimationTaskAdapter() {
			override fun ended(task: AnimationTask) {
				val destination = ascender.invoke(subGraphVerticeView)
				drawingView.navigator.panCenter(OUTER_END_ZOOM_FACTOR)
				val outerAnimation = createOuterAscendAnimation(drawingView, destination, terminator)
				animator.schedule(outerAnimation)
				outerAnimation.start()
			}
		})
		return animation
	}

	private fun createOuterAscendAnimation(
		drawingView: DrawingView<*>,
		destination: ZoomedPointTranslation,
		terminator: () -> Unit
	): AnimationTask {
		val animation = ZoomedPointVoyageAnimation(
			drawingView,
			ZOOM_DURATION,
			destination)

		animation.addListener(object: AnimationTaskAdapter() {
			override fun ended(task: AnimationTask) {
				terminator.invoke()
			}
		})
		return animation
	}
}