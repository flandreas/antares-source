package ch.scorpion.jabbah.graph.login

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.event.EventBus

data class SessionEvent(val data: SessionData?)

/**
 * Established after successful login with [LoginService].
 * Holds [SessionData] e.g. containing the token used for calling protected Akrab REST services.
 * Posts [SessionEvent] on the system's [EventBus] when the session is established or dropped.
 */
object Session {

    private val LOG by logger(Session::class)

    var data: SessionData? = null
        private set(value) {
            if (field != value) {
                field = value
                BaseModule.eventBus.post(SessionEvent(value))
            }
        }

    val exists: Boolean get() = data != null

    fun establish(data: SessionData) {
        Session.data = data
        LOG.debug("Established authenticated session")
    }

    fun drop() {
        data = null
        LOG.debug("Dropped authenticated session")
    }
}