package ch.scorpion.jabbah.edit

/**
 * Manages the selected [Component]s of a [Drawing].
 */
interface SelectionManager {

    /** Holds the number of selected [Component]s.*/
    val selectionCount: Int get() = selection.count()

    /** Holds the currently selected [Component]s*/
    val selection: Collection<Component>

	fun dispose()

    /**
     * Selects the specified [Component].
     *
     * If the specified [Component] is already selected, this method does nothing.
     * @param component the [Component] to select
     */
    fun select(component: Component)

    /**
     * Selects the specified [Component]s.
     * @param components the [Component]s to be selected.
     */
    fun select(components: Collection<Component>)

    /**
     * Selects all [Component]s in the [Drawing] whose selections are managed by this
     * [SelectionManager].
     */
    fun selectAll()

    /**
     * Deselects the specified [Component].
     *
     * If the specified [Component] is already deselected, this method does nothing.
     * @param component the [Component] to deselect.
     */
    fun deselect(component: Component)

    /**
     * Deselects the specified [Component]s.
     *
     * If the specified [Component]s is already deselected, this method does nothing.
     * @param components the [Component]s to deselect.
     */
    fun deselect(components: Collection<Component>)

    /**
     * Deselects all [Component]s in the [Drawing] whose selections are managed by this
     * [SelectionManager].
     */
    fun deselectAll()

    /**
     * Determines whether the specified [Component] is selected.
     * @param component the [Component] whose selection state is to be determined.
     * *
     * @return `true` if `component` is currently selected.
     */
    fun isSelected(component: Component): Boolean

	/**
	 * Selects the next [Component] of a [Drawing], or the first one if none is yet selected.
	 * Does nothing if the [Drawing] is empty.
	 */
	fun selectNext()

	/**
	 * Selects the previous [Component] of a [Drawing], or the last one if none is yet selected.
	 * Does nothing if the [Drawing] is empty.
	 */
	fun selectPrevious()
}

/**
 * Creates a [SelectionManager] for a particular [DrawingView].
 */
interface SelectionManagerFactory {

	/** Creates a new [SelectionManager] for the specified [DrawingViewContent].*/
	fun create(content: DrawingViewContent<*>): SelectionManager
}