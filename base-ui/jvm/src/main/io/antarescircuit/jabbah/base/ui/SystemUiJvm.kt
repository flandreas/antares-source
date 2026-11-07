package io.antarescircuit.jabbah.base.ui

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.time.RealTimeTimerJvm
import java.awt.Desktop
import java.awt.Frame
import java.net.URI
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

/** Desktop adapters for the shared System API. */
object SystemUiJvm {

	fun install() {
		System.invoker = { SwingUtilities.invokeLater(it) }
		System.timerFactory = { RealTimeTimerJvm() }
		System.browser = { url, actionName ->
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				Desktop.getDesktop().browse(URI(url))
			} else {
				JOptionPane.showConfirmDialog(
					Frame.getFrames().firstOrNull(),
					Translations.getString("application.cannotOpenUrl.name"),
					actionName,
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.ERROR_MESSAGE,
				)
			}
		}
		System.messageHandler = { title, message, isError ->
			JOptionPane.showMessageDialog(
				Frame.getFrames().firstOrNull(),
				message,
				title,
				if (isError) JOptionPane.ERROR_MESSAGE else JOptionPane.INFORMATION_MESSAGE,
			)
		}
	}
}
