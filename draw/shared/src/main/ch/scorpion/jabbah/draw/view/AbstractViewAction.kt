package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View

/**
 * A base implementation of an [Action] that acts on the currently active [View] in a [ViewManager]
 * and that disables itself if no [View] is active.
 *
 * Listens for [PropertyChangeEvent] from the active [View] and calls [handleViewPropertyChanged] to
 * allow subclasses to handle them.
 */
abstract class AbstractViewAction(
	baseName: String,
	protected val eventBus: EventBus = BaseModule.eventBus,
	val viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractAction(baseName) {

	private val activeViewHandler: EventHandler<ActiveViewChangedEvent> = { activeViewChanged(it.oldView, it.newView) }

	init {
		eventBus.register(ActiveViewChangedEvent::class, activeViewHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(activeViewHandler)
	}

	private val viewPropertyListener = ViewPropertyListener()

	protected val view: View<*>? get() = viewManager.activeView

	private inner class ViewPropertyListener : PropertyChangeListener<Any> {
		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			handleViewPropertyChanged(e)
		}
	}

	protected open fun handleViewPropertyChanged(e: PropertyChangeEvent<Any>) {
		// empty
	}

	private fun activeViewChanged(oldView: View<out InputEventContext>?, newView: View<out InputEventContext>?) {
		oldView?.removePropertyChangeListener(viewPropertyListener)
		updateEnabled()
		newView?.addPropertyChangeListener(viewPropertyListener)
		notifyActiveViewChanged()
	}

	protected fun updateEnabled() {
		enabled = calculateEnabled()
	}

	protected open fun calculateEnabled(): Boolean {
		return viewManager.activeView != null
	}

	protected open fun notifyActiveViewChanged() {
		// empty
	}
}

