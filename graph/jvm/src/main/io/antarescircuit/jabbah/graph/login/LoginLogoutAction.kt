package io.antarescircuit.jabbah.graph.login

import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.module.GraphModuleJvm

class LoginLogoutAction : AbstractAction("graph.action.login", opensDialog = true) {

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