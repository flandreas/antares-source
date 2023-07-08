package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.event.EventBus

/**
 * Can be posted on the [Application]'s [EventBus] when a controlled malfunction
 * is detected by any code. Subsystems on the UI layer can receive this event,
 * collect the [Application]'s current state, and ask the user to attach
 * it to a newly created issue on the project's issue tracker.
 */
data class SystemMalfunctionEvent(val description: String)