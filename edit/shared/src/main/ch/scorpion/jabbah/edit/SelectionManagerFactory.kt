package ch.scorpion.jabbah.edit

/**
 * Creates a [SelectionManager] for a particular [DrawingView].
 */
interface SelectionManagerFactory {

    /** Creates a new [SelectionManager] for the specified [DrawingView].*/
    fun create(view: DrawingView<out Drawing<Component>>): SelectionManager
}