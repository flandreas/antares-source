package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.animation.AnimationTask
import ch.scorpion.jabbah.animation.AnimationTaskAdapter
import ch.scorpion.jabbah.animation.Animator
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.ZoomPanAnimation
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Manages the various animations used for animating the descend into a [SubGraphVerticeView] when using the pan
 * functions of a [View].
 */
class DescendAnimationManager(val animator: Animator = AnimationModule.animator) {

    companion object {
        private const val ZOOM_DURATION = 1_000.0
        private const val OUTER_END_ZOOM_FACTOR = 16.0
        private const val INNER_START_ZOOM_FACTOR = 0.3
        private const val INNER_END_ZOOM_FACTOR = 1.0
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

    private fun createInnerDescendAnimation(drawingView: DrawingView<*>, terminator: () -> Unit): ZoomPanAnimation {
        val animation = ZoomPanAnimation(
            drawingView,
            INNER_END_ZOOM_FACTOR,
	        drawingView.drawing.boundingBox.center,
	        ZOOM_DURATION
        )
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
    ): ZoomPanAnimation {
        val animation = ZoomPanAnimation(
            drawingView,
            OUTER_END_ZOOM_FACTOR,
	        subGraphVerticeView.boundingBox.center,
	        ZOOM_DURATION
        )

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
	 * the outer animation is started. This is typically code that exchanges the [DrawingView] in a [View]
	 * @param terminator the code to be executed when the overall animation has ended
	 */
	fun ascendFrom(
		drawingView: DrawingView<*>,
		subGraphVerticeView: SubGraphVerticeView<*>,
		endZoomFactor: Double,
		ascender: (SubGraphVerticeView<*>) -> Unit,
		terminator: () -> Unit
	) {
		animator
			.schedule(createInnerAscendAnimation(drawingView, subGraphVerticeView, endZoomFactor, ascender, terminator))
			.start()
	}

	private fun createInnerAscendAnimation(
		drawingView: DrawingView<*>,
		subGraphVerticeView: SubGraphVerticeView<*>,
		endZoomFactor: Double,
		ascender: (SubGraphVerticeView<*>) -> Unit,
		terminator: () -> Unit
	): ZoomPanAnimation {
		val animation = ZoomPanAnimation(
			drawingView,
			INNER_START_ZOOM_FACTOR,
			drawingView.drawing.boundingBox.center,
			ZOOM_DURATION
		)
		animation.addListener(object: AnimationTaskAdapter() {
			override fun ended(task: AnimationTask) {
				ascender.invoke(subGraphVerticeView)
				drawingView.navigator.panCenter(OUTER_END_ZOOM_FACTOR)
				val outerAnimation = createOuterAscendAnimation(drawingView, subGraphVerticeView, endZoomFactor, terminator)
				animator.schedule(outerAnimation)
				outerAnimation.start()

			}
		})
		return animation
	}

	private fun createOuterAscendAnimation(
		drawingView: DrawingView<*>,
		subGraphVerticeView: SubGraphVerticeView<*>,
		endZoomFactor: Double,
		terminator: () -> Unit
	): ZoomPanAnimation {
		val animation = ZoomPanAnimation(
			drawingView,
			endZoomFactor,
			drawingView.drawing.boundingBox.center,
			ZOOM_DURATION)

		animation.addListener(object: AnimationTaskAdapter() {
			override fun ended(task: AnimationTask) {
				terminator.invoke()
			}
		})
		return animation
	}
}