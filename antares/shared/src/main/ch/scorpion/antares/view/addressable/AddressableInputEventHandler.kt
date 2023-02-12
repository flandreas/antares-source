package ch.scorpion.antares.view.addressable

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.VerticeViewActorInteractionHandler

class AddressableInputEventHandler(
	private val openRequestProvider: (view: DrawingView<GraphView>, newDesktopView: Boolean) -> OpenMemoryContentsRequest,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	private val inputEventHandler = DoubleClickHandler()
	private val actorInteractionHandler = ActorHandler()

	fun <T : InputEventContext> getInputEventHandler(): InputEventHandler<T> = inputEventHandler

	fun getActorInteractionHandler(component: Component): ActorInteractionHandler {
		VerticeViewActorInteractionHandler.getInactiveInstance(component)
		return actorInteractionHandler
	}

	private fun requestOpenMemoryContents(view: DrawingView<GraphView>, newDesktopView: Boolean) {
		eventBus.post(openRequestProvider(view, newDesktopView))
	}

	private inner class DoubleClickHandler : InputEventHandlerAdapter<InputEventContext>() {
		override fun mouseClicked(context: InputEventContext): InputEventHandler<InputEventContext>? {
			if (context.mouseEvent?.clickCount == 2 && context.mouseEvent?.isLeftButtonDown == true) {
				requestOpenMemoryContents(context.view as DrawingView<GraphView>, context.mouseEvent?.isAltDown == true)
				return null
			}
			return super.mouseClicked(context)
		}
	}

	private inner class ActorHandler : VerticeViewActorInteractionHandler() {
		override fun handleDoubleClick(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
			requestOpenMemoryContents(context.view as DrawingView<GraphView>, context.mouseEvent?.isAltDown == true)
			return null
		}
	}
}