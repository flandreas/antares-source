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

class AttentionDrawerImpl() : AttentionDrawer {

    companion object {
        private val STROKE = Stroke()
        private val COLOR = Color.RED
        private val DURATION = 500.0
        private val MAX_RADIUS = 30.0
    }

    override fun drawAttentionTo(location: Point2D, view: DrawingView<*>, animator: Animator) {
        val ring = GrowingRing(location)
        val animation = animator.schedule(
            target = ring,
            consumer = { ring.radius = it },
            sequence = DoubleRange(0.0, MAX_RADIUS, SequenceType.ONCE),
            duration = DURATION
        )
        animation.addListener(object : AnimationTaskAdapter() {
            override fun ended(task: AnimationTask) {
                view.ghostContainer.remove(ring)
                view.ghostContainer.validate()
            }
        })
        view.ghostContainer.add(ring)
        animation.start()
    }

    private inner class GrowingRing(center: Point2D) : AbstractRectangularUnzoomable(MAX_RADIUS, center) {

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
            val thickness = if (radius < MAX_RADIUS * 0.75) {
                radius
            } else {
                MAX_RADIUS - radius
            }
            context.g.draw(Ring2D(rect.x, rect.y, rect.width, rect.height, thickness))
        }
    }
}