package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View

/**
 * Manages open [View]s and allows to create new [View]s.
 * Posts [ActiveViewChangedEvent] on [EventBus] whenever the active [View] changes.
 */
interface ViewManager {

	/** Holds the currently active [View]. */
	var activeView: View<out InputEventContext>?

	@Suppress("UNCHECKED_CAST")
	fun <T> castedActiveView(): T? = activeView as T?

	/** Registers [View].*/
	fun registerView(view: View<out InputEventContext>)

	/** Unregisters a [View]. */
	fun unregisterView(view: View<out InputEventContext>)
}

/**
 * Posted by a [ViewManager] when the active {@link View} has changed.
 */
data class ActiveViewChangedEvent(
	val viewManager: ViewManager,
	val oldView: View<out InputEventContext>?,
	val newView: View<out InputEventContext>?)