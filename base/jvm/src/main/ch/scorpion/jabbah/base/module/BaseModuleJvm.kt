package ch.scorpion.jabbah.base.module

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.Modifier
import ch.scorpion.jabbah.base.help.BrowserHelpProviderJvm
import ch.scorpion.jabbah.base.help.HelpSource
import ch.scorpion.jabbah.base.help.HelpSourceRegistry
import ch.scorpion.jabbah.base.invocation.InteractiveErrorHandler
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.invocation.SwingInvocationHandler
import ch.scorpion.jabbah.base.invocation.UnexpectedErrorService
import ch.scorpion.jabbah.base.invocation.UnexpectedErrorServiceImpl
import ch.scorpion.jabbah.base.preferences.*
import ch.scorpion.jabbah.base.sound.SoundEffects
import ch.scorpion.jabbah.base.time.RealTimeServiceJvm
import ch.scorpion.jabbah.base.ui.UIBasics
import java.awt.Toolkit
import java.net.URL

/**
 * Setup of the [ch.scorpion.jabbah.base] module for the JVM target.
 */
object BaseModuleJvm : AbstractModule() {

	const val PREF_TREE_VIEW = "base.preferences.group.view"
	private const val PREF_TREE_ROOT = "base.preferences.group.root"
	const val PREF_TREE_GENERAL = "base.preferences.group.general"
	const val PREF_TREE_NETWORK = "base.preferences.group.network"
	const val PREF_TREE_RENDERING = "base.preferences.group.rendering"

	val preferencesTree: PreferenceGroup = PreferenceGroup(PREF_TREE_ROOT)

	val unexpectedErrorService: UnexpectedErrorService by lazy {
		UnexpectedErrorServiceImpl(URL(BaseModule.properties.getString(DataLocation.PROP_SERVER_URL))
	) }

	override fun initialize() {
		defineKeyCodes()

		InvocationHandler.implementation = SwingInvocationHandler()
		BaseModule.timeService = RealTimeServiceJvm()

		BaseModule.require()
		BaseModule.helpProvider = BrowserHelpProviderJvm()

		fillProperties(BaseModule.properties)

		buildPreferencesTree(preferencesTree)
		registerHelpSources()
	}

	override fun resetDependencies() {
		BaseModule.reset()
	}

	private fun fillProperties(properties: Properties) {
		properties.set(UIBasics.PROP_TREE_SHOW_ROOT_HANDLES, true)
		properties.set(InteractiveErrorHandler.PROP_SHOW_UNEXPECTED_ERROR, true)
		properties.set(FontIdentification.PROP_FONT_IDENTIFICATION, "")
	}

	private fun defineKeyCodes() {
		Modifier.Meta.mask = Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx
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
		KeyEvent.VK_SPACE = java.awt.event.KeyEvent.VK_SPACE
		KeyEvent.VK_SHIFT = java.awt.event.KeyEvent.VK_SHIFT
		KeyEvent.VK_META = java.awt.event.KeyEvent.VK_META
		KeyEvent.VK_CTRL = java.awt.event.KeyEvent.VK_CONTROL
		KeyEvent.VK_ALT_GRAPH = java.awt.event.KeyEvent.VK_ALT_GRAPH
		KeyEvent.VK_0 = java.awt.event.KeyEvent.VK_0
		KeyEvent.VK_1 = java.awt.event.KeyEvent.VK_1
		KeyEvent.VK_2 = java.awt.event.KeyEvent.VK_2
		KeyEvent.VK_3 = java.awt.event.KeyEvent.VK_3
		KeyEvent.VK_4 = java.awt.event.KeyEvent.VK_4
		KeyEvent.VK_5 = java.awt.event.KeyEvent.VK_5
		KeyEvent.VK_6 = java.awt.event.KeyEvent.VK_6
		KeyEvent.VK_7 = java.awt.event.KeyEvent.VK_7
		KeyEvent.VK_8 = java.awt.event.KeyEvent.VK_8
		KeyEvent.VK_9 = java.awt.event.KeyEvent.VK_9
		KeyEvent.VK_A = java.awt.event.KeyEvent.VK_A
		KeyEvent.VK_B = java.awt.event.KeyEvent.VK_B
		KeyEvent.VK_C = java.awt.event.KeyEvent.VK_C
		KeyEvent.VK_D = java.awt.event.KeyEvent.VK_D
		KeyEvent.VK_E = java.awt.event.KeyEvent.VK_E
		KeyEvent.VK_F = java.awt.event.KeyEvent.VK_F
		KeyEvent.VK_X = java.awt.event.KeyEvent.VK_X
		KeyEvent.VK_Z = java.awt.event.KeyEvent.VK_Z
		KeyEvent.VK_NUMPAD_0 = java.awt.event.KeyEvent.VK_NUMPAD0
		KeyEvent.VK_NUMPAD_1 = java.awt.event.KeyEvent.VK_NUMPAD1
		KeyEvent.VK_NUMPAD_2 = java.awt.event.KeyEvent.VK_NUMPAD2
		KeyEvent.VK_NUMPAD_3 = java.awt.event.KeyEvent.VK_NUMPAD3
		KeyEvent.VK_NUMPAD_4 = java.awt.event.KeyEvent.VK_NUMPAD4
		KeyEvent.VK_NUMPAD_5 = java.awt.event.KeyEvent.VK_NUMPAD5
		KeyEvent.VK_NUMPAD_6 = java.awt.event.KeyEvent.VK_NUMPAD6
		KeyEvent.VK_NUMPAD_7 = java.awt.event.KeyEvent.VK_NUMPAD7
		KeyEvent.VK_NUMPAD_8 = java.awt.event.KeyEvent.VK_NUMPAD8
		KeyEvent.VK_NUMPAD_9 = java.awt.event.KeyEvent.VK_NUMPAD9
	}

	private fun buildPreferencesTree(root: PreferenceGroup) {
		root.add(PreferenceGroup(PREF_TREE_GENERAL))
		root.add(PreferenceGroup(PREF_TREE_NETWORK))
		root.add(PreferenceGroup(PREF_TREE_RENDERING))
		root.add(PreferenceGroup(PREF_TREE_VIEW))

		root.getGroup(PREF_TREE_GENERAL).add(EnumPreference(
			id = Language.PROP_LANGUAGE,
			nameKey = "base.preferences.language",
			values = Language.entries.toTypedArray(),
			withName = Language::withCode,
			needsRestart = true
		))
		root.getGroup(PREF_TREE_GENERAL).add(LogLevelPreference())

		// Needs restart because ToolTips are usually cached
		root.getGroup(PREF_TREE_GENERAL).add(BooleanPreference(
			id = PROP_BEGINNER_HELP_TOOLTIP,
			nameKey = "base.preferences.beginnerTooltips",
			needsRestart = true))

		root.getGroup(PREF_TREE_GENERAL).add(BooleanPreference(
			id = InteractiveErrorHandler.PROP_SHOW_UNEXPECTED_ERROR,
			nameKey = "base.preferences.showUnexpectedErrors"))

		root.getGroup(PREF_TREE_NETWORK).add(IntPreference(
			id = BaseModule.PROP_CONNECTION_TIMEOUT,
			nameKey = "base.preferences.connectionTimeout",
			minValue = 1,
			needsRestart = true))

		root.getGroup(PREF_TREE_RENDERING).add(FontIdentificationPreference())

		root.getGroup(PREF_TREE_RENDERING).add(BooleanPreference(
			id = SoundEffects.PROP_ENABLE_SOUND_EFFECTS,
			nameKey = "base.preferences.soundEffects"
		))

		root.getGroup(PREF_TREE_VIEW).add(BooleanPreference(
			id = UIBasics.PROP_TREE_SHOW_ROOT_HANDLES,
			nameKey = "base.preference.treeRootHandles",
			needsRestart = true))
	}

	private fun registerHelpSources() {
		HelpSourceRegistry.register(PreferencesDialogPanel.HELP_ID, HelpSource("/misc/preferences"))
	}
}