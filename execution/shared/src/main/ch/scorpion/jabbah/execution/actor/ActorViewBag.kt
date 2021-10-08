package ch.scorpion.jabbah.execution.actor

import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.container.DrawableBagImpl
import ch.scorpion.jabbah.draw.container.DrawableBagInputEventHandler
import ch.scorpion.jabbah.execution.SignalHandler

open class ActorViewBag<T : Drawable>(
	location: Point2D = Point2D.ZERO,
	override val useLocation: Boolean = false
) : DrawableBagImpl<T>(location, useLocation), ActorView {

	private val handler = createHandler()

	/** ---- [ActorView] interface */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler {
		handler.useFor(this)
		return handler
	}

	override fun getExecutionTooltip(x: Double, y: Double): Tooltip? {
		val p = Point2D(x, y)
		if (useLocation) {
			return getActorViewAt(p)?.getExecutionTooltip(p.subtract(this.location))
		}
		return getActorViewAt(p)?.getExecutionTooltip(p)
	}

	override fun executionStarted(signalHandler: SignalHandler) { }

	override fun executionStopped(signalHandler: SignalHandler) { }

	/** ---- [ActorViewBag] */

	protected open fun createHandler(): Handler = Handler()

	protected open fun getActorViewAt(pos: Point2D): ActorView? =
		getDrawableAt(pos.x, pos.y) { it is ActorView } as ActorView?

	protected open inner class Handler : DrawableBagInputEventHandler<T, ActorInteractionContext>() {

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