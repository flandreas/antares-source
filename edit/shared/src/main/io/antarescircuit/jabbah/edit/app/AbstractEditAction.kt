package io.antarescircuit.jabbah.edit.app

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.PropertyChangeEvent
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.view.AbstractViewAction
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.module.EditModule

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

	protected val drawingView: DrawingView<Component, Drawing<Component>>? get() = viewManager.castedActiveView()

	protected fun <T> castedDrawingView(): T? = viewManager.castedActiveView()

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