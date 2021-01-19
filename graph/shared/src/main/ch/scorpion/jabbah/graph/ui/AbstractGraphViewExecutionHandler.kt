package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.TooltipHandler
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeEvent
import ch.scorpion.jabbah.graph.view.GraphView

abstract class AbstractGraphViewExecutionHandler(
	protected val view: DrawingView<GraphView>,
	protected val eventBus: EventBus = BaseModule.eventBus,
	applicationMode: ApplicationMode
) {

	protected var currentMode: ApplicationMode = applicationMode
		private set

	protected val mouseHandler = createMouseHandler()

	protected val keyHandler = createKeyHandler()

	private val modeEventHandler: EventHandler<ApplicationModeEvent> = {
		currentMode = it.applicationMode
		updateActivationState()
	}

	private val viewCanvasListener: PropertyChangeListener<Any> = object : PropertyChangeListener<Any> {
		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			if (e.name == View.PROP_CANVAS) {
				updateActivationState()
				view.removePropertyChangeListener(this)
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
		{ d, x, y -> (d as ActorView).getExecutionTooltip(x, y) })

	/** Returns the [ActorView] in [view] at the specified location, if any.*/
	protected fun getActorViewAt(x: Double, y: Double): ActorView? {
		return view.getInnerDrawableAt(x, y) { it is ActorView } as ActorView?
	}
}