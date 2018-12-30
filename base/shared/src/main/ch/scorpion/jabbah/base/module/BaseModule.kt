package ch.scorpion.jabbah.base.module

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.base.time.ControlledTimeService
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.base.time.TimeService

/**
 * Module definitions for the [ch.scorpion.jabbah.base] package.
 */
object BaseModule : AbstractModule() {

	val properties: Properties = Properties()

    var settings: Settings = Settings()

    var eventBus: EventBus = EventBusImpl()

    val systemSpeed: SystemSpeed by lazy { SystemSpeed(eventBus) }

    var timeService: TimeService = ControlledTimeService()

    override fun initialize() {
	    Translations.addBundle("jabbah-base")
	    fillProperties(properties)
    }

	private fun fillProperties(properties: Properties) {
		properties.set(LogSystem.PROP_LOG_LEVEL, LogLevel.Info.name)
	}
}