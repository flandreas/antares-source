package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.ViewManager
import javax.swing.Action

/**
 * A base [Action] that is only enabled if at least one [Component] is selected
 */
abstract class AbstractSelectionAwareAction(
        name: String,
        viewManager: ViewManager,
        protected val eventBus: EventBus
) : AbstractViewAction(name, eventBus, viewManager) {

    constructor(name: String, viewManager: ViewManager): this(name, viewManager, BaseModule.eventBus)

    init {
        eventBus.register(SelectionChangeEvent::class, {handleSelectionChanged(it)})
        isEnabled = false
    }

    /** ---- [AbstractViewAction] */

    override fun activeViewChanged(oldView: View<out InputEventContext>?, newView: View<out InputEventContext>?) {
        isEnabled = viewManager.activeView != null && calculateEnabled()
    }

    /** ---- [AbstractSelectionAwareAction] */

    protected fun handleSelectionChanged(@Suppress("UNUSED_PARAMETER") event: SelectionChangeEvent) {
        isEnabled = calculateEnabled()
    }

    protected open fun calculateEnabled(): Boolean {
        return getSelectionCount() > 0
    }

    protected fun getSelectionCount(): Int {
        return (viewManager.activeView as DrawingView<*>).selectionManager.selectionCount
    }

    /**
     * Returns the one and only selected [Component] or 'null' if none
     * or more than one [Component] is selected.
     */
    protected fun getSingleSelection(): Component? {
        if (getSelectionCount() != 1) {
            return null
        }
        return (viewManager.activeView as DrawingView<*>).selectionManager.selection.first()
    }

    protected fun getSelection(): Collection<Component> {
        return (viewManager.activeView as DrawingView<*>).selectionManager.selection
    }

    protected fun getDrawingView(): DrawingView<*>? {
        return viewManager.activeView as DrawingView<*>
    }
}