package ch.scorpion.jabbah.base.module

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.base.time.ControlledTimeService
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.base.time.TimeService

/**
 * Module definitions for the [jabbah.base] package.
 */
object BaseModule : AbstractModule() {

    /**
     * Clients should NOT keep a reference to the [Properties] in this property, because it might be replaced
     * with another implementation while bootstrapping.
     * TODO Refactoring: Find a way to solve this better
     */
	var properties: Properties = Properties()
        // Copy all current content to the new [Properties] object, which allows replacing this [Properties] object
        // (which has already been loaded from persistent store) with a more specific implementation from a
        // higher level module while bootstrapping.
        set(value) {
            value.copyFrom(field)
            field = value
        }

    var eventBus: EventBus = EventBusImpl()

    val systemSpeed: SystemSpeed by lazy { SystemSpeed(eventBus) }

    var timeService: TimeService = ControlledTimeService()

    override fun initialize() {
        // empty
    }
}