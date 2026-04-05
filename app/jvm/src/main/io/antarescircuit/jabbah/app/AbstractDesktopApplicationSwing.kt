package io.antarescircuit.jabbah.app

import io.antarescircuit.jabbah.app.dump.ErrorUploader
import io.antarescircuit.jabbah.app.dump.SystemMalfunctionHandler
import io.antarescircuit.jabbah.app.health.SystemHealthChecker
import io.antarescircuit.jabbah.app.module.AppModuleJvm
import io.antarescircuit.jabbah.app.rating.RatingPanel
import io.antarescircuit.jabbah.app.rating.RatingService
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.invocation.BusyHandler
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.base.preferences.PreferencesDialogPanel
import io.antarescircuit.jabbah.draw.view.CanvasJvm
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import io.antarescircuit.jabbah.edit.editor.EditEditorModule
import io.antarescircuit.jabbah.edit.model.DrawingImpl
import io.antarescircuit.jabbah.edit.module.EditModule
import org.apache.commons.cli.CommandLine
import org.apache.commons.lang3.SystemUtils
import java.awt.Desktop
import java.awt.Image
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JOptionPane

abstract class AbstractDesktopApplicationSwing(
	commandLine: CommandLine,
	controller: ApplicationDataViewController,
	private val ratingService: RatingService = AppModuleJvm.ratingService
) : AbstractDesktopApplication(commandLine, controller) {

	protected lateinit var mainFrame: AbstractApplicationFrame

	/**
	 * The file path to data to be opened when the [Application] is launched.
	 * Uses on macOS platform where this is not passed to the [Application] as a command line argument,
	 * but is received as a platform event. This event arrives at a time when the system is not yet
	 * fully initialized, and must therefore be handled later. This property stores the path received
	 * in the event to be processed later.
	 */
	protected var launchDataPath: String? = null
		private set

	/** ---- [Application] */

	override fun start() {
		if (SystemUtils.IS_OS_MAC) {
			installMacOSHandlers()
		}
		InvocationHandler.invoke {
			init()
			mainFrame.addWindowListener(object : WindowAdapter() {
				override fun windowClosing(e: WindowEvent?) {
					quit()
				}
			})
		}
	}

	/** ---- [AbstractApplication] */

	override fun init() {
		super.init()

		registerApplicationUsage()

		SystemMalfunctionHandler.initialize(this)
		ErrorUploader.initialize(this)

		SystemHealthChecker.start(controller)

		ApplicationDataViewSwing(controller, fileExtension, displayName)

		mainFrame = createMainFrame()
		mainFrame.jMenuBar = createMenuBarBuilder().menuBar
		mainFrame.isVisible = true

		BusyHandler.register(mainFrame, null)

		InvocationHandler.invoke {
			val firstUsage = isFirstUsage
			openInitialSavable()
			if (!EditAuthModule.userHolder.user.isDeveloper) {
				checkForNewestVersion()
			}
			if (firstUsage) {
				showWelcomeMessage()
			}
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
		val offeredVersion = AppModuleJvm.remoteControlService.checkForNewerVersion(aboutInfo.version)
		if (offeredVersion != null) {
			NewVersionPanel.showAsDialog(mainFrame, offeredVersion)
		}
	}

	private fun registerApplicationUsage() {
		AppModuleJvm.applicationUsageService.registerUsage()
	}

	private fun showWelcomeMessage() {
		WelcomePanel.showAsDialog(this)
	}

	/** ---- [AbstractDesktopApplication] */

	override fun handleShutdown() {
		super.handleShutdown()
		if (ratingService.requiresRating()) {
			// Cannot use InvocationHandler, App would quit before dialog is shown
			RatingPanel.showAsDialog(Translations.getString("application.rating.action.name"), this, cancelable = false, mainFrame)
		}
	}

	override fun shutdownUI() {
		mainFrame.dispose()
	}


	/** ---- [AbstractDesktopApplicationSwing] */

	protected abstract val taskbarIcon: Image

	/**
	 * Called on macOS if a file is to be opened when the [Application] is launched,
	 * or when the user double-clicks on a file that should be opened by the already
	 * running [Application].
	 * */
	protected abstract fun openFile(path: String)

	protected open fun createMenuBarBuilder(): MenuBarBuilder {
		return MenuBarBuilder(frame = mainFrame, eventBus = controller.eventBus)
	}

	protected open fun createMainFrame(): AbstractApplicationFrame {
		val canvas = CanvasJvm(EditModule.drawingViewFactory.create(DrawingImpl(), null, displayGlobalMessages = true, name = ""))

		@Suppress("UNCHECKED_CAST")
		val editor: Editor = EditEditorModule.createEditor("editor", canvas.view as DrawingView<Drawing<Component>>)

		return SimpleApplicationFrame(this, editor, emptyList())
	}

	private fun installMacOSHandlers() {
		installMacOSOpenFileHandler()
		installMacOSAboutHandler()
		installMacOSQuitHandler()
		installMacOSPreferencesHandler()
	}

	private fun installMacOSOpenFileHandler() {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.APP_OPEN_FILE)) {
			Desktop.getDesktop().setOpenFileHandler {
				if (it.files != null && it.files.isNotEmpty()) {
					try {
						openFile(it.files[0].absolutePath)
					} catch (e: Exception) {
						launchDataPath = it.files[0].absolutePath
					}
				}
			}
		}
	}

	private fun installMacOSAboutHandler() {
		Desktop.getDesktop().setAboutHandler {
			AboutPanel.showAsDialog(this)
		}
	}

	private fun installMacOSQuitHandler() {
		Desktop.getDesktop().setQuitHandler { _, response ->
			if (!this.quit()) {
				response.cancelQuit()
			}
		}
	}

	private fun installMacOSPreferencesHandler() {
		Desktop.getDesktop().setPreferencesHandler {
			PreferencesDialogPanel.showAsDialog(Translations.getString("base.preferences.action.name"), mainFrame)
		}
	}
}