package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionChangeEvent

/**
 * A base [Action] that is only enabled if at least one [Component] is selected.
 */
abstract class AbstractSelectionAwareAction(
	baseName: String,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractEditAction(baseName, eventBus, viewManager) {

	init {
		eventBus.register(SelectionChangeEvent::class) { updateEnabled() }
		enabled = false
	}

	protected val selection: Collection<Component> get() = drawingView!!.selectionManager.selection

	protected val selectionCount: Int get() =  drawingView?.selectionManager?.selectionCount ?: 0

	/**
	 * Returns the one and only selected [Component] or 'null' if none
	 * or more than one [Component] is selected.
	 */
	protected val singleSelection: Component? get() {
		return if (selectionCount != 1) {
			null
		} else
			drawingView?.selectionManager?.selection?.first()
	}

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && selectionCount > 0
	}
}