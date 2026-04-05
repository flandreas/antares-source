package io.antarescircuit.jabbah.execution.actor

import io.antarescircuit.jabbah.base.Tooltip
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.draw.container.DrawableBagInputEventHandler
import io.antarescircuit.jabbah.draw.container.DrawableContainerImpl
import io.antarescircuit.jabbah.execution.SignalHandler

open class ActorViewContainer<T: Drawable>(
	location: Point2D = Point2D.ZERO,
	useLocation: Boolean = false
) : DrawableContainerImpl<T>(location, useLocation), ActorView {

	private val handler = Handler()

	/** ---- [ActorView] interface */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler {
		handler.useFor(this)
		return handler
	}

	override fun <T: InputEventContext> getExecutionTooltip(context: T): Tooltip? {
		val p = context.location
		if (useLocation) {
			return getActorViewAt(p)?.getExecutionTooltip(context.withXY(p.subtract(this.location)))
		}
		return getActorViewAt(p)?.getExecutionTooltip(context)
	}

	override fun executionStarted(signalHandler: SignalHandler) {
		drawables.filterIsInstance<ActorView>().forEach { it.executionStarted(signalHandler) }
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		drawables.filterIsInstance<ActorView>().forEach { it.executionStopped(signalHandler) }
	}

	/** ---- [ActorViewContainer] */

	private fun getActorViewAt(pos: Point2D): ActorView? =
		getDrawableAt(pos.x, pos.y) { it is ActorView } as ActorView?

	private inner class Handler : DrawableBagInputEventHandler<T, ActorInteractionContext>() {

		override fun getDrawableAt(location: Point2D): Drawable? = getActorViewAt(location) as Drawable?

		override fun handlerOfDrawable(drawable: Drawable, context: ActorInteractionContext): InputEventHandler<ActorInteractionContext> {
			return if (drawable is ActorView) {
				drawable.getActorInteractionHandler(context)
			} else {
				super.handlerOfDrawable(drawable, context)
			}
		}
	}
}