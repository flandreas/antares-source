package ch.scorpion.jabbah.base.module

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.time.RealTimeServiceJvm

/**
 * Setup of the [ch.scorpion.jabbah.base] module for the JVM target.
 */
object BaseModuleJvm : AbstractModule() {

	override fun initialize() {
		defineKeyCodes()

		System.SYSTEM = SystemJvm()
		Math = MathJvm()
		LOG_SYSTEM = LogSystemJVM()
		Translations = TranslationsJvm()

		BaseModule.timeService = RealTimeServiceJvm()
		BaseModule.require()
	}
	
	private fun defineKeyCodes() {
		KeyEvent.VK_LEFT = 0x25
        KeyEvent.VK_RIGHT = 0x27
	}
}