package ch.scorpion.jabbah.app.dump

import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.app.SystemMalfunctionEvent
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.UserActionTrail
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.io.output.StringBuilderWriter
import java.awt.Frame

/** Handles [SystemMalfunctionEvent]s posted on the system [EventBus] by any code. */
object SystemMalfunctionHandler {

	private val LOG by logger(SystemMalfunctionHandler::class)

	private lateinit var application: DesktopApplication
	private var currentEvent: SystemMalfunctionEvent? = null

	fun initialize(application: DesktopApplication) {
		SystemMalfunctionHandler.application = application
		BaseModule.eventBus.register(SystemMalfunctionEvent::class) { receive(it) }
	}

	/** Make sure that only one [SystemMalfunctionEvent] per AWT event is handled.*/
	private fun receive(event: SystemMalfunctionEvent) {
		if (currentEvent == null) {
			currentEvent = event
			System.invokeLater {
				handle()
			}
		}
	}

	private fun handle() {
		LOG.userTrail("Handling system malfunction: ${currentEvent!!.description}")
		sendToBackend()
		SystemMalfunctionPanel.showAsDialog(application, currentEvent!!, Frame.getFrames()[0])
		currentEvent = null
	}

	@OptIn(DelicateCoroutinesApi::class)
    private fun sendToBackend() {
		val versionId = application.aboutInfo.version.toString()
		val writer = StringBuilderWriter()
		if (versionId.isNotBlank()) {
			writer.appendLine("Version: $versionId")
		}
		writer.append(UserActionTrail.toString())
		writer.append(currentEvent!!.description)

		GlobalScope.launch(Dispatchers.IO) {
			BaseModuleJvm.unexpectedErrorService.sendUnexpectedError(writer.toString())
		}
	}
}