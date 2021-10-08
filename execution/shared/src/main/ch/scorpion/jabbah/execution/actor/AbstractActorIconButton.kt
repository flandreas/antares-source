package ch.scorpion.jabbah.execution.actor

import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.drawable.AbstractIconButton
import ch.scorpion.jabbah.draw.graphics.Icon
import ch.scorpion.jabbah.execution.SignalHandler

/**
 * Base class for implementing buttons whose [handleClicked] method is called when the user clicks
 * the icon during execution.
 */
abstract class AbstractActorIconButton(
	icon: Icon,
	location: Point2D = Point2D.ZERO,
	tooltipKey: String? = null
) : AbstractIconButton(icon, location, tooltipKey), ActorView {

	private val actorInteractionHandler = createActorInteractionHandler()

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler = actorInteractionHandler

	override fun getExecutionTooltip(x: Double, y: Double): Tooltip? = getTooltip(x, y)

	override fun executionStarted(signalHandler: SignalHandler) { }

	override fun executionStopped(signalHandler: SignalHandler) { }

	protected abstract fun handleClicked(context: ActorInteractionContext)

	protected open fun createActorInteractionHandler(): InputEventHandlerAdapter<ActorInteractionContext> = Handler()

	protected open inner class Handler : InputEventHandlerAdapter<ActorInteractionContext>() {

		override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? =
			if (keepMouseMoved(context.location)) this else null

		override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler? {
			if (enabled) {
				isHovering = false
				handleClicked(context)
			}
			return null
		}
	}
}