package ch.scorpion.antares.view.gate

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * An [Action] for enabling and disabling [GateMnemonic]
 */
class GateMnemonicAction(eventBus: EventBus = BaseModule.eventBus) : AbstractAction("antares.action.gateMnemonic") {

    init {
        eventBus.register(GateMnemonicsEvent::class, { updateState() })
        updateState()
    }

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
        GateMnemonic.enabled = !GateMnemonic.enabled
    }


    private fun updateState() {
        selected = GateMnemonic.enabled
    }
}