package ch.scorpion.jabbah.graph.view.ui

import ch.scorpion.jabbah.animation.*
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.ZoomPan
import ch.scorpion.jabbah.draw.view.ZoomPanRange
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Manages the various animations used for animating the descend into a [SubGraphVerticeView] when using the pan
 * functions of a [View].
 */
class DescendAnimationManager(val animator: Animator) {

    @Suppress("unused") constructor(): this(AnimationModule.animator)

    companion object {
        private val ZOOM_DURATION = 1_000.0
        private val OUTER_END_ZOOM_FACTOR = 16.0
        private val INNER_START_ZOOM_FACTOR = 0.3
        private val INNER_END_ZOOM_FACTOR = 1.0
    }

    /**
     * Starts an asynchronous animation that descends into the specified [SubGraphVerticeView].
     * @param diver the code to be executed when the outer animation has finished and before
     * the inner animation is started. This is typically code that exchanges the [DrawingView] in a [View]
     */
    fun descendInto(drawingView: DrawingView<*>,
        subGraphVerticeView: SubGraphVerticeView<*>,
        diver: (SubGraphVerticeView<*>) -> Unit,
        ender: () -> Unit
    ) {
        animator
            .schedule(createOuterAnimation(drawingView, subGraphVerticeView, diver, ender))
            .start()
    }

    private fun createInnerAnimation(drawingView: DrawingView<*>, ender: () -> Unit): ZoomPanAnimation {
        val animation = ZoomPanAnimation(
            drawingView,
            INNER_END_ZOOM_FACTOR,
            Point2D(drawingView.drawing.boundingBox.centerX, drawingView.drawing.boundingBox.centerY)
        )
        animation.addListener(object: AnimationTaskAdapter() {
            override fun ended(task: AnimationTask) {
                ender.invoke()
            }
        })
        return animation
    }

    private fun createOuterAnimation(
        drawingView: DrawingView<*>,
        subGraphVerticeView: SubGraphVerticeView<*>,
        diver: (SubGraphVerticeView<*>) -> Unit,
        ender: () -> Unit): ZoomPanAnimation
    {
        val animation = ZoomPanAnimation(
            drawingView,
            OUTER_END_ZOOM_FACTOR,
            Point2D(subGraphVerticeView.boundingBox.centerX, subGraphVerticeView.boundingBox.centerY)
        )

        /** Starts the inner animation after the outer animation has ended. */
        animation.addListener(object: AnimationTaskAdapter() {
            override fun ended(task: AnimationTask) {
                diver.invoke(subGraphVerticeView)
                drawingView.navigator.panCenter(INNER_START_ZOOM_FACTOR)
                val innerAnimation = createInnerAnimation(drawingView, ender)
                animator.schedule(innerAnimation)
                innerAnimation.start()
            }
        })

        return animation
    }

    private class ZoomPanAnimation(
        view: View<*>,
        endZoomFactor: Double,
        toBeCentered: Point2D
    ) : AbstractAnimationTask<ZoomPan>(
            view,
            { view.navigator.setZoomPan(it) },
            ZoomPanRange(view, endZoomFactor, toBeCentered),
            ZOOM_DURATION
    )
}