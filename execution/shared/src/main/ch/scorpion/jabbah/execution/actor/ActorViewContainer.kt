package ch.scorpion.jabbah.execution.actor

import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.container.DrawableContainerImpl

open class ActorViewContainer<T: Drawable>(
	location: Point2D = Point2D.ZERO,
	useLocation: Boolean = false
) : DrawableContainerImpl<T>(location, useLocation), ActorView {

	private val handler = Handler()

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler? {
		return handler
	}

	override fun getExecutionTooltip(x: Double, y: Double): Tooltip? {
		val p = Point2D(x, y)
		if (useLocation) {
			return getActorViewAt(p)?.getExecutionTooltip(p.subtract(this.location))
		}
		return getActorViewAt(p)?.getExecutionTooltip(p)
	}

	private fun getActorViewAt(pos: Point2D): ActorView? {
		return getDrawableAt(pos.x, pos.y) { it is ActorView } as ActorView?
	}

	private inner class Handler : InputEventHandlerAdapter<ActorInteractionContext>() {

		override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? {
			val actorView = getActorViewAt(context.location)
			if (actorView != null) {
				val localContext = localContext(context)
				return actorView.getActorInteractionHandler(localContext)?.mouseMoved(localContext)
			}
			return null
		}

		override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? {
			val actorView = getActorViewAt(context.location)
			if (actorView != null) {
				val localContext = localContext(context)
				return actorView.getActorInteractionHandler(localContext)?.mousePressed(localContext)
			}
			return null
		}

		override fun mouseReleased(context: ActorInteractionContext): ActorInteractionHandler? {
			val actorView = getActorViewAt(context.location)
			if (actorView != null) {
				val localContext = localContext(context)
				return actorView.getActorInteractionHandler(localContext)?.mouseReleased(localContext)
			}
			return null
		}

		override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler? {
			val actorView = getActorViewAt(context.location)
			if (actorView != null) {
				val localContext = localContext(context)
				return actorView.getActorInteractionHandler(localContext)?.mouseClicked(localContext)
			}
			return null
		}

		private fun localContext(c: ActorInteractionContext): ActorInteractionContext {
			return if (location == Point2D.ZERO) c else c.withXY(c.location.subtract(location)) as ActorInteractionContext
		}
	}
}