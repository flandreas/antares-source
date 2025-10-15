package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.module.EditModule

/**
 * A base implementation of an [Action] to be used for editing objects in a [DrawingView].
 * Ony enabled if [DrawingView.editable] is `true`.
 */
abstract class AbstractEditAction(
	baseName: String,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	commandManager: CommandManager = EditModule.commandManager
) : AbstractViewAction(baseName, eventBus, viewManager) {

	private val disableWithInactiveCommandManager = ActiveCommandManagerAction(this, commandManager, eventBus)

	@Suppress("UNCHECKED_CAST")
	protected val drawingView: DrawingView<Drawing<Component>>? get() = viewManager.activeView?.view as? DrawingView<Drawing<Component>>?

	override fun dispose() {
		super.dispose()
		disableWithInactiveCommandManager.dispose()
	}

	override fun handleViewPropertyChanged(e: PropertyChangeEvent<Any>) {
		super.handleViewPropertyChanged(e)
		if (e.name == DrawingView.PROP_EDITABLE) {
			updateEnabled()
		}
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled()
			&& disableWithInactiveCommandManager.enabled
			&& drawingView?.editable ?: false
}