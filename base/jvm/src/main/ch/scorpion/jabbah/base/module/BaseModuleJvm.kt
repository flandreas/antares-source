package ch.scorpion.jabbah.base.module

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.time.RealTimeServiceJvm
import javafx.scene.input.KeyCode

/**
 * Setup of the [ch.scorpion.jabbah.base] module for the JVM target.
 */
object BaseModuleJvm : AbstractModule() {

	var useJavaFx: Boolean = false

	override fun initialize() {
		defineKeyCodes()

		System.SYSTEM = SystemJvm(useJavaFx)
		Math = MathJvm()
		LOG_SYSTEM = LogSystemJVM()
		Translations = TranslationsJvm()

		BaseModule.timeService = RealTimeServiceJvm()
		BaseModule.require()
	}

	private fun defineKeyCodes() {
		if (useJavaFx) {
			defineKeyCodesFx()
		} else {
			defineKeyCodesSwing()
		}
	}
	
	private fun defineKeyCodesSwing() {
		KeyEvent.VK_LEFT = java.awt.event.KeyEvent.VK_LEFT
        KeyEvent.VK_RIGHT = java.awt.event.KeyEvent.VK_RIGHT
		KeyEvent.VK_ESCAPE = java.awt.event.KeyEvent.VK_ESCAPE
		KeyEvent.VK_ENTER = java.awt.event.KeyEvent.VK_ENTER
		KeyEvent.VK_0 = java.awt.event.KeyEvent.VK_0
		KeyEvent.VK_1 = java.awt.event.KeyEvent.VK_1
	}

	private fun defineKeyCodesFx() {
		KeyEvent.VK_LEFT = KeyCode.LEFT.ordinal
		KeyEvent.VK_RIGHT = KeyCode.RIGHT.ordinal
		KeyEvent.VK_ESCAPE = KeyCode.ESCAPE.ordinal
		KeyEvent.VK_ENTER = KeyCode.ENTER.ordinal
	}
}