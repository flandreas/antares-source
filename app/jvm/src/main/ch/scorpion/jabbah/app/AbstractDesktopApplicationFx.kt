package ch.scorpion.jabbah.app

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
import java.io.FileNotFoundException

abstract class AbstractDesktopApplicationFx(
	protected val primaryStage: Stage,
	args: Array<String>,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractDesktopApplication(args, eventBus) {

	/** ---- [Application] */

	override fun start() {
		init()
	}

	override fun open() {
		val file = FileChooser().showOpenDialog(primaryStage)
		if (file != null) {
			openFile(file.absolutePath)
		}
	}

	override fun saveAs(): Boolean {
		val fileChooser = FileChooser()
		// TODO Select file if available
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
		Platform.exit()
	}

	/** ---- [AbstractDesktopApplicationFx] */

	abstract fun fillPrimaryStage(primaryStage: Stage)

}