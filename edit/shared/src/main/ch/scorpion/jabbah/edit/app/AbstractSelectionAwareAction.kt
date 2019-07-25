package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.SelectionChangeEvent

/**
 * A base [Action] that is only enabled if at least one [Component] is selected.
 */
abstract class AbstractSelectionAwareAction(
	baseName: String,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractViewAction(baseName, eventBus, viewManager) {

	init {
		eventBus.register(SelectionChangeEvent::class) { updateEnabled() }
		enabled = false
	}

	/** ---- [AbstractViewAction] */

	override fun activeViewChanged(oldView: View<out InputEventContext>?, newView: View<out InputEventContext>?) {
		updateEnabled()
	}

	/** ---- [AbstractSelectionAwareAction] */

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && getSelectionCount() > 0
	}

	protected fun getSelectionCount(): Int =
		(viewManager.activeView as DrawingView<*>?)?.selectionManager?.selectionCount ?: 0

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

	protected fun getSelection(): Collection<Component> =
		(viewManager.activeView as DrawingView<*>).selectionManager.selection

	protected fun getDrawingView(): DrawingView<*>? = viewManager.activeView as DrawingView<*>

}