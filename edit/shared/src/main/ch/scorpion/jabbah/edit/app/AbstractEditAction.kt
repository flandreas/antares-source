package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView

/**
 * A base implementation of an [Action] to be used for editing objects in a [DrawingView].
 * Ony enabled if [DrawingView.editable] is `true`.
 */
abstract class AbstractEditAction(
	baseName: String,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractViewAction(baseName, eventBus, viewManager) {

	@Suppress("UNCHECKED_CAST")
	protected val drawingView: DrawingView<Drawing<Component>>? get() = viewManager.activeView as DrawingView<Drawing<Component>>?

	override fun handleViewPropertyChanged(e: PropertyChangeEvent<Any>) {
		super.handleViewPropertyChanged(e)
		if (e.name == DrawingView.PROP_EDITABLE) {
			updateEnabled()
		}
	}

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && drawingView?.editable ?: false
	}
}