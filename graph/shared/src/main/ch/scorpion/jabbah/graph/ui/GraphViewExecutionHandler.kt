package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.view.TooltipHandler
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.FocusManager
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionContextImpl
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
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
        private val view: DrawingView<GraphView<GraphElementView<*>>>,
        private val scheduler: Scheduler,
        eventBus: EventBus
) {

    /** Handles [MouseEvent]s on [view] during execution.*/
    private val mouseHandler = MouseHandler()

    /** Handles [KeyEvent]s on [view] during execution.*/
    private val keyHandler = KeyHandler()

    /** Gateway to the custom tooltip system.*/
    private val tooltipHandler = TooltipHandler(
            eventBus,
            { _, x, y -> getActorViewAt(x, y) as Drawable? },
            { d, x, y -> (d as ActorView).getExecutionTooltip(x, y ) })

	private val context = ReusableActorInteractionContext(
		signalHandler = scheduler.signalHandler,
		view = view
	)

    /** Returns the [ActorView] in [view] at the specified location, if any.*/
    private fun getActorViewAt(x: Double, y: Double): ActorView? {
        val drawable = view.drawing.getDrawableAt(x, y)
        if (drawable != null && drawable is ActorView) {
            return drawable
        }
        return null
    }

    init {
        eventBus.register(SchedulerActivationStateEvent::class) { updateActivationState() }
	    updateActivationState()
    }

    fun dispose() {
        passivate()
    }

    private fun updateActivationState() {
        if (scheduler.isActive) {
            activate()
        } else {
            passivate()
        }
    }

    private fun activate() {
        view.addMouseListener(mouseHandler)
        view.addMouseMotionListener(mouseHandler)
        view.addKeyListener(keyHandler)
    }

    private fun passivate() {
        view.removeMouseListener(mouseHandler)
        view.removeMouseMotionListener(mouseHandler)
        view.removeKeyListener(keyHandler)
    }

    private inner class MouseHandler: MouseAdapter() {

        override fun mouseMoved(e: MouseEvent) {
            val x = view.viewToModelX(e.x.toDouble())
            val y = view.viewToModelY(e.y.toDouble())

            val actorView = getActorViewAt(x, y)
            tooltipHandler.handle(view, view.drawing, x, y)

            if (actorView?.getActorInteractionHandler() != null) {
	            actorView.getActorInteractionHandler()!!.mouseMoved(mouseEventContext(e, x, y))
            } else {
                view.setCursor(Cursor.DEFAULT)
            }
        }

        override fun mousePressed(e: MouseEvent) {
            tooltipHandler.clear(view)

            if (e.button == Button.BUTTON2) {
                return
            }

            val x = view.viewToModelX(e.x.toDouble())
            val y = view.viewToModelY(e.y.toDouble())

	        getActorViewAt(x, y)?.let {
		        if (e.button == Button.BUTTON1) {
			        if (it.getActorInteractionHandler() != null) {
				        if (it is Component && it.isFocusable) {
					        it.requestFocus()
				        }
				        it.getActorInteractionHandler()!!.mousePressed(mouseEventContext(e, x, y))
				        view.drawing.validate()
			        }
		        }
	        }
        }

        override fun mouseDragged(e: MouseEvent) {
            if (e.button !== Button.BUTTON1) {
                return
            }

            val x = view.viewToModelX(e.x.toDouble())
            val y = view.viewToModelY(e.y.toDouble())

            val actorView = getActorViewAt(x, y)
            if (actorView?.getActorInteractionHandler() != null) {
                actorView.getActorInteractionHandler()!!.mouseDragged(mouseEventContext(e, x, y))
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
            if (actorView?.getActorInteractionHandler() != null) {
                actorView.getActorInteractionHandler()!!.mouseReleased(mouseEventContext(e, x, y))
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
            if (actorView?.getActorInteractionHandler() != null) {
                actorView.getActorInteractionHandler()!!.mouseClicked(mouseEventContext(e, x, y))
                view.drawing.validate()
            }
        }

	    private fun mouseEventContext(e: MouseEvent, x: Double, y: Double): ActorInteractionContext {
		    context.mouseEvent = e
		    context.keyEvent = null
		    context.x = x
		    context.y = y
		    return context
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
                (FocusManager.focusOwner as ActorView).getActorInteractionHandler()?.keyPressed(keyEventContext(e))
            }
        }

	    private fun keyEventContext(e: KeyEvent): ActorInteractionContext {
		    context.mouseEvent = null
		    context.keyEvent = e
		    context.x = 0.0
		    context.y = 0.0
		    return context
	    }
    }

	/** Used to avoid object creation for every event.*/
	private inner class ReusableActorInteractionContext(
		override val signalHandler: SignalHandler,
		override val view: View<*>,
		override var mouseEvent: MouseEvent? = null,
		override var keyEvent: KeyEvent? = null,
		override var x: Double = 0.0,
		override var y: Double = 0.0
	) : ActorInteractionContext {

		/** Returns a copy of this [ActorInteractionContext] with other x and y coordinates*/
		override fun withXY(x: Double, y: Double): ActorInteractionContext {
			context.x = x
			context.y = y
			return context
		}
	}
}