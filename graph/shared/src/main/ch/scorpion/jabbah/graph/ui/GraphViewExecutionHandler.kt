package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.FocusManager
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.ApplicationMode.EXECUTE
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView


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
 * [GraphViewExecutionHandler] listens for [SchedulerActivationStateEvent]s from [scheduler] and disables
 * itself when not in execution mode.
 */
class GraphViewExecutionHandler(
	view: DrawingView<GraphView>,
	private val scheduler: Scheduler = ExecutionModule.scheduler,
	eventBus: EventBus = BaseModule.eventBus,
	applicationMode: ApplicationMode
) : AbstractGraphViewExecutionHandler(view, eventBus, applicationMode) {

	companion object {
		private val LOG by logger(GraphViewExecutionHandler::class)
	}

	/** The target [ActorInteractionHandler] to which the next event is forwarded during complex interactions.*/
	private var target: ActorInteractionHandler? = null

	override fun createMouseHandler(): MouseAdapter = MouseHandler()

	override fun createKeyHandler(): KeyAdapter = KeyHandler()

	override val activationCondition: Boolean get() = currentMode === EXECUTE

	override fun activate() {
		super.activate()
		view.addMouseListener(mouseHandler)
	}

	override fun passivate() {
		super.passivate()
		view.removeMouseListener(mouseHandler)
	}

	// TODO Refactoring: Many commonalities with SelectionToolImpl. Unify!
	private inner class MouseHandler : MouseAdapter() {

		override fun mouseMoved(e: MouseEvent) {
			val x = view.viewToModelX(e.x.toDouble())
			val y = view.viewToModelY(e.y.toDouble())
			val context = mouseEventContext(e, x, y)

			if (target != null) {
				target = target!!.mouseMoved(context)
				if (target != null) {
					return
				}
			}

			val actorViewAt = getActorViewAt(x, y)
			target = actorViewAt?.getActorInteractionHandler(context)?.mouseMoved(context)
			if (actorViewAt == null) {
				view.setCursor(Cursor.DEFAULT)
			}
			tooltipHandler.handle(view, view.drawing, x, y)
		}

		override fun mousePressed(e: MouseEvent) {
			tooltipHandler.clear(view)

			if (e.button == Button.BUTTON2) {
				return
			}

			val x = view.viewToModelX(e.x.toDouble())
			val y = view.viewToModelY(e.y.toDouble())
			val context = mouseEventContext(e, x, y)

			if (target != null) {
				target = target?.mousePressed(context)
				if (target != null) {
					return
				}
			}

			val actorViewAt = getActorViewAt(x, y)
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

			val x = view.viewToModelX(e.x.toDouble())
			val y = view.viewToModelY(e.y.toDouble())
			val context = mouseEventContext(e, x, y)

			if (target != null) {
				target = target?.mouseDragged(context)
				if (target != null) {
					return
				}
			}

			val actorViewAt = getActorViewAt(x, y)
			target = actorViewAt?.getActorInteractionHandler(context)?.mouseDragged(context)
			if (actorViewAt == null) {
				view.setCursor(Cursor.DEFAULT)
			}
		}

		override fun mouseReleased(e: MouseEvent) {
			if (!e.isLeftButtonDown) {
				return
			}

			val x = view.viewToModelX(e.x.toDouble())
			val y = view.viewToModelY(e.y.toDouble())
			val context = mouseEventContext(e, x, y)

			if (target != null) {
				target = target?.mouseReleased(context)
			}
			target = null
			if (getActorViewAt(x, y) == null) {
				view.setCursor(Cursor.DEFAULT)
			}
		}

		override fun mouseClicked(e: MouseEvent) {
			if (e.button !== Button.BUTTON1) {
				return
			}

			val x = view.viewToModelX(e.x.toDouble())
			val y = view.viewToModelY(e.y.toDouble())
			val context = mouseEventContext(e, x, y)

			if (target != null) {
				target = target?.mouseClicked(context)
				return
			}

			val actorViewAt = getActorViewAt(x, y)
			target = actorViewAt?.getActorInteractionHandler(context)?.mouseClicked(context)
		}

		private fun mouseEventContext(e: MouseEvent, x: Double, y: Double): ActorInteractionContext {
			return ActorInteractionContext(
				signalHandler = scheduler,
				view = view,
				mouseEvent = e,
				x = x,
				y = y
			)
		}
	}

	/** Performs a single execution step if [Scheduler] is currently paused (i.e. if in single step mode). */
	private inner class KeyHandler : KeyAdapter() {

		override fun keyPressed(e: KeyEvent) {
			LOG.debug("keyPressed: ${e.key}")

			val context = keyEventContext(e)

			// Try to forward KeyEvent to the focus ActorView
			if (FocusManager.focusOwner is ActorView) {
				(FocusManager.focusOwner as ActorView).getActorInteractionHandler(context).keyPressed(context)
				return
			}

			// Try to forward KeyEvent to any ActorView that consumes it
			if (FocusManager.focusOwner == null) {
				view.drawing.getVerticeViews().forEach {
					it.getActorInteractionHandler(context).keyPressed(context)
					context.keyEvent?.let { event ->
						if (event.isConsumed()) {
							return
						}
					}
				}
			}
		}

		override fun keyReleased(e: KeyEvent) {
			val context = keyEventContext(e)

			if (FocusManager.focusOwner is ActorView) {
				(FocusManager.focusOwner as ActorView).getActorInteractionHandler(context).keyReleased(context)
				return
			}

			// Try to forward KeyEvent to any ActorView that consumes it
			if (FocusManager.focusOwner == null) {
				view.drawing.getVerticeViews().forEach {
					it.getActorInteractionHandler(context).keyReleased(context)
					context.keyEvent?.let { event ->
						if (event.isConsumed()) {
							return
						}
					}
				}
			}
		}

		private fun keyEventContext(e: KeyEvent): ActorInteractionContext {
			return ActorInteractionContext(
				signalHandler = scheduler,
				view = view,
				keyEvent = e
			)
		}
	}
}