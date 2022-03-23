package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.app.module.AppModuleJvm
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.invocation.BusyHandler
import ch.scorpion.jabbah.base.preferences.PreferencesDialogPanel
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.module.EditModule
import org.apache.commons.cli.CommandLine
import org.apache.commons.lang3.SystemUtils
import java.awt.Desktop
import java.awt.Image
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

abstract class AbstractDesktopApplicationSwing(
	commandLine: CommandLine,
	controller: ApplicationDataViewController
) : AbstractDesktopApplication(commandLine, controller) {

	protected lateinit var mainFrame: AbstractApplicationFrame

	/** ---- [Application] */

	override fun start() {
		if (SystemUtils.IS_OS_MAC) {
			installMacOSHandlers()
		}
		SwingUtilities.invokeLater {
			init()
			mainFrame.addWindowListener(object : WindowAdapter() {
				override fun windowClosing(e: WindowEvent?) {
					quit()
				}
			})
			mainFrame.isVisible = true
		}
	}

	/** ---- [AbstractApplication] */

	override fun init() {
		super.init()

		registerApplicationUsage()

		ApplicationDataViewSwing(controller, fileExtension, displayName)

		mainFrame = createMainFrame()
		mainFrame.jMenuBar = createMenuBarBuilder().menuBar

		DrawViewModule.viewManager.activeView = mainFrame.editor.view
		BusyHandler.register(mainFrame, null)

		SwingUtilities.invokeLater {
			openInitialSavable()
			checkForNewestVersion()
		}
	}

	override fun showAboutInfo() {
		AboutPanel.showAsDialog(this)
	}


	/**
	 * Opens the [Savable] to be initially available when the application starts.
	 * This implementation uses the command line argument, or opens a new file if no command line argument is available.
	 */
	protected open fun openInitialSavable() {
		if (commandLine.argList.size == 0) {
			controller.newData()
		} else {
			try {
				controller.open(FileSavable.withPath(commandLine.argList[0]))
			} catch (e: IllegalArgumentException) {
				JOptionPane.showConfirmDialog(
					mainFrame,
					Translations.getString("application.fileNotFound.text", commandLine.argList[0]),
					Translations.getString("application.fileNotFound.title"),
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.ERROR_MESSAGE)
				controller.newData()
			}
		}
	}

	private fun checkForNewestVersion() {
		val offeredVersion = AppModuleJvm.applicationVersionService.checkForNewerVersion(aboutInfo.version)
		if (offeredVersion != null) {
			NewVersionPanel.showAsDialog(mainFrame, offeredVersion)
		}
	}

	private fun registerApplicationUsage() {
		AppModuleJvm.applicationUsageService.registerUsage()
	}

	/** ---- [AbstractDesktopApplication] */

	override fun shutdownUI() {
		mainFrame.dispose()
	}

	/** ---- [AbstractDesktopApplicationSwing] */

	protected abstract val taskbarIcon: Image

	protected open fun createMenuBarBuilder(): MenuBarBuilder {
		return MenuBarBuilder(frame = mainFrame, eventBus = controller.eventBus)
	}

	protected open fun createMainFrame(): AbstractApplicationFrame {
		val canvas = CanvasJvm(EditModule.drawingViewFactory.create(DrawingImpl(), null, displayGlobalMessages = true))

		@Suppress("UNCHECKED_CAST")
		val editor: Editor = EditEditorModule.createEditor(canvas.view as DrawingView<Drawing<Component>>)

		return SimpleApplicationFrame(this, editor, emptyList())
	}

	private fun installMacOSHandlers() {
		installMacOSAboutHandler()
		installMacOSQuitHandler()
		installMacOSPreferencesHandler()
	}

	private fun installMacOSAboutHandler() {
		Desktop.getDesktop().setAboutHandler {
			AboutPanel.showAsDialog(this)
		}
	}

	private fun installMacOSQuitHandler() {
		Desktop.getDesktop().setQuitHandler { _, _ ->
			this.quit()
		}
	}

	private fun installMacOSPreferencesHandler() {
		Desktop.getDesktop().setPreferencesHandler {
			PreferencesDialogPanel.showAsDialog(mainFrame)
		}
	}
}