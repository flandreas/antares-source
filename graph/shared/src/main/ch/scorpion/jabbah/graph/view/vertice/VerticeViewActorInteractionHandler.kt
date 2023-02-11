package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.graph.view.VerticeView

/**
 * A base implementation of [InputEventHandler] in [ActorInteractionContext] that
 * provides some basic functionality used by most [VerticeView]s:
 *
 * - Display [Cursor.CLICK] in [mouseMoved] if [clickable] is `true`
 * - Posts a [ComponentMessage] on the system [EventBus] if the user double-clicks on the [VerticeView]
 *  and [openable] is `false`
 */
open class VerticeViewActorInteractionHandler(
	private val clickable: Boolean = false,
	private val openable: Boolean = false
) : InputEventHandlerAdapter<ActorInteractionContext>() {

	companion object {
		private var COMPONENT: Component? = null

		private val INACTIVE_INSTANCE = VerticeViewActorInteractionHandler()

		fun getInactiveInstance(component: Component): VerticeViewActorInteractionHandler {
			COMPONENT = component
			return INACTIVE_INSTANCE
		}
	}

	override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? {
		if (clickable) {
			context.view.setCursor(Cursor.CLICK)
		}
		return null
	}

	override fun mouseClicked(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? =
		when (context.mouseEvent?.clickCount) {
			1 -> handleSingleClick(context)
			2 -> handleDoubleClick(context)
			else -> null
		}

	protected open fun handleSingleClick(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
		if (COMPONENT is VerticeView<*>) {
			(COMPONENT as VerticeView<*>).getPortViewAt(context.x, context.y)?.let { pv ->
				if (!pv.port.isConnected && pv.port.portType.isInput) {
					pv.handleExecutionClick(context)
				}
			}
		}
		return null
	}

	protected open fun handleDoubleClick(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
		COMPONENT?.let {
			AbstractVerticeView.cannotOpenMsg(it)
		}
		return null
	}
}