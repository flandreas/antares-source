package io.antarescircuit.jabbah.draw.view

import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.event.PropertyChangeEvent
import io.antarescircuit.jabbah.base.event.PropertyChangeListener
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.View

abstract class AbstractContentViewAction(
	baseName: String,
	protected val eventBus: EventBus = BaseModule.eventBus,
	val viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractAction(baseName) {

	private val activeViewHandler: EventHandler<ActiveContentViewChangedEvent> = { activeViewChanged(it.oldView, it.newView) }

	private val viewPropertyListener = ViewPropertyListener()

	protected val contentView: ContentView<*>? get() = viewManager.activeView

	protected val view: View<*>? get() = viewManager.activeView?.view

	init {
		eventBus.register(ActiveContentViewChangedEvent::class, activeViewHandler)
		view?.addPropertyChangeListener(viewPropertyListener)
		enabled = false
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(activeViewHandler)
	}

	protected fun <T> castedView(): T = viewManager.castedActiveView()!!

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

	override fun calculateEnabled(): Boolean = viewManager.activeView != null

	protected open fun notifyActiveViewChanged() {
		// empty
	}
}