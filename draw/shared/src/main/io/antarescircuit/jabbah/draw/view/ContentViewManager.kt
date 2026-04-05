package io.antarescircuit.jabbah.draw.view

import io.antarescircuit.jabbah.base.event.EventBus

/**
 * Manages open [ContentView]s.
 * Posts [ActiveContentViewChangedEvent] on [EventBus] whenever the active [ContentView] changes.
 */
interface ContentViewManager {

	/** Holds the currently active [ContentView]. */
	var activeView: ContentView<*>?

	@Suppress("UNCHECKED_CAST")
	fun <T> castedActiveView(): T? = activeView as T?
}

/**
 * Posted by a [ContentViewManager] when the active [ContentView] has changed.
 */
data class ActiveContentViewChangedEvent(
	val viewManager: ContentViewManager,
	val oldView: ContentView<*>?,
	val newView: ContentView<*>?)