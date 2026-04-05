package io.antarescircuit.jabbah.execution.actor

import io.antarescircuit.jabbah.base.Tooltip
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.InputEventHandlerAdapter
import io.antarescircuit.jabbah.draw.drawable.ButtonAction
import io.antarescircuit.jabbah.draw.drawable.DrawableButton
import io.antarescircuit.jabbah.draw.drawable.DrawableButtonRenderer
import io.antarescircuit.jabbah.draw.style.*

/**
 * Base class for implementing buttons whose [actorAction] method is called when the user clicks
 * the icon during execution.
 */
open class ActorDrawableButton<C: InputEventContext>(
	location: Point2D = Point2D.ZERO,
	tooltipKey: String? = null,
	stylable: Stylable,
	private val actorAction: ButtonAction<ActorInteractionContext>,
	renderer: DrawableButtonRenderer,
	round: Boolean = false
) : DrawableButton<C>(location, tooltipKey, stylable, {}, renderer, round = round), ActorView {

	constructor(
		location: Point2D,
		tooltipKey: String? = null,
		styleType: StyleType = StyleType.ANNOTATION,
		styleProvider: StyleProvider = DrawStyleModule.styleProvider,
		actorAction: ButtonAction<ActorInteractionContext>,
		renderer: DrawableButtonRenderer,
		round: Boolean = false
	) : this(location, tooltipKey, StylableImpl(styleType = styleType, styleProvider = styleProvider), actorAction, renderer, round)

	private val actorInteractionHandler = createActorInteractionHandler()

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler = actorInteractionHandler

	override fun <T: InputEventContext> getExecutionTooltip(context: T): Tooltip? = getTooltip(context)

	protected open fun createActorInteractionHandler(): ActorInteractionHandler = ActorHandler()

	protected open inner class ActorHandler : InputEventHandlerAdapter<ActorInteractionContext>() {

		override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? =
			if (keepMouseMoved(context.location)) this else null

		override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? {
			if (enabled) {
				actorAction.execute(context)
			}
			return null
		}
	}
}