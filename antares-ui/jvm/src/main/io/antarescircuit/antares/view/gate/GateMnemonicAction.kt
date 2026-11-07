package io.antarescircuit.antares.view.gate

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule

/**
 * An [Action] for enabling and disabling [GateMnemonic]
 */
class GateMnemonicAction(
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("antares.action.gateMnemonic") {

	private val gateMnemonicsHandler: EventHandler<GateMnemonicsEvent> = { updateState() }

    init {
        eventBus.register(GateMnemonicsEvent::class, gateMnemonicsHandler)
        updateState()
    }

	override fun dispose() {
		super.dispose()
		eventBus.unregister(gateMnemonicsHandler)
	}

    override fun execute(event: io.antarescircuit.jabbah.base.event.ActionEvent) {
        GateMnemonic.enabled = !GateMnemonic.enabled
    }

    private fun updateState() {
        selected = GateMnemonic.enabled
    }
}