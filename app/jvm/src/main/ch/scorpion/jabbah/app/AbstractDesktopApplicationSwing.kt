package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.invocation.BusyHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.FileExtensionFilter
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import org.apache.commons.cli.CommandLine
import java.awt.Image
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileFilter

abstract class AbstractDesktopApplicationSwing(
	commandLine: CommandLine,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractDesktopApplication(commandLine, eventBus) {

	protected lateinit var mainFrame: AbstractApplicationFrame

	/** ---- [Application] */

	override val applicationDataChanged: Boolean get() = mainFrame.applicationDataChanged

	override fun start() {
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

	override fun saveAs(): Boolean {
		val fileChooser = JFileChooser()
		fileChooser.isAcceptAllFileFilterUsed = true
		fileChooser.isFileHidingEnabled = true
		fileChooser.fileFilter = createFileFilter()
		if (savable is FileSavable) {
			if (!(savable as FileSavable).filePath.isNullOrEmpty()) {
				fileChooser.selectedFile = File((savable as FileSavable).filePath)
			}
		}

		if (fileChooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
			saveTo(fileChooser.selectedFile.absolutePath)
			return true
		}

		return false
	}

	override fun open() {
		val fileChooser = JFileChooser()
		fileChooser.isAcceptAllFileFilterUsed = false
		fileChooser.fileFilter = createFileFilter()
		if (fileChooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
			openFrom(fileChooser.selectedFile.absolutePath)
		}
	}

	private fun createFileFilter(): FileFilter {
		return FileExtensionFilter(fileExtension, displayName)
	}

	/** ---- [AbstractApplication] */

	override fun init() {
		super.init()
		mainFrame = createMainFrame()
		mainFrame.jMenuBar = createMenuBarBuilder().menuBar
		DrawViewModule.viewManager.activeView = mainFrame.editor.view
		BusyHandler.register(mainFrame, null)
		installTaskbarIcon()
		SwingUtilities.invokeLater {
			openInitialSavable()
		}
	}

	override fun showAboutInfo() {
		AboutPanel.showAsDialog(this)
	}

	override fun canReplaceSavable(actionKey: String): Boolean {
		if (!applicationDataChanged) {
			return true
		}

		val answer =JOptionPane.showConfirmDialog(
			mainFrame,
			Translations.getString("application.unsavedData.question"),
			Translations.getString(actionKey),
			JOptionPane.YES_NO_CANCEL_OPTION,
			JOptionPane.QUESTION_MESSAGE)

		return when(answer) {
			JOptionPane.NO_OPTION -> {
				savable = null
				true
			}
			JOptionPane.CANCEL_OPTION -> false
			JOptionPane.YES_OPTION -> savable?.save(this) ?: true
			else -> throw IllegalStateException("unsupported answer")
		}
	}

	/**
	 * Opens the [Savable] to be initially available when the application starts.
	 * This implementation uses the command line argument, or opens a new file if no command line argument is available.
	 */
	protected open fun openInitialSavable() {
		if (commandLine.argList.size == 0) {
			newFile()
		} else {
			try {
				openFrom(commandLine.argList[0])
			} catch(e: IllegalArgumentException) {
				JOptionPane.showConfirmDialog(
					mainFrame,
					Translations.getString("application.fileNotFound.text", commandLine.argList[0]),
					Translations.getString("application.fileNotFound.title"),
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.ERROR_MESSAGE)
				newFile()
			}
		}
	}

	/** ---- [AbstractDesktopApplication] */

	override fun shutdownUI() {
		mainFrame.dispose()
	}

	/** ---- [AbstractDesktopApplicationSwing] */

	protected abstract val taskbarIcon: Image

	protected open fun createMenuBarBuilder(): MenuBarBuilder {
		return MenuBarBuilder(frame = mainFrame, eventBus = eventBus)
	}

	protected open fun createMainFrame(): AbstractApplicationFrame {
		val canvas: Canvas = CanvasJvm { DrawingViewImpl(DrawingImpl(), it) }
		val editor: Editor = EditEditorModule.createEditor(canvas.view as DrawingView<Drawing<Component>>)
		return SimpleApplicationFrame(this, editor, emptyList())
	}

	private fun installTaskbarIcon() {
		//Taskbar.getTaskbar().iconImage = taskbarIcon
	}
}