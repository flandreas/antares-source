package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.module.BaseModule
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import java.io.FileNotFoundException

abstract class AbstractDesktopApplicationFx(
	protected val primaryStage: Stage,
	args: Array<String>,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractDesktopApplication(args, eventBus) {

	protected val storedWidth = BaseModule.settings.getInt("application.frame.w", 1000).toDouble()
	protected val storedHeight = BaseModule.settings.getInt("application.frame.h", 800).toDouble()

	init {
		eventBus.register(CurrentSavableEvent::class, { updateTitle() })
		primaryStage.x = BaseModule.settings.getInt("application.frame.x", 100).toDouble()
		primaryStage.y = BaseModule.settings.getInt("application.frame.y", 100).toDouble()
		primaryStage.width = storedWidth
		primaryStage.height = storedHeight
	}

	/** ---- [Application] */

	override fun start() {
		init()

	}

	override fun open() {
		val fileChooser = FileChooser()
		fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter(displayName, "*.$fileExtension"))
		val file = fileChooser.showOpenDialog(primaryStage)
		if (file != null) {
			openFile(file.absolutePath)
		}
	}

	override fun saveAs(): Boolean {
		val fileChooser = FileChooser()
		fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter(displayName, "*.$fileExtension"))

		if (savable is FileSavable) {
			if (StringUtils.isNotEmpty((savable as FileSavable).filePath)) {
				val file = File((savable as FileSavable).filePath)
				fileChooser.initialDirectory = file.parentFile!!
				fileChooser.initialFileName = file.name
			}
		}

		val file = fileChooser.showSaveDialog(primaryStage)
		if (file != null) {
			saveFile(file.absolutePath)
			return true
		}
		return false
	}

	/** ---- [AbstractApplication] */

	override fun init() {
		// TODO Install BusyHandler
		// TODO Declare active view in ViewManager
		Platform.runLater {
			if (commandLine.argList.isEmpty()) {
				newFile()
			} else {
				try {
					openFile(commandLine.argList[0])
				} catch (e: FileNotFoundException) {
					val alert = Alert(Alert.AlertType.ERROR)
					alert.title = Translations.getString("application.fileNotFound.title")
					alert.contentText = Translations.getString("application.fileNotFound.text", commandLine.argList[0])
					alert.showAndWait()
					newFile()
				}
			}
		}
	}

	override fun canReplaceSavable(actionKey: String): Boolean {
		if (!applicationDataChanged) {
			return true
		}

		val alert = Alert(Alert.AlertType.CONFIRMATION)
		alert.title = Translations.getString(actionKey)
		alert.headerText = null
		alert.contentText = Translations.getString("application.unsavedData.question")
		alert.buttonTypes.setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
		val answer = alert.showAndWait().get()

		return when(answer) {
			ButtonType.NO -> true
			ButtonType.CANCEL -> false
			ButtonType.YES -> savable?.save(this) ?: true
			else -> throw IllegalStateException("unsupported answer")
		}
	}

	/** ---- [AbstractDesktopApplication] */

	override fun shutdownUI() {
		BaseModule.settings.set("application.frame.x", primaryStage.x.toInt())
		BaseModule.settings.set("application.frame.y", primaryStage.y.toInt())
		BaseModule.settings.set("application.frame.w", primaryStage.width.toInt())
		BaseModule.settings.set("application.frame.h", primaryStage.height.toInt())
		Platform.exit()
	}

	/** ---- [AbstractDesktopApplicationFx] */

	abstract fun fillPrimaryStage(primaryStage: Stage)

	private fun updateTitle() {
		val savableName = savable?.description ?: ""
		primaryStage.title = "$displayName - $savableName"
	}

}