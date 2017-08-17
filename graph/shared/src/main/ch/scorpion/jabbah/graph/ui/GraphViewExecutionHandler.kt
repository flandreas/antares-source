package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.FocusManager
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.draw.graphics.Cursor


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
        private val view: DrawingView<GraphView<GraphElementView<*>>>,
        private val scheduler: Scheduler,
        eventBus: EventBus
) {

    /** Handles [MouseEvent]s on [view] during execution.*/
    private val mouseHandler = MouseHandler()

    /** Handles [KeyEvent]s on [view] during execution.*/
    private val keyHandler = KeyHandler()

    /** Returns the [ActorView] in [view] at the specified location, if any.*/
    private fun getActorViewAt(x: Double, y: Double): ActorView? {
        val drawable = view.drawing.getDrawableAt(x, y)
        if (drawable != null && drawable is ActorView) {
            return drawable
        }
        return null
    }

    init {
        eventBus.register(SchedulerActivationStateEvent::class, { updateActivationState() })
        updateActivationState()
    }

    private fun updateActivationState() {
        if (scheduler.isActive) {
            view.addMouseListener(mouseHandler)
            view.addMouseMotionListener(mouseHandler)
            view.addKeyListener(keyHandler)
        } else {
            view.removeMouseListener(mouseHandler)
            view.removeMouseMotionListener(mouseHandler)
            view.removeKeyListener(keyHandler)
        }
    }

    private inner class MouseHandler: MouseAdapter() {

        override fun mouseMoved(e: MouseEvent) {
            val x = view.viewToModelX(e.x.toDouble())
            val y = view.viewToModelY(e.y.toDouble())

            val actorView = getActorViewAt(x, y)
            if (actorView != null) {
                view.setToolTipText(actorView.getExecutionToolTipText(x, y, 150))
            } else {
                view.setToolTipText(null)
            }

            if (actorView != null && actorView.getActorInteractionHandler() != null) {
                view.setCursor(Cursor.HAND)
            } else {
                view.setCursor(Cursor.DEFAULT)
            }
        }

        override fun mousePressed(e: MouseEvent) {
            if (e.button !== Button.BUTTON1) {
                return
            }

            val x = view.viewToModelX(e.x.toDouble())
            val y = view.viewToModelY(e.y.toDouble())

            val actorView = getActorViewAt(x, y)
            if (actorView != null && actorView.getActorInteractionHandler() != null) {
                if (actorView is Component && actorView.isFocusable) {
                    actorView.requestFocus()
                }
                actorView.getActorInteractionHandler()!!.mousePressed(scheduler.signalHandler, e, x, y)
                view.drawing.validate()
            }
        }

        override fun mouseReleased(e: MouseEvent) {
            if (e.button !== Button.BUTTON1) {
                return
            }

            val x = view.viewToModelX(e.x.toDouble())
            val y = view.viewToModelY(e.y.toDouble())

            val actorView = getActorViewAt(x, y)
            if (actorView != null && actorView.getActorInteractionHandler() != null) {
                actorView.getActorInteractionHandler()!!.mouseReleased(scheduler.signalHandler, e, x, y)
                view.drawing.validate()
            }
        }

        override fun mouseClicked(e: MouseEvent) {
            if (e.button !== Button.BUTTON1) {
                return
            }

            val x = view.viewToModelX(e.x.toDouble())
            val y = view.viewToModelY(e.y.toDouble())

            val actorView = getActorViewAt(x, y)
            if (actorView != null && actorView.getActorInteractionHandler() != null) {
                actorView.getActorInteractionHandler()!!.mouseClicked(scheduler.signalHandler, e, x, y)
                view.drawing.validate()
            }
        }
    }

    /** Performs a single execution step if [Scheduler] is currently paused (i.e. if in single step mode). */
    private inner class KeyHandler : KeyAdapter() {
        override fun keyPressed(e: KeyEvent) {
            if (e.key == ' '.toInt()) {
                if (scheduler.isPaused) {
                    scheduler.step()
                }
            }
            if (FocusManager.focusOwner is ActorView) {
                (FocusManager.focusOwner as ActorView).getActorInteractionHandler()?.keyPressed(scheduler.signalHandler, e)
            }
        }
    }
}