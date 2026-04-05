package io.antarescircuit.jabbah.draw.view

import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.view.find.Searchable

/**
 * Represents a view that can be managed by [ContentViewManager] and therefore be the "current" view of the system.
 *
 * @param C the type of context supported by [view]
 */
interface ContentView<C : InputEventContext> : Searchable {

	/** The object of the platform UI system that displays [view]. Used for focus management.*/
	val mainUI: Any?

	/**
	 * The [View] containing [Drawable]s displayed by this [ContentView].
	 * Can be `null` if this [ContentView] doesn't display [Drawable]s, but any other kind of content.
	 */
	val view: View<out C>?
}