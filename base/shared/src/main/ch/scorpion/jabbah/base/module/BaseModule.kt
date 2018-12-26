package ch.scorpion.jabbah.base.module

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Settings
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.base.preferences.PreferenceGroup
import ch.scorpion.jabbah.base.time.ControlledTimeService
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.base.time.TimeService

/**
 * Module definitions for the [jabbah.base] package.
 */
object BaseModule : AbstractModule() {

	const val PREF_TREE_ROOT = "base.preferences.group.root"

	val properties: Properties = Properties()

	val preferencesTree: PreferenceGroup = PreferenceGroup(PREF_TREE_ROOT)

    var settings: Settings = Settings()

    var eventBus: EventBus = EventBusImpl()

    val systemSpeed: SystemSpeed by lazy { SystemSpeed(eventBus) }

    var timeService: TimeService = ControlledTimeService()

    override fun initialize() {
	    Translations.addBundle("jabbah-base")
    }
}