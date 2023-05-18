package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.View

abstract class AbstractContentViewAction(
	baseName: String,
	protected val eventBus: EventBus = BaseModule.eventBus,
	val viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractAction(baseName) {

	private val activeViewHandler: EventHandler<ActiveContentViewChangedEvent> = { activeViewChanged(it.oldView, it.newView) }

	init {
		eventBus.register(ActiveContentViewChangedEvent::class, activeViewHandler)
		updateEnabled()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(activeViewHandler)
	}

	private val viewPropertyListener = ViewPropertyListener()

	protected val contentView: ContentView<*>? get() = viewManager.activeView

	protected val view: View<*>? get() = viewManager.activeView?.view

	private inner class ViewPropertyListener : PropertyChangeListener<Any> {
		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			handleViewPropertyChanged(e)
		}
	}

	protected open fun handleViewPropertyChanged(e: PropertyChangeEvent<Any>) {
		// empty
	}

	private fun activeViewChanged(oldView: ContentView<*>?, newView: ContentView<*>?) {
		oldView?.view?.removePropertyChangeListener(viewPropertyListener)
		updateEnabled()
		newView?.view?.addPropertyChangeListener(viewPropertyListener)
		notifyActiveViewChanged()
	}

	protected fun updateEnabled() {
		enabled = calculateEnabled()
	}

	protected open fun calculateEnabled(): Boolean = viewManager.activeView != null

	protected open fun notifyActiveViewChanged() {
		// empty
	}
}