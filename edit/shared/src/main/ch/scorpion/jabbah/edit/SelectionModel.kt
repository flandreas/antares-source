package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.draw.Drawable

/**
 * A [SelectionModel] is used to graphically show that a [Component] is selected.
 *
 * [SelectionModel] instances don't have a one-to-one relationship with [Component] instances. Instead,
 * [SelectionModel] instances are designed to potentially be cached and reused for different [Component]
 * instances of the same class.
 *
 * Implementing classes should detach from its [Component] in [dispose].
 *
 * [SelectionModel]s are provided by instances of [SelectionModelProvider].
 * @param T the type of the [Component] this [SelectionModel] selects
 */
interface SelectionModel<out T : Component> : Drawable {

    /** Holds the [Component] that is selected by this [SelectionModel]. */
    val component: T

    /**
     * Performs a setup of this [SelectionModel] according to the [Component] for which it has been created.
     * Implementing classes should adjust their geometry and start listening for geometry updates of the [Component].
     */
    fun setup()

    /**
     * Notifies this [SelectionModel] that it has been added to the selection container of the specified
     * [DrawingView] in order to select the [Component] that has been set in [component].
     *
     * This method must be called by a [SelectionManager] after adding this [SelectionModel] to the
     * selection container of a [DrawingView].
     *
     * Can be implemented by subclasses that must adjust their state according to the containing [DrawingView]'s
     * state, for example to react to zoom factor changes.
     *
     * @param view the [DrawingView] to which this [SelectionModel] has been added.
     */
    fun notifyAdded(view: DrawingView<*>)

    /**
     * Notifies this [SelectionModel] that it has been removed from the selection container of the specified
     * [DrawingView] in order to stop selecting the [Component] that has been set in [component].
     *
     * This method must be called by a [SelectionManager] after removing this [SelectionModel] from the
     * selection container of a [DrawingView].
     *
     * @param view the [DrawingView] from which this [SelectionModel] has been removed.
     */
    fun notifyRemoved(view: DrawingView<*>)

	/**
	 * This method is automatically called whenever the underlying [Component] geometry has been changed.
	 *
	 * Inheriting classes implement this method to update the shape of this [SelectionModel] according to the new
	 * geometry of the [Component].
	 */
	fun componentUpdated()
}