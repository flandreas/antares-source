package io.antarescircuit.jabbah.edit.view

import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.animation.*
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Ring2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.DrawableContainer
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.drawable.AbstractRectangularUnzoomable
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.edit.DrawingView

/**
 * [AttentionDrawer] produces a short graphical animation that draws the attention of the user
 * to a particular location in the [View].
 */
interface AttentionDrawer {

	companion object {

		/** The name of the [Color] property in [Properties] representing the fill color of this [AttentionDrawer].*/
		const val PROP_COLOR = "edit.view.AttentionDrawer.color"
	}

	fun drawAttentionTo(location: Point2D, view: DrawingView<*,*>, animator: Animator)
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

	override fun drawAttentionTo(location: Point2D, view: DrawingView<*,*>, animator: Animator) {
		val maxRadius = properties.getFloat(PROP_MAX_RADIUS).toDouble()
		val ring = GrowingRing(location, maxRadius, color)
		val animation = animator.schedule(
			target = ring,
			consumer = { ring.radius = it },
			sequence = DoubleRange(0.0, maxRadius),
			duration = properties.getFloat(PROP_DURATION).toDouble()
		)
		animation.addListener(object : AnimationTaskAdapter() {
			override fun ended(task: AnimationTask, canceled: Boolean) {
				container(view).remove(ring)
				container(view).validate()
			}
		})
		container(view).add(ring)
		animation.start()
	}

	private fun container(view: DrawingView<*,*>): DrawableContainer<Drawable> {
		return view.animationContainer
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