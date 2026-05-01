package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.base.event.*
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.view.TooltipHandler
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.execution.actor.ActorView
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.app.ApplicationModeEvent
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolder
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView

abstract class AbstractGraphViewExecutionHandler(
	protected val view: DrawingView<GraphElementView<*>, GraphView>,
	protected val eventBus: EventBus = BaseModule.eventBus
) {

	private val applicationModeHolder: ApplicationModeHolder get() = (view.applicationContextHolder as GraphApplicationContextHolder).applicationModeHolder

	protected val currentMode: ApplicationMode get() = applicationModeHolder.currentMode

	protected val mouseHandler = createMouseHandler()

	private val keyHandler = createKeyHandler()

	private val modeEventHandler: EventHandler<ApplicationModeEvent> = {
		if (it.source === applicationModeHolder) {
			updateActivationState()
		}
	}

	private val viewCanvasListener: PropertyChangeListener<Any> = object : PropertyChangeListener<Any> {
		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			if (e.name == View.PROP_CANVAS) {
				updateActivationState()
				//view.removePropertyChangeListener(this)
			}
		}
	}

	init {
		eventBus.register(ApplicationModeEvent::class, modeEventHandler)
		view.addPropertyChangeListener(viewCanvasListener)
	}

	protected abstract fun createMouseHandler(): MouseAdapter

	protected abstract fun createKeyHandler(): KeyAdapter

	protected abstract val activationCondition: Boolean

	open fun dispose() {
		eventBus.unregister(ApplicationModeEvent::class, modeEventHandler)
		view.removePropertyChangeListener(viewCanvasListener)
		tooltipHandler.dispose()
		passivate()
	}

	protected open fun activate() {
		view.addMouseMotionListener(mouseHandler)
		view.addKeyListener(keyHandler)
	}

	protected open fun passivate() {
		view.removeMouseMotionListener(mouseHandler)
		view.removeKeyListener(keyHandler)
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
		{ d, context -> (d as ActorView).getExecutionTooltip(context) })

	/** Returns the [ActorView] in [view] at the specified location, if any.*/
	protected fun getActorViewAt(x: Double, y: Double): ActorView? {
		return view.getInnerDrawableAt(x, y) { it is ActorView } as ActorView?
	}
}