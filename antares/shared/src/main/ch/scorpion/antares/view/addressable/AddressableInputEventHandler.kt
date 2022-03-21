package ch.scorpion.antares.view.addressable

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
import ch.scorpion.jabbah.graph.view.GraphView

class AddressableInputEventHandler(
	private val openRequestProvider: (view: DrawingView<GraphView>, newDesktopView: Boolean) -> OpenMemoryContentsRequest,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	private val inputEventHandler = DoubleClickHandler()
	private val actorInteractionHandler = DoubleClickActorHandler()

	fun <T : InputEventContext> getInputEventHandler(): InputEventHandler<T> = inputEventHandler

	fun getActorInteractionHandler(): ActorInteractionHandler = actorInteractionHandler

	private fun requestOpenMemoryContents(view: DrawingView<GraphView>, newDesktopView: Boolean) {
		eventBus.post(openRequestProvider(view, newDesktopView))
	}

	private inner class DoubleClickHandler : InputEventHandlerAdapter<InputEventContext>() {
		override fun mouseClicked(context: InputEventContext): InputEventHandler<InputEventContext>? {
			if (context.mouseEvent?.clickCount == 2) {
				requestOpenMemoryContents(context.view as DrawingView<GraphView>, context.mouseEvent?.isAltDown == true)
				return null
			}
			return super.mouseClicked(context)
		}
	}

	private inner class DoubleClickActorHandler : ClickableActorInteractionHandlerAdapter() {
		override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler? {
			if (context.mouseEvent?.clickCount == 2) {
				requestOpenMemoryContents(context.view as DrawingView<GraphView>, context.mouseEvent?.isAltDown == true)
			}
			return null
		}
	}
}