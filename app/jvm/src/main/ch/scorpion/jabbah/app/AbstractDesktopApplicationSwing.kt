package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.invocation.BusyHandler
import ch.scorpion.jabbah.base.module.BaseModule
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
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.io.FileNotFoundException
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

abstract class AbstractDesktopApplicationSwing(
	args: Array<String>,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractDesktopApplication(args, eventBus) {

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
		fileChooser.fileFilter = ApplicationFileFilter(this)
		if (savable is FileSavable) {
			if (!(savable as FileSavable).filePath.isNullOrEmpty()) {
				fileChooser.selectedFile = File((savable as FileSavable).filePath)
			}
		}

		if (fileChooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
			saveFile(fileChooser.selectedFile.absolutePath)
			return true
		}

		return false
	}

	override fun open() {
		val fileChooser = JFileChooser()
		fileChooser.isAcceptAllFileFilterUsed = false
		fileChooser.fileFilter = ApplicationFileFilter(this)

		if (fileChooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
			openFile(fileChooser.selectedFile.absolutePath)
		}
	}

	/** ---- [AbstractApplication] */

	override fun init() {
		mainFrame = createMainFrame()
		mainFrame.jMenuBar = createMenuBarBuilder().menuBar
		DrawViewModule.viewManager.activeView = mainFrame.editor.view
		BusyHandler.register(mainFrame, null)
		SwingUtilities.invokeLater {
			if (commandLine.argList.size == 0) {
				newFile()
			} else {
				try {
					openFile(commandLine.argList[0])
				} catch(e: FileNotFoundException) {
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
			JOptionPane.NO_OPTION -> true
			JOptionPane.CANCEL_OPTION -> false
			JOptionPane.YES_OPTION -> savable?.save(this) ?: true
			else -> throw IllegalStateException("unsupported answer")
		}
	}

	/** ---- [AbstractDesktopApplication] */

	override fun shutdownUI() {
		mainFrame.dispose()
	}

	/** ---- [AbstractDesktopApplicationSwing] */

	protected open fun createMenuBarBuilder(): MenuBarBuilder {
		return MenuBarBuilder(this, eventBus)
	}

	protected open fun createMainFrame(): AbstractApplicationFrame {
		val canvas: Canvas = CanvasJvm({ DrawingViewImpl(DrawingImpl(), it) })
		val editor: Editor = EditEditorModule.createEditor(canvas.view as DrawingView<Drawing<Component>>)
		return SimpleApplicationFrame(this, editor, emptyList())
	}
}