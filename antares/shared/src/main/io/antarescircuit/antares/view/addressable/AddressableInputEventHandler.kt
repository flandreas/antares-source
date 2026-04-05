package io.antarescircuit.antares.view.addressable

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.draw.InputEventHandlerAdapter
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.VerticeViewActorInteractionHandler

class AddressableInputEventHandler(
	private val eventBus: EventBus = BaseModule.eventBus,
	private val openRequestProvider: (view: DrawingView<GraphView>, newDesktopView: Boolean) -> OpenMemoryContentsRequest,
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
				context.mouseEvent?.consumeEvent()
				return null
			}
			return super.mouseClicked(context)
		}
	}

	private inner class ActorHandler : VerticeViewActorInteractionHandler(openable = true) {
		override fun handleDoubleClick(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
			requestOpenMemoryContents(context.view as DrawingView<GraphView>, context.mouseEvent?.isAltDown == true)
			context.mouseEvent?.consumeEvent()
			return null
		}
	}
}