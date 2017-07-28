package ch.scorpion.jabbah.edit.view

import ch.scorpion.jabbah.animation.*
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Ring2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.drawable.AbstractRectangularUnzoomable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.edit.DrawingView

/**
 * [AttentionDrawer] produces a short graphical animation that draws the attention of the user
 * to a particular location in the [View].
 */
interface AttentionDrawer {
    fun drawAttentionTo(location: Point2D, view: DrawingView<*>, animator: Animator)
}

/**
 * An implementation of [AttentionDrawer] that draws a growing circle around the location.
 */
class AttentionDrawerImpl : AttentionDrawer {

    companion object {
        private val STROKE = Stroke()
        private val COLOR = Color.RED
        private val DURATION = 250.0
        private val MAX_RADIUS = 20.0
    }

    /** ---- [AttentionDrawer] interface */

    override fun drawAttentionTo(location: Point2D, view: DrawingView<*>, animator: Animator) {
        val circle = GrowingCircle(location)
        val circleAnimation = GrowingCircleAnimation(circle)

        val ring = GrowingRingHole(location)
        val ringAnimation = GrowingRingHoleAnimation(ring)
        ringAnimation.addListener(object : AnimationTaskAdapter() {
            override fun ended(task: AnimationTask) {
                view.ghostContainer.remove(ring)
                view.ghostContainer.validate()
            }
        })

        circle.zoomPan = view.zoomPan
        view.ghostContainer.add(circle)

        circleAnimation.addListener(object : AnimationTaskAdapter() {
            override fun ended(task: AnimationTask) {
                view.ghostContainer.remove(circle)
                view.ghostContainer.add(ring)
                ringAnimation.start()
            }
        })
        animator.schedule(circleAnimation)
        animator.schedule(ringAnimation)

        circleAnimation.start()
    }

    private inner class GrowingCircleAnimation(val circle: GrowingCircle) : AbstractAnimationTask<Double>(
            target = circle,
            consumer = { circle.radius = it},
            sequence = DoubleRange(0.0, MAX_RADIUS, SequenceType.ONCE),
            duration = DURATION
    )

    private inner class GrowingCircle(center: Point2D) : AbstractRectangularUnzoomable(0.0, center) {

        override val lineWidth: Double get() = STROKE.width.toDouble()

        var radius: Double = 0.0
            set(value) {
                field = value
                super.halfSize = radius
                validate()
            }

        override fun draw(context: DrawContext) {
            context.g.stroke = STROKE
            context.g.color = COLOR
            val rect = getViewRectangle()
            context.g.fillOval(rect.x.toInt(), rect.y.toInt(), rect.width.toInt(), rect.height.toInt())
        }
    }

    private inner class GrowingRingHoleAnimation(val ring: GrowingRingHole) : AbstractAnimationTask<Double>(
            target = ring,
            consumer = { ring.thickness = it},
            sequence = DoubleRange(MAX_RADIUS, 0.0, SequenceType.ONCE),
            duration = DURATION
    )

    private inner class GrowingRingHole(center: Point2D) : AbstractRectangularUnzoomable(MAX_RADIUS, center) {

        override val lineWidth: Double get() = STROKE.width.toDouble()

        var thickness: Double = MAX_RADIUS
            set(value) {
                invalidate()
                field = value
                invalidate()
                validate()
            }

        override fun draw(context: DrawContext) {
            context.g.stroke = STROKE
            context.g.color = COLOR
            val rect = getViewRectangle()
            context.g.draw(Ring2D(rect.x, rect.y, rect.width, rect.height, thickness))
        }
    }
}