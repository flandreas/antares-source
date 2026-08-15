package io.antarescircuit.antares.ai

import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.swing.ShowSidebarPaneContentRequest

/**
 * Opens the sidebar pane of the circuit assistant and puts the caret into its prompt field.
 */
class ShowAiChatAction(
	private val panelProvider: () -> AiChatPanelSwing?,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("antares.ai.action.show") {

	override fun calculateEnabled(): Boolean = panelProvider() != null

	override fun execute(event: ActionEvent) {
		val panel = panelProvider() ?: return
		eventBus.post(ShowSidebarPaneContentRequest(panel))
		panel.focusInput()
	}
}
