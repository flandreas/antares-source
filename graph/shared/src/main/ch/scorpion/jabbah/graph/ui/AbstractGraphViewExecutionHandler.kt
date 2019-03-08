package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.event.MouseAdapter
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.view.TooltipHandler
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.ApplicationModeEvent
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView

abstract class AbstractGraphViewExecutionHandler(
	protected val view: DrawingView<GraphView<GraphElementView<*>>>,
	protected val eventBus: EventBus = BaseModule.eventBus
) {

	protected var currentMode: ApplicationMode = ApplicationMode.EDIT
		private set

	protected val mouseHandler = createMouseHandler()

	private val modeEventHandler: EventHandler<ApplicationModeEvent> = {
		currentMode = it.applicationMode
		updateActivationState()
	}

	init {
		eventBus.register(ApplicationModeEvent::class, modeEventHandler)
	}

	protected abstract fun createMouseHandler(): MouseAdapter

	protected abstract  val activationCondition: Boolean

	open fun dispose() {
		eventBus.unregister(ApplicationModeEvent::class, modeEventHandler)
		passivate()
	}

	protected open fun activate() {
		view.addMouseMotionListener(mouseHandler)
	}

	protected open fun passivate() {
		view.removeMouseMotionListener(mouseHandler)
	}

	private fun updateActivationState() {
		if (activationCondition) {
			activate()
		} else {
			passivate()
		}
	}

	/** Gateway to the custom tooltip system.*/
	protected val tooltipHandler = TooltipHandler(
		eventBus,
		{ _, x, y -> getActorViewAt(x, y) as Drawable? },
		{ d, x, y -> (d as ActorView).getExecutionTooltip(x, y) })

	/** Returns the [ActorView] in [view] at the specified location, if any.*/
	protected fun getActorViewAt(x: Double, y: Double): ActorView? {
		return view.getInnerDrawableAt(x, y) { it is ActorView } as ActorView?
	}
}