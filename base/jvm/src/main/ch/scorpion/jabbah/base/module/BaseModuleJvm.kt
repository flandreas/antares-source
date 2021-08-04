package ch.scorpion.jabbah.base.module

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.invocation.InteractiveErrorHandler
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.invocation.SwingInvocationHandler
import ch.scorpion.jabbah.base.preferences.BooleanPreference
import ch.scorpion.jabbah.base.preferences.PreferenceGroup
import ch.scorpion.jabbah.base.time.RealTimeServiceJvm

/**
 * Setup of the [ch.scorpion.jabbah.base] module for the JVM target.
 */
object BaseModuleJvm : AbstractModule() {

	private const val PREF_TREE_ROOT = "base.preferences.group.root"
	private const val PREF_TREE_GENERAL = "base.preferences.group.general"

	val preferencesTree: PreferenceGroup = PreferenceGroup(PREF_TREE_ROOT)

	override fun initialize() {
		defineKeyCodes()

		InvocationHandler.implementation = SwingInvocationHandler()
		BaseModule.timeService = RealTimeServiceJvm()
		BaseModule.require()

		fillProperties(BaseModule.properties)

		buildPreferencesTree(preferencesTree)
	}

	private fun fillProperties(properties: Properties) {
		properties.set(InteractiveErrorHandler.PROP_SHOW_UNEXPECTED_ERROR, true)
	}

	private fun defineKeyCodes() {
		defineKeyCodesSwing()
	}

	private fun defineKeyCodesSwing() {
		KeyEvent.VK_LEFT = java.awt.event.KeyEvent.VK_LEFT
		KeyEvent.VK_RIGHT = java.awt.event.KeyEvent.VK_RIGHT
		KeyEvent.VK_UP = java.awt.event.KeyEvent.VK_UP
		KeyEvent.VK_DOWN = java.awt.event.KeyEvent.VK_DOWN
		KeyEvent.VK_ESCAPE = java.awt.event.KeyEvent.VK_ESCAPE
		KeyEvent.VK_ALT = java.awt.event.KeyEvent.VK_ALT
		KeyEvent.VK_ENTER = java.awt.event.KeyEvent.VK_ENTER
		KeyEvent.VK_DELETE = java.awt.event.KeyEvent.VK_DELETE
		KeyEvent.VK_0 = java.awt.event.KeyEvent.VK_0
		KeyEvent.VK_1 = java.awt.event.KeyEvent.VK_1
	}

	private fun buildPreferencesTree(root: PreferenceGroup) {
		root.add(PreferenceGroup(PREF_TREE_GENERAL))

		root.getGroup(PREF_TREE_GENERAL).add(LanguagePreference())
		root.getGroup(PREF_TREE_GENERAL).add(LogLevelPreference())

		// Needs restart because ToolTips are usually cached
		root.getGroup(PREF_TREE_GENERAL).add(BooleanPreference(
			id = PROP_BEGINNER_HELP_TOOLTIP,
			nameKey = "base.preferences.beginnerTooltips",
			needsRestart = true))

		root.getGroup(PREF_TREE_GENERAL).add(BooleanPreference(
			id = InteractiveErrorHandler.PROP_SHOW_UNEXPECTED_ERROR,
			nameKey = "base.preferences.showUnexpectedErrors"))
	}
}