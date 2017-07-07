package ch.scorpion.antares.view.gate

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import java.awt.event.ActionEvent
import javax.swing.Action

/**
 * An [Action] for enabling and disabling [GateMnemonic]
 */
class GateMnemonicAction(eventBus: EventBus) : AbstractAction("antares.action.gateMnemonic") {
    constructor(): this(BaseModule.eventBus)

    init {
        eventBus.register(GateMnemonicsEvent::class, { updateState() })
        updateState()
    }

    override fun actionPerformed(e: ActionEvent?) {
        GateMnemonic.enabled = !GateMnemonic.enabled
    }

    private fun updateState() {
        putValue(Action.SELECTED_KEY, GateMnemonic.enabled)
    }
}