package ch.scorpion.jabbah.graph.login

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.module.GraphModuleJvm

class LoginLogoutAction : AbstractAction("graph.action.login") {

    private val sessionListener: EventHandler<SessionEvent> = { update() }

    init {
        BaseModule.eventBus.register(SessionEvent::class, sessionListener)
        update()
    }

    override fun dispose() {
        BaseModule.eventBus.unregister(sessionListener)
    }

    override fun execute(event: ActionEvent) {
        if (Session.exists) {
            GraphModuleJvm.loginService.logout()
        } else {
            LoginPanel.showAsDialog()
        }
    }

    private fun update() {
        name = if (Session.exists) {
            Translations.getString("graph.action.logout.name")
        } else {
            Translations.getString("graph.action.login.name")
        }
    }
}