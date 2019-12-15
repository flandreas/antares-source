package ch.scorpion.jabbah.edit.view

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.animation.*
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Ring2D
import ch.scorpion.jabbah.base.module.BaseModule
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

	companion object {

		/** The name of the [Color] property in [Properties] representing the fill color of this [AttentionDrawer].*/
		const val PROP_COLOR = "edit.view.AttentionDrawer.color"
	}

	fun drawAttentionTo(location: Point2D, view: DrawingView<*>, animator: Animator)
}

class AttentionDrawerImpl(
	private val properties: Properties = BaseModule.properties,
	private val color: Color = properties.get(AttentionDrawer.PROP_COLOR)
) : AttentionDrawer {

	companion object {

		/** The name of the [Float] property in [Properties] representing the animation duration (in ms).*/
		const val PROP_DURATION = "edit.view.AttentionDrawer.duration"

		/** The name of the [Float] property in [Properties] representing the maximum radius.*/
		const val PROP_MAX_RADIUS = "edit.view.AttentionDrawer.maxRadius"

		private val STROKE = Stroke()
	}

	override fun drawAttentionTo(location: Point2D, view: DrawingView<*>, animator: Animator) {
		val maxRadius = properties.getFloat(PROP_MAX_RADIUS).toDouble()
		val ring = GrowingRing(location, maxRadius, color)
		val animation = animator.schedule(
			target = ring,
			consumer = { ring.radius = it },
			sequence = DoubleRange(0.0, maxRadius, SequenceType.ONCE),
			duration = properties.getFloat(PROP_DURATION).toDouble()
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

	private inner class GrowingRing(
		center: Point2D,
		private val maxRadius: Double,
		private val color: Color
	) : AbstractRectangularUnzoomable(maxRadius, center) {

		override val lineWidth: Double get() = STROKE.width.toDouble()

		var radius: Double = 0.0
			set(value) {
				field = value
				super.halfSize = radius
				validate()
			}

		override fun draw(context: DrawContext) {
			context.g.stroke = STROKE
			context.g.color = color
			val rect = getViewRectangle()
			val thickness = if (radius < maxRadius * 0.75) {
				radius
			} else {
				maxRadius - radius
			}
			context.g.draw(Ring2D(rect.x, rect.y, rect.width, rect.height, thickness))
		}
	}
}