package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.Drawable

/**
 * Represents a view that can be managed by [ContentViewManager] and therefore be the "current" view of the system.
 *
 * @param C the type of context supported by [view]
 */
interface ContentView<C : InputEventContext> {

	/**
	 * The main object hold by this [ContentView] and thus the one whose properties might be
	 * viewed or edited by the user.
	 * Can be `null` if this [ContentView] is currently "empty".
	 */
	val mainBean: Any?

	/** The object of the platform UI system that displays [view]. Used for focus management.*/
	val mainUI: Any?

	/**
	 * The [View] containing [Drawable]s displayed by this [ContentView].
	 * Can be `null` if this [ContentView] doesn't display [Drawable]s, but any other kind of content.
	 */
	val view: View<out C>?
}