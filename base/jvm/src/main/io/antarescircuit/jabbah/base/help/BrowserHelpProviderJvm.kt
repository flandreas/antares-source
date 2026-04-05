package io.antarescircuit.jabbah.base.help

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.module.BaseModule
import java.awt.Frame
import javax.swing.JOptionPane

/**
 * Provides help on the JVM platform by interpreting the corresponding
 * [HelpSource] for [HelpId] as a part of an URL and by opening that
 * URL in an external web browser.
 */
class BrowserHelpProviderJvm() : HelpProvider {

	override fun provideHelpFor(helpId: HelpId?) {
		val helpSource = helpId?.let { HelpSourceRegistry.getHelpSource(it) }
		if (helpSource == null) {
			JOptionPane.showConfirmDialog(
				Frame.getFrames()[0],
				Translations.getString("base.action.help.noHelpAvailable.msg"),
				Translations.getString("base.action.help.name"),
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.ERROR_MESSAGE)
			return
		}
		System.browse("${BaseModule.baseDocumentationUrl!!.invoke()}$helpSource", Translations.getString("base.action.help.name"))
	}
}