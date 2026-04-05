package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.base.event.*
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.draw.FocusManager
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.execution.actor.ActorView
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.execution.scheduler.SchedulerActivationStateEvent
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.app.ApplicationMode.EXECUTE
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView


/**
 * Handles input events on a root [GraphView] while its [Graph] is executed by a [Scheduler].
 *
 * A [GraphViewExecutionHandler] has generally the following responsibilities:
 *
 * - Display the tooltip of the [GraphElementView] at the current mouse location
 * - Dive into a [SubGraphVerticeView] when the user double clicks on it
 * - Forward mouse and key events to [ActorInteractionHandler]s of [ActorView]s like switches
 *
 * The responsibilities 1. and 2. are already covered by the [GraphView]'s [Editor] when in edit mode.
 *
 * [GraphViewExecutionHandler] listens for [SchedulerActivationStateEvent]s from [Scheduler] and disables
 * itself when not in execution mode.
 */
class GraphViewExecutionHandler(
	view: DrawingView<GraphView>,
	private val applicationContextHolder: GraphApplicationContextHolder,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractGraphViewExecutionHandler(view, eventBus) {

	companion object {
		private val LOG by logger(GraphViewExecutionHandler::class)
	}

	/** The target [ActorInteractionHandler] to which the next event is forwarded during complex interactions.*/
	private var target: ActorInteractionHandler? = null

	override fun createMouseHandler(): MouseAdapter = MouseHandler()

	override fun createKeyHandler(): KeyAdapter = KeyHandler()

	override val activationCondition: Boolean get() = currentMode === EXECUTE

	/** Enhance superclass behaviour by mouseListener.*/
	override fun activate() {
		super.activate()
		view.addMouseListener(mouseHandler)
	}

	/** Enhance superclass behaviour by mouseListener.*/
	override fun passivate() {
		super.passivate()
		FocusManager.resetFocus()
		view.removeMouseListener(mouseHandler)
		target = null
	}

	// TODO Refactoring: Many commonalities with SelectionToolImpl. Unify!
	private inner class MouseHandler : MouseAdapter() {

		override fun mouseMoved(e: MouseEvent) {
			val p = view.viewToModel(e.location)
			val context = mouseEventContext(e, p)

			if (target != null) {
				target = target!!.mouseMoved(context)
				if (target != null) {
					return
				}
			}

			val actorViewAt = getActorViewAt(p.x, p.y)
			target = actorViewAt?.getActorInteractionHandler(context)?.mouseMoved(context)
			if (actorViewAt == null) {
				view.setCursor(Cursor.DEFAULT)
			}
			tooltipHandler.handle(view, view.drawing, context)
		}

		override fun mousePressed(e: MouseEvent) {
			tooltipHandler.clear(view)

			if (e.button == Button.BUTTON2) {
				return
			}

			val p = view.viewToModel(e.location)
			val context = mouseEventContext(e, p)

			if (target != null) {
				target = target?.mousePressed(context)
				if (target != null) {
					return
				}
			}

			val actorViewAt = getActorViewAt(p.x, p.y)
			target = actorViewAt?.getActorInteractionHandler(context)?.mousePressed(context)
			if (actorViewAt == null) {
				view.setCursor(Cursor.DEFAULT)
				FocusManager.resetFocus()
			}
		}

		override fun mouseDragged(e: MouseEvent) {
			if (!e.isLeftButtonDown) {
				return
			}

			val p = view.viewToModel(e.location)
			val context = mouseEventContext(e, p)

			if (target != null) {
				target = target?.mouseDragged(context)
				if (target != null) {
					return
				}
			}

			val actorViewAt = getActorViewAt(p.x, p.y)
			target = actorViewAt?.getActorInteractionHandler(context)?.mouseDragged(context)
			if (actorViewAt == null) {
				view.setCursor(Cursor.DEFAULT)
			}
		}

		override fun mouseReleased(e: MouseEvent) {
			if (!e.isLeftButtonDown) {
				return
			}

			val p = view.viewToModel(e.location)
			val context = mouseEventContext(e, p)

			if (target != null) {
				target = target?.mouseReleased(context)
				if (target != null) {
					return
				}
			}
			target = null
			if (getActorViewAt(p.x, p.y) == null) {
				view.setCursor(Cursor.DEFAULT)
			}
		}

		override fun mouseClicked(e: MouseEvent) {
			if (e.button !== Button.BUTTON1) {
				return
			}

			val p = view.viewToModel(e.location)
			val context = mouseEventContext(e, p)

			if (target != null) {
				target = target?.mouseClicked(context)
				return
			}

			val actorViewAt = getActorViewAt(p.x, p.y)
			target = actorViewAt?.getActorInteractionHandler(context)?.mouseClicked(context)
		}

		private fun mouseEventContext(e: MouseEvent, p: Point2D): ActorInteractionContext {
			return ActorInteractionContext(
				signalHandler = applicationContextHolder.scheduler,
				view = view,
				mouseEvent = e,
				x = p.x,
				y = p.y
			)
		}
	}

	/** Performs a single execution step if [Scheduler] is currently paused (i.e. if in single step mode). */
	private inner class KeyHandler : KeyAdapter() {

		override fun keyPressed(e: KeyEvent) {
			LOG.trace("keyPressed: ${e.key}")

			val context = keyEventContext(e)

			// Try to forward KeyEvent to the focus ActorView
			if (FocusManager.focusOwner is ActorView) {
				target = (FocusManager.focusOwner as ActorView).getActorInteractionHandler(context).keyPressed(context)
				return
			}

			// Try to forward KeyEvent to any ActorView that consumes it
			if (FocusManager.focusOwner == null) {
				view.drawing.getVerticeViews().forEach {
					it.getActorInteractionHandler(context).keyPressed(context)
					context.keyEvent?.let { event ->
						if (event.isEventConsumed()) {
							return
						}
					}
				}
			}
		}

		override fun keyReleased(e: KeyEvent) {
			val context = keyEventContext(e)

			if (FocusManager.focusOwner is ActorView) {
				target = (FocusManager.focusOwner as ActorView).getActorInteractionHandler(context).keyReleased(context)
				return
			}

			// Try to forward KeyEvent to any ActorView that consumes it
			if (FocusManager.focusOwner == null) {
				view.drawing.getVerticeViews().forEach {
					it.getActorInteractionHandler(context).keyReleased(context)
					context.keyEvent?.let { event ->
						if (event.isEventConsumed()) {
							return
						}
					}
				}
			}
		}

		private fun keyEventContext(e: KeyEvent): ActorInteractionContext {
			return ActorInteractionContext(
				signalHandler = applicationContextHolder.scheduler,
				view = view,
				keyEvent = e
			)
		}
	}
}